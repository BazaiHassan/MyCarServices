package com.hbazai.mycarservices.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hbazai.mycarservices.data.local.dao.CarDao
import com.hbazai.mycarservices.data.local.dao.ServiceRecordDao
import com.hbazai.mycarservices.data.local.entity.CarEntity
import com.hbazai.mycarservices.data.local.entity.ServiceRecordEntity

@Database(
    entities  = [CarEntity::class, ServiceRecordEntity::class],
    version   = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun serviceRecordDao(): ServiceRecordDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cars ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE service_records ADD COLUMN cause TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE service_records ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE service_records ADD COLUMN providerName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE service_records ADD COLUMN providerPhone TEXT NOT NULL DEFAULT ''")
    }
}