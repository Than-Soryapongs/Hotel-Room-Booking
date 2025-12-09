package com.madproject.roombookingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madproject.roombookingapp.data.model.BookingRequest
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.data.model.PagedResponse
import com.madproject.roombookingapp.data.repository.BookingRepository
import com.madproject.roombookingapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookingResult = MutableLiveData<Resource<BookingResponse>>()
    val bookingResult: LiveData<Resource<BookingResponse>> = _bookingResult

    private val _myBookings = MutableLiveData<Resource<PagedResponse<BookingResponse>>>()
    val myBookings: LiveData<Resource<PagedResponse<BookingResponse>>> = _myBookings

    private val _bookingDetail = MutableLiveData<Resource<BookingResponse>>()
    val bookingDetail: LiveData<Resource<BookingResponse>> = _bookingDetail

    fun createBooking(request: BookingRequest) {
        viewModelScope.launch {
            bookingRepository.createBooking(request).collect { result ->
                _bookingResult.value = result
            }
        }
    }

    fun getMyBookings(page: Int? = null, size: Int? = null, sort: String? = null) {
        viewModelScope.launch {
            bookingRepository.getMyBookings(page, size, sort).collect { result ->
                _myBookings.value = result
            }
        }
    }

    fun getBookingById(id: Long) {
        viewModelScope.launch {
            bookingRepository.getBookingById(id).collect { result ->
                _bookingDetail.value = result
            }
        }
    }
}
