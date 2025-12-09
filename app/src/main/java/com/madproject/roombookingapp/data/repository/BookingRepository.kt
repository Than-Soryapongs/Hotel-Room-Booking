package com.madproject.roombookingapp.data.repository

import com.madproject.roombookingapp.data.model.BookingRequest
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.data.model.PagedResponse
import com.madproject.roombookingapp.data.remote.ApiService
import com.madproject.roombookingapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val apiService: ApiService
) {

    fun createBooking(request: BookingRequest): Flow<Resource<BookingResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createBooking(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Booking creation failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getBookingById(id: Long): Flow<Resource<BookingResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getBookingById(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load booking"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getBookingByNumber(bookingNumber: String): Flow<Resource<BookingResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getBookingByNumber(bookingNumber)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load booking"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getMyBookings(page: Int? = null, size: Int? = null, sort: String? = null): Flow<Resource<PagedResponse<BookingResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMyBookings(page, size, sort)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load bookings"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getAllBookings(page: Int? = null, size: Int? = null, sort: String? = null): Flow<Resource<PagedResponse<BookingResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAllBookings(page, size, sort)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load bookings"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun cancelBooking(id: Long): Flow<Resource<Map<String, String>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.cancelBooking(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to cancel booking"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }
}
