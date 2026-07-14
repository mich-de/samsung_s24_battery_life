package com.s24optimizer.exec

import android.os.ParcelFileDescriptor
import android.util.Log
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.InputStreamReader

class AdbExecutor private constructor() :
    Shizuku.OnRequestPermissionResultListener,
    Shizuku.OnBinderReceivedListener {

    companion object {
        val instance: AdbExecutor by lazy {
            AdbExecutor().also { it.init() }
        }
    }

    @Volatile
    private var _permissionsGranted = false

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

    override fun onBinderReceived() {
        Log.i("AdbExecutor", "Shizuku binder received, version=${Shizuku.getVersion()}")
        if (Shizuku.getVersion() >= 13) {
            // Bypass checkSelfPermission() — su Android 16 il provider di Shizuku Manager
            // richiede INTERACT_ACROSS_USERS_FULL che le app normali non hanno.
            // L'app è già autorizzata via shizuku.json flags=3 (granted).
            _permissionsGranted = true
            Log.i("AdbExecutor", "Permissions bypassed (shizuku.json flags=3)")
        } else {
            Log.w("AdbExecutor", "Shizuku version too old: ${Shizuku.getVersion()}")
        }
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
            val result = execute(command)
            result.stdout.trim() == "1"
        } catch (e: Exception) {
            Log.w("AdbExecutor", "Check failed: $command", e)
            false
        }
    }

    fun execute(command: String): ExecutionResult {
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

            val t1 = Thread { stdout = stdoutStream.bufferedReader().readText() }
            val t2 = Thread { stderr = stderrStream.bufferedReader().readText() }

            t1.start()
            t2.start()

            val exitCode = remote.waitFor()
            t1.join(5000)
            t2.join(5000)

            ExecutionResult(stdout, stderr, exitCode)
        } catch (e: Exception) {
            Log.e("AdbExecutor", "Command failed", e)
            ExecutionResult("", "", -1, e.message)
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        _permissionsGranted = grantResult == 0
        Log.i("AdbExecutor", "Permission result: $grantResult, granted=$permissionsGranted")
    }
}
