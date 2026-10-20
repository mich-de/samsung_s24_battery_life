package com.s24optimizer.data

import android.content.ContentResolver

/**
 * Master auto-sync — the single switch behind Settings > Accounts and backup > Auto-sync data.
 *
 * There is no settings key for it. `settings put global auto_sync 0` writes a row this
 * device does not otherwise have and nothing reads: `dumpsys content` keeps reporting
 * `master sync automatically: true` afterwards. The state lives inside SyncManager, and the
 * only door into it is ContentResolver's static pair below.
 *
 * That needs `android.permission.WRITE_SYNC_SETTINGS`, which is normal protection — granted
 * at install, no prompt, and no Shizuku. So this is the one mod that keeps working when the
 * shell bridge is down.
 */
object MasterSync {

    fun isEnabled(): Boolean = ContentResolver.getMasterSyncAutomatically()

    fun setEnabled(enabled: Boolean) = ContentResolver.setMasterSyncAutomatically(enabled)
}
