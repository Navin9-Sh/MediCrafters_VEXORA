package com.mediwise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mediwise.data.local.dao.AppointmentDao
import com.mediwise.data.local.dao.DoctorDao
import com.mediwise.data.local.entity.AppointmentEntity
import com.mediwise.data.local.entity.DoctorEntity

@Database(
    entities = [DoctorEntity::class, AppointmentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val doctorDao: DoctorDao
    abstract val appointmentDao: AppointmentDao
    
    companion object {
        const val DATABASE_NAME = "clinical_db"
    }
}
