package com.streamvault.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.streamvault.domain.util.ChannelNormalizer

/**
 * Recomputes every channel's `logical_group_id` from the name already stored.
 *
 * The group id is what links "Rai 1" and "IT| RAI 1 UHD" as variants of one channel. It is
 * written at import time, so a fix to [ChannelNormalizer] (such as recognising the `IT|`
 * country prefix) only reached a playlist after the user synced it again by hand, and until
 * then the player showed no variants and had no fallback stream. The name is all the
 * normalizer needs, so the regrouping runs here, offline, with no network and no user step.
 *
 * Bump the schema and add a migration like this one whenever the normalizer's output changes.
 */
object FeatureMigrationsV79To80 {
    val MIGRATION_79_80 = object : Migration(79, 80) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val changed = ArrayList<Pair<Long, String>>()
            db.query("SELECT id, name, provider_id, logical_group_id FROM channels").use { cursor ->
                while (cursor.moveToNext()) {
                    val regrouped = ChannelNormalizer.getLogicalGroupId(cursor.getString(1), cursor.getLong(2))
                    if (regrouped != cursor.getString(3)) changed += cursor.getLong(0) to regrouped
                }
            }
            if (changed.isEmpty()) return
            val update = db.compileStatement("UPDATE channels SET logical_group_id = ? WHERE id = ?")
            for ((id, groupId) in changed) {
                update.bindString(1, groupId)
                update.bindLong(2, id)
                update.executeUpdateDelete()
                update.clearBindings()
            }
            update.close()
        }
    }
}
