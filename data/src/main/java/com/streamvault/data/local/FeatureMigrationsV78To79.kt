package com.streamvault.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Indices for the columns the browse and detail screens actually sort and filter on.
 *
 * Every channel-list query ends in `ORDER BY number`, and no index covered it, so SQLite
 * materialised the whole playlist and sorted it in a temporary B-tree before applying its LIMIT.
 * The movie and series detail screens look up other copies of the same title by `tmdb_id` and
 * `year`, neither of which was indexed either, so opening a title scanned the whole catalogue.
 *
 * Adding an index rewrites no rows, so this migration is safe to interrupt.
 */
object FeatureMigrationsV78To79 {
    val MIGRATION_78_79 = object : Migration(78, 79) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_channels_provider_id_number` " +
                    "ON `channels` (`provider_id`, `number`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_channels_provider_id_category_id_number` " +
                    "ON `channels` (`provider_id`, `category_id`, `number`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_movies_provider_id_tmdb_id` " +
                    "ON `movies` (`provider_id`, `tmdb_id`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_movies_provider_id_year` " +
                    "ON `movies` (`provider_id`, `year`)"
            )
            // series has tmdb_id but no year column, so there is nothing to index there.
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_series_provider_id_tmdb_id` " +
                    "ON `series` (`provider_id`, `tmdb_id`)"
            )
        }
    }
}
