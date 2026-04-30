package com.clinicalsystem.core.di

import android.content.Context
import androidx.room.Room
import com.clinicalsystem.data.local.AppDatabase
import com.clinicalsystem.data.local.dao.AppointmentDao
import com.clinicalsystem.data.local.dao.DoctorDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDoctorDao(database: AppDatabase): DoctorDao = database.doctorDao

    @Provides
    @Singleton
    fun provideAppointmentDao(database: AppDatabase): AppointmentDao = database.appointmentDao
}
