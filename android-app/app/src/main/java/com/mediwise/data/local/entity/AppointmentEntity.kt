package com.mediwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediwise.domain.model.Appointment

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val slotId: String,
    val date: String,
    val time: String,
    val status: String,
    val type: String
) {
    fun toDomain(): Appointment {
        return Appointment(
            id = id,
            patientId = patientId,
            doctorId = doctorId,
            doctorName = doctorName,
            doctorSpecialty = doctorSpecialty,
            slotId = slotId,
            date = date,
            time = time,
            status = status,
            type = type
        )
    }
}

fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        id = id,
        patientId = patientId,
        doctorId = doctorId,
        doctorName = doctorName,
        doctorSpecialty = doctorSpecialty,
        slotId = slotId,
        date = date,
        time = time,
        status = status,
        type = type
    )
}
