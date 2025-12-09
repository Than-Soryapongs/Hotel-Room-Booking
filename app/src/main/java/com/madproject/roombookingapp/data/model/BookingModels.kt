package com.madproject.roombookingapp.data.model

import java.math.BigDecimal

/**
 * Booking-related network models matching the backend API contracts.
 */
data class BookingRequest(
    val roomId: Long,
    val checkInDate: String,
    val checkOutDate: String,
    val numberOfGuests: Int,
    val specialRequests: String? = null
)

data class BookingResponse(
    val id: Long,
    val bookingNumber: String,
    val userId: Long,
    val username: String,
    val roomId: Long,
    val roomNumber: String,
    val roomType: String,
    val checkInDate: String,
    val checkOutDate: String,
    val numberOfGuests: Int,
    val totalAmount: BigDecimal?,
    val status: String,
    val specialRequests: String?,
    val checkInTime: String?,
    val checkOutTime: String?,
    val createdAt: String?,
    val updatedAt: String?
)
