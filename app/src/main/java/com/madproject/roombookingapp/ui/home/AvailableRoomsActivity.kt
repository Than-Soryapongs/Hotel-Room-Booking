package com.madproject.roombookingapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.data.model.RoomSearchRequest
import com.madproject.roombookingapp.databinding.ActivityAvailableRoomsBinding
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.RoomViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AvailableRoomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvailableRoomsBinding
    private val roomViewModel: RoomViewModel by viewModels()
    private lateinit var adapter: AvailableRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableRoomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupList()
        val params = readSearchParams()
        if (params != null) {
            fetchRooms(params)
        } else {
            finish()
        }
        observeSearchResults()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.toolbar.title = getString(R.string.available_rooms_title)
    }

    private fun setupList() {
        adapter = AvailableRoomAdapter { room ->
            val intent = Intent(this, RoomDetailActivity::class.java).apply {
                putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.id)
                putExtra(RoomDetailActivity.EXTRA_ROOM_NUMBER, room.roomNumber)
                putExtra(RoomDetailActivity.EXTRA_ROOM_TYPE, room.roomType)
                putExtra(RoomDetailActivity.EXTRA_PRICE, room.pricePerNight?.toPlainString())
                putExtra(RoomDetailActivity.EXTRA_CAPACITY, room.capacity)
                putExtra(RoomDetailActivity.EXTRA_DESC, room.description)
                putStringArrayListExtra(RoomDetailActivity.EXTRA_IMAGES, ArrayList(room.imageUrls ?: emptyList()))
                putExtra(RoomDetailActivity.EXTRA_CHECK_IN, intent.getStringExtra(RoomDetailActivity.EXTRA_CHECK_IN))
                putExtra(RoomDetailActivity.EXTRA_CHECK_OUT, intent.getStringExtra(RoomDetailActivity.EXTRA_CHECK_OUT))
                putExtra(RoomDetailActivity.EXTRA_GUESTS, intent.getIntExtra(RoomDetailActivity.EXTRA_GUESTS, 1))
            }
            startActivity(intent)
        }
        binding.recyclerRooms.layoutManager = LinearLayoutManager(this)
        binding.recyclerRooms.adapter = adapter
    }

    private fun readSearchParams(): RoomSearchRequest? {
        val checkIn = intent.getStringExtra(EXTRA_CHECK_IN)
        val checkOut = intent.getStringExtra(EXTRA_CHECK_OUT)
        val guests = intent.getIntExtra(EXTRA_GUESTS, 1)
        if (checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) return null
        return RoomSearchRequest(
            minCapacity = guests,
            checkInDate = checkIn,
            checkOutDate = checkOut
        )
    }

    private fun fetchRooms(request: RoomSearchRequest) {
        roomViewModel.searchRooms(request)
    }

    private fun observeSearchResults() {
        roomViewModel.searchResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> showState(loading = true)
                is Resource.Success -> {
                    val rooms = result.data.orEmpty()
                    adapter.submitList(rooms)
                    showState(data = rooms.isNotEmpty())
                }
                is Resource.Error -> {
                    binding.stateError.text = result.message ?: getString(R.string.home_booking_error_generic)
                    showState(error = true)
                }
                else -> showState()
            }
        }
    }

    private fun showState(
        loading: Boolean = false,
        data: Boolean = false,
        error: Boolean = false
    ) {
        binding.progressBar.isVisible = loading
        binding.recyclerRooms.isVisible = data
        binding.stateEmpty.isVisible = !loading && !data && !error
        binding.stateError.isVisible = error
    }

    companion object {
        const val EXTRA_CHECK_IN = "extra_check_in"
        const val EXTRA_CHECK_OUT = "extra_check_out"
        const val EXTRA_GUESTS = "extra_guests"
    }
}
