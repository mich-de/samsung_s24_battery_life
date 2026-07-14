package com.s24optimizer

import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import moe.shizuku.api.BinderContainer
import rikka.shizuku.Shizuku

class ShizukuBridgeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("ShizukuBridge", "Received binder broadcast")
        if (Shizuku.pingBinder()) return

        val container = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("moe.shizuku.privileged.api.intent.extra.BINDER", BinderContainer::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("moe.shizuku.privileged.api.intent.extra.BINDER")
        }
        if (container?.binder != null) {
            Shizuku.onBinderReceived(container.binder, context.packageName)
            Log.i("ShizukuBridge", "Binder received via broadcast!")
        }
    }
}

class ShizukuBridgeProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.i("ShizukuBridge", "onCreate")
        if (Shizuku.pingBinder()) {
            Log.i("ShizukuBridge", "Already connected")
            return true
        }

        val ctx = context ?: return true
        val pkg = ctx.packageName

        val receiver = ShizukuBridgeReceiver()
        val filter = IntentFilter("moe.shizuku.api.action.BINDER_RECEIVED")
        if (Build.VERSION.SDK_INT >= 34) {
            ctx.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            ctx.registerReceiver(receiver, filter)
        }

        val intent = Intent("rikka.shizuku.intent.action.REQUEST_BINDER")
        intent.setPackage("moe.shizuku.privileged.api")
        intent.putExtra("moe.shizuku.privileged.api.intent.extra.AUTHORITY", "$pkg.shizuku")
        intent.putExtra("moe.shizuku.privileged.api.intent.extra.PACKAGE_NAME", pkg)
        ctx.sendBroadcast(intent)
        Log.i("ShizukuBridge", "Sent REQUEST_BINDER broadcast")

        tryGetBinderFromManager(ctx)
        return true
    }

    private fun tryGetBinderFromManager(ctx: Context) {
        try {
            val uri = Uri.parse("content://moe.shizuku.privileged.api.shizuku")
            val reply = ctx.contentResolver.call(uri, "sendBinder", null, Bundle.EMPTY)
            if (reply != null) {
                reply.setClassLoader(BinderContainer::class.java.classLoader)
                val container = if (Build.VERSION.SDK_INT >= 33) {
                    reply.getParcelable("moe.shizuku.privileged.api.intent.extra.BINDER", BinderContainer::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    reply.getParcelable("moe.shizuku.privileged.api.intent.extra.BINDER")
                }
                if (container?.binder != null) {
                    Shizuku.onBinderReceived(container.binder, ctx.packageName)
                    Log.i("ShizukuBridge", "Binder received from Manager provider")
                }
            }
        } catch (e: Exception) {
            Log.w("ShizukuBridge", "Direct provider call failed", e)
        }
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val pkg = context?.packageName ?: return null
        when (method) {
            "sendBinder" -> {
                Log.i("ShizukuBridge", "Shizuku Manager called sendBinder")
                if (extras != null) {
                    extras.setClassLoader(BinderContainer::class.java.classLoader)
                    val container = if (Build.VERSION.SDK_INT >= 33) {
                        extras.getParcelable("moe.shizuku.privileged.api.intent.extra.BINDER", BinderContainer::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        extras.getParcelable("moe.shizuku.privileged.api.intent.extra.BINDER")
                    }
                    if (container?.binder != null) {
                    Shizuku.onBinderReceived(container.binder, pkg)
                    Log.i("ShizukuBridge", "Binder received via call()!")
                    }
                }
                return Bundle()
            }
            "getBinder" -> {
                val binder = Shizuku.getBinder()
                if (binder != null && binder.pingBinder()) {
                    val reply = Bundle()
                    reply.putParcelable(
                        "moe.shizuku.privileged.api.intent.extra.BINDER",
                        BinderContainer(binder)
                    )
                    return reply
                }
                return null
            }
        }
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}
