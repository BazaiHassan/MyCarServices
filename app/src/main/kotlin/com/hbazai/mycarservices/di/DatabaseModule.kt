package com.hbazai.mycarservices.di

import android.content.Context
import androidx.room.Room
import com.hbazai.mycarservices.data.local.AppDatabase
import com.hbazai.mycarservices.data.local.MIGRATION_1_2
import com.hbazai.mycarservices.data.local.dao.CarDao
import com.hbazai.mycarservices.data.local.dao.ServiceRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_car_services.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideCarDao(db: AppDatabase): CarDao = db.carDao()

    @Provides
    fun provideServiceRecordDao(db: AppDatabase): ServiceRecordDao =
        db.serviceRecordDao()
}