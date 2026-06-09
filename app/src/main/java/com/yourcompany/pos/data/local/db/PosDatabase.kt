package com.yourcompany.pos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourcompany.pos.data.local.dao.CartDao
import com.yourcompany.pos.data.local.dao.HoldOrderDao
import com.yourcompany.pos.data.local.dao.MemberDao
import com.yourcompany.pos.data.local.dao.OrderDao
import com.yourcompany.pos.data.local.dao.ProductDao
import com.yourcompany.pos.data.local.dao.SettingsDao
import com.yourcompany.pos.data.local.entity.CartItemEntity
import com.yourcompany.pos.data.local.entity.HoldOrderEntity
import com.yourcompany.pos.data.local.entity.MemberEntity
import com.yourcompany.pos.data.local.entity.OrderEntity
import com.yourcompany.pos.data.local.entity.OrderLineEntity
import com.yourcompany.pos.data.local.entity.ProductEntity
import com.yourcompany.pos.data.local.entity.SettingsEntity

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderLineEntity::class,
        MemberEntity::class,
        SettingsEntity::class,
        HoldOrderEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun memberDao(): MemberDao
    abstract fun settingsDao(): SettingsDao
    abstract fun holdOrderDao(): HoldOrderDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN nfcCardId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN category TEXT NOT NULL DEFAULT '未分類'")
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS cart_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        productId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        sku TEXT NOT NULL,
                        unitPrice REAL NOT NULL,
                        taxRate REAL NOT NULL,
                        quantity INTEGER NOT NULL,
                        imageUrl TEXT,
                        modifiersRaw TEXT NOT NULL DEFAULT '',
                        note TEXT,
                        customDiscount REAL NOT NULL DEFAULT 0.0,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO cart_items_new (productId, productName, sku, unitPrice, taxRate, quantity, imageUrl, updatedAt)
                    SELECT productId, productName, sku, unitPrice, taxRate, quantity, imageUrl, updatedAt FROM cart_items
                """.trimIndent())
                db.execSQL("DROP TABLE cart_items")
                db.execSQL("ALTER TABLE cart_items_new RENAME TO cart_items")
                
                db.execSQL("ALTER TABLE orders ADD COLUMN globalDiscount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE orders ADD COLUMN orderType TEXT NOT NULL DEFAULT '外帶'")
                db.execSQL("ALTER TABLE orders ADD COLUMN tableNumber TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE order_lines ADD COLUMN note TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE order_lines ADD COLUMN modifiersRaw TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS settings (
                        `key` TEXT PRIMARY KEY NOT NULL,
                        value TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Insert default settings
                val time = System.currentTimeMillis()
                db.execSQL("INSERT OR IGNORE INTO settings (`key`, value, updatedAt) VALUES ('storeName', 'NEON POS', $time)")
                db.execSQL("INSERT OR IGNORE INTO settings (`key`, value, updatedAt) VALUES ('storeAddress', '台北信義門市', $time)")
                db.execSQL("INSERT OR IGNORE INTO settings (`key`, value, updatedAt) VALUES ('storePhone', '02-1234-5678', $time)")
                db.execSQL("INSERT OR IGNORE INTO settings (`key`, value, updatedAt) VALUES ('taxRate', '0.05', $time)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `hold_orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `itemsJson` TEXT NOT NULL, `totalAmount` REAL NOT NULL, `note` TEXT)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN pickupNumber TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN stockQuantity INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE products ADD COLUMN lowStockThreshold INTEGER NOT NULL DEFAULT 10")
            }
        }
    }
}
