package com.madproject.roombookingapp.data.repository

import com.madproject.roombookingapp.data.model.AvailableRoomResponse
import com.madproject.roombookingapp.data.model.PagedResponse
import com.madproject.roombookingapp.data.model.RoomResponse
import com.madproject.roombookingapp.data.model.RoomSearchRequest
import com.madproject.roombookingapp.data.remote.ApiService
import com.madproject.roombookingapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val apiService: ApiService
) {

    fun getRoomById(id: Long): Flow<Resource<RoomResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getRoomById(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load room"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getRoomByNumber(roomNumber: String): Flow<Resource<RoomResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getRoomByNumber(roomNumber)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load room"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun getAllRooms(page: Int? = null, size: Int? = null, sort: String? = null): Flow<Resource<PagedResponse<RoomResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getAllRooms(page, size, sort)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to load rooms"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun searchAvailableRooms(request: RoomSearchRequest): Flow<Resource<List<AvailableRoomResponse>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.searchAvailableRooms(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to search rooms"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun checkAvailability(id: Long, checkInDate: String, checkOutDate: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.checkRoomAvailability(id, checkInDate, checkOutDate)
            if (response.isSuccessful && response.body() != null) {
                val available = response.body()!!.getOrDefault("available", false)
                emit(Resource.Success(available))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to check availability"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }
}
