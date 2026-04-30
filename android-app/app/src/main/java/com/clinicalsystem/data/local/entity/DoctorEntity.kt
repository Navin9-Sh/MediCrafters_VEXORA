package com.clinicalsystem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.clinicalsystem.domain.model.Doctor

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val specialty: String,
    val consultationFee: String,
    val avgRating: Double,
    val totalReviews: Int,
    val profileImageUrl: String?,
    val isAvailable: Boolean
) {
    fun toDomain(): Doctor {
        return Doctor(
            id = id,
            fullName = fullName,
            specialty = specialty,
            consultationFee = consultationFee,
            rating = avgRating,
            reviewCount = totalReviews,
            profileImage = profileImageUrl,
            available = isAvailable
        )
    }
}

fun Doctor.toEntity(): DoctorEntity {
    return DoctorEntity(
        id = id,
        fullName = fullName,
        specialty = specialty,
        consultationFee = consultationFee,
        avgRating = rating,
        totalReviews = reviewCount,
        profileImageUrl = profileImage,
        isAvailable = available
    )
}
