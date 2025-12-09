package com.madproject.roombookingapp.data.model

/**
 * Generic Spring Data page response mapping for Retrofit/Gson.
 */
data class PagedResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val size: Int = 0,
    val number: Int = 0,
    val first: Boolean = false,
    val last: Boolean = false,
    val empty: Boolean = true
)
