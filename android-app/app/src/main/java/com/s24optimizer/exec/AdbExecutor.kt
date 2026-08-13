package com.s24optimizer.exec

import android.content.pm.PackageManager
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.util.concurrent.CopyOnWriteArrayList

class AdbExecutor private constructor() :
    Shizuku.OnRequestPermissionResultListener,
    Shizuku.OnBinderReceivedListener {

    companion object {
        private const val TAG = "AdbExecutor"

        /** Guard against a hung shell wedging whatever thread called us. */
        const val DEFAULT_TIMEOUT_MS = 10_000L

        private const val SEP = "__S24_SEP__"

        val instance: AdbExecutor by lazy {
            AdbExecutor().also { it.init() }
        }
    }

    @Volatile
    private var _permissionsGranted = false

    private val readyListeners = CopyOnWriteArrayList<() -> Unit>()

    val permissionsGranted: Boolean get() = isConnected && _permissionsGranted

    val isConnected: Boolean get() = Shizuku.pingBinder()

    private fun init() {
        Shizuku.addRequestPermissionResultListener(this)
        Shizuku.addBinderReceivedListenerSticky(this)
        try {
            Shizuku.getBinder()
        } catch (_: Exception) {
            // will be delivered async
        }
    }

    fun removeListener() {
        Shizuku.removeRequestPermissionResultListener(this)
        Shizuku.removeBinderReceivedListener(this)
    }

    /**
     * Runs [block] as soon as Shizuku is usable — immediately if it already is, otherwise
     * when permission arrives. Replaces the old fire-once-and-give-up pattern, where a
     * task that ran before Shizuku was ready simply never happened.
     */
    fun onReady(block: () -> Unit) {
        if (permissionsGranted) {
            runListener(block)
        } else {
            readyListeners.add(block)
        }
    }

    private fun runListener(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Ready listener failed", e)
        }
    }

    private fun flushReadyListeners() {
        if (!permissionsGranted || readyListeners.isEmpty()) return
        val pending = readyListeners.toList()
        readyListeners.clear()
        Thread({ pending.forEach { runListener(it) } }, "shizuku-ready").apply {
            isDaemon = true
        }.start()
    }

    override fun onBinderReceived() {
        Log.i(TAG, "Shizuku binder received, version=${Shizuku.getVersion()}")
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _permissionsGranted = true
                Log.i(TAG, "Permission already granted")
                flushReadyListeners()
                return
            }
        } catch (e: SecurityException) {
            // Android 16: checkSelfPermission() requires INTERACT_ACROSS_USERS_FULL
            // shizuku.json flags=3 should auto-grant, requestPermission() triggers it
            Log.i(TAG, "checkSelfPermission blocked (Android 16), requesting...")
        }
        Shizuku.requestPermission(0)
        Log.i(TAG, "Permission requested (id=0)")
    }

    data class ExecutionResult(
        val stdout: String,
        val stderr: String,
        val exitCode: Int,
        val error: String? = null
    ) {
        val isSuccess = exitCode == 0 && error == null
        override fun toString(): String {
            if (error != null) return "ERR: $error"
            val out = stdout.trim()
            val err = stderr.trim()
            return when {
                err.isNotEmpty() -> "$out\nERR: $err (code $exitCode)"
                out.isNotEmpty() -> out
                else -> if (exitCode == 0) "OK" else "Exit code $exitCode"
            }
        }
    }

    fun check(command: String): Boolean {
        if (command.isBlank()) return false
        return try {
            execute(command).stdout.trim() == "1"
        } catch (e: Exception) {
            Log.w(TAG, "Check failed: $command", e)
            false
        }
    }

    fun execute(command: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS): ExecutionResult {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Not fatal, but every occurrence is a latent ANR. Surface it in logcat.
            Log.w(TAG, "execute() called on the main thread: $command")
        }
        if (!permissionsGranted) return ExecutionResult("", "", -1, "Shizuku not granted")

        return try {
            val binder = Shizuku.getBinder()
                ?: return ExecutionResult("", "", -1, "No Shizuku binder")
            val service = IShizukuService.Stub.asInterface(binder)
            val remote = service.newProcess(arrayOf("sh", "-c", command), null, null)

            val stdoutStream = ParcelFileDescriptor.AutoCloseInputStream(remote.inputStream)
            val stderrStream = ParcelFileDescriptor.AutoCloseInputStream(remote.errorStream)

            var stdout = ""
            var stderr = ""

            val t1 = reader("shell-out", stdoutStream) { stdout = it }
            val t2 = reader("shell-err", stderrStream) { stderr = it }
            t1.start()
            t2.start()

            var exitCode = -1
            val waiter = Thread({
                exitCode = try { remote.waitFor() } catch (_: Exception) { -1 }
            }, "shell-wait").apply { isDaemon = true }
            waiter.start()
            waiter.join(timeoutMs)

            if (waiter.isAlive) {
                // waitFor() has no timeout of its own; kill the process so the binder
                // call returns instead of parking this thread forever.
                Log.w(TAG, "Timeout after ${timeoutMs}ms, destroying: $command")
                try { remote.destroy() } catch (_: Exception) {}
                waiter.join(1_000)
                t1.join(1_000); t2.join(1_000)
                return ExecutionResult(stdout, stderr, -1, "Timeout after ${timeoutMs}ms")
            }

            t1.join(2_000)
            t2.join(2_000)

            ExecutionResult(stdout, stderr, exitCode)
        } catch (e: Exception) {
            Log.e(TAG, "Command failed", e)
            ExecutionResult("", "", -1, e.message)
        }
    }

    /**
     * Runs many commands inside a single shell. Spawning one process per command costs
     * tens of milliseconds each, which is the difference between a snappy startup and a
     * multi-second stall when checking the whole catalog.
     */
    fun executeBatch(commands: List<String>, timeoutMs: Long = 60_000L): List<ExecutionResult> {
        if (commands.isEmpty()) return emptyList()
        val script = commands.joinToString("\n") { "$it\necho $SEP\$?" }
        val result = execute(script, timeoutMs)
        if (result.error != null) return commands.map { result }

        val out = ArrayList<ExecutionResult>(commands.size)
        val buffer = StringBuilder()
        for (line in result.stdout.lineSequence()) {
            if (line.startsWith(SEP)) {
                val code = line.removePrefix(SEP).trim().toIntOrNull() ?: -1
                out.add(ExecutionResult(buffer.toString(), "", code))
                buffer.setLength(0)
            } else {
                if (buffer.isNotEmpty()) buffer.append('\n')
                buffer.append(line)
            }
        }
        // A truncated or malformed run must not silently shift results onto the wrong
        // commands: pad rather than misalign.
        while (out.size < commands.size) {
            out.add(ExecutionResult("", "", -1, "No output"))
        }
        return out.subList(0, commands.size)
    }

    private fun reader(name: String, stream: java.io.InputStream, sink: (String) -> Unit) =
        Thread({
            sink(try { stream.bufferedReader().readText() } catch (_: Exception) { "" })
        }, name).apply { isDaemon = true }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        _permissionsGranted = grantResult == 0
        Log.i(TAG, "Permission result: $grantResult, granted=$permissionsGranted")
        flushReadyListeners()
    }
}
