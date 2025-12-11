package com.madproject.roombookingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madproject.roombookingapp.data.model.AvailableRoomResponse
import com.madproject.roombookingapp.data.model.RoomResponse
import com.madproject.roombookingapp.data.model.RoomSearchRequest
import com.madproject.roombookingapp.data.repository.RoomRepository
import com.madproject.roombookingapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _searchResult = MutableLiveData<Resource<List<AvailableRoomResponse>>>()
    val searchResult: LiveData<Resource<List<AvailableRoomResponse>>> = _searchResult

    private val _roomDetail = MutableLiveData<Resource<RoomResponse>>()
    val roomDetail: LiveData<Resource<RoomResponse>> = _roomDetail

    fun searchRooms(request: RoomSearchRequest) {
        viewModelScope.launch {
            roomRepository.searchAvailableRooms(request).collect { result ->
                _searchResult.value = result
            }
        }
    }

    fun getRoomById(id: Long) {
        viewModelScope.launch {
            roomRepository.getRoomById(id).collect { result ->
                _roomDetail.value = result
            }
        }
    }
}
