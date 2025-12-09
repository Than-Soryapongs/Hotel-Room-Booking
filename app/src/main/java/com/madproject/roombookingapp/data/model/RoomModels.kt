package com.madproject.roombookingapp.data.model

import java.math.BigDecimal

/**
 * Room-related network models matching the backend API contracts.
 */
data class RoomResponse(
    val id: Long,
    val roomNumber: String,
    val roomType: String,
    val pricePerNight: BigDecimal?,
    val capacity: Int,
    val floor: Int?,
    val roomStatus: String,
    val description: String?,
    val hasWifi: Boolean?,
    val hasBalcony: Boolean?,
    val imageUrls: List<String>?,
    val createdAt: String?,
    val updatedAt: String?
)

data class AvailableRoomResponse(
    val id: Long,
    val roomNumber: String,
    val roomType: String,
    val pricePerNight: BigDecimal?,
    val capacity: Int,
    val floor: Int?,
    val description: String?,
    val hasWifi: Boolean?,
    val hasBalcony: Boolean?,
    val imageUrls: List<String>?,
    val isAvailable: Boolean
)

data class RoomSearchRequest(
    val roomType: String? = null,
    val minCapacity: Int? = null,
    val maxPrice: BigDecimal? = null,
    val hasWifi: Boolean? = null,
    val hasBalcony: Boolean? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null
)
