package com.madproject.roombookingapp.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import coil.load
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.data.model.BookingRequest
import com.madproject.roombookingapp.databinding.ActivityRoomDetailBinding
import com.madproject.roombookingapp.util.Constants
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailBinding
    private val bookingViewModel: BookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        bindRoom()
        observeBooking()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun bindRoom() {
        val roomNumber = intent.getStringExtra(EXTRA_ROOM_NUMBER).orEmpty()
        val roomType = intent.getStringExtra(EXTRA_ROOM_TYPE).orEmpty()
        val desc = intent.getStringExtra(EXTRA_DESC).orEmpty()
        val price = intent.getStringExtra(EXTRA_PRICE)
        val capacity = intent.getIntExtra(EXTRA_CAPACITY, 1)
        val images = intent.getStringArrayListExtra(EXTRA_IMAGES).orEmpty()

        binding.tvTitle.text = roomNumber.ifBlank { getString(R.string.room_detail_title_fallback) }
        binding.tvSubtitle.text = roomType
        binding.tvDescription.text = desc.ifBlank { getString(R.string.available_rooms_empty) }
        binding.tvCapacity.text = getString(R.string.available_room_capacity, capacity)
        binding.tvPrice.text = price?.let { getString(R.string.available_room_price, it) }
            ?: getString(R.string.available_room_price_unknown)

        val imageUrl = images.firstOrNull()
        val fullImageUrl = if (imageUrl != null && !imageUrl.startsWith("http")) {
            Constants.BASE_URL.trimEnd('/') + imageUrl
        } else {
            imageUrl
        }
        binding.ivPhoto.load(fullImageUrl) {
            crossfade(true)
            placeholder(R.drawable.room_carousel_bg_1)
            error(R.drawable.room_carousel_bg_2)
        }

        binding.btnBook.setOnClickListener { submitBooking() }
    }

    private fun submitBooking() {
        val roomId = intent.getLongExtra(EXTRA_ROOM_ID, -1L)
        val checkIn = intent.getStringExtra(EXTRA_CHECK_IN)
        val checkOut = intent.getStringExtra(EXTRA_CHECK_OUT)
        val guests = intent.getIntExtra(EXTRA_GUESTS, 1)
        if (roomId <= 0 || checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) {
            binding.stateError.isVisible = true
            binding.stateError.text = getString(R.string.room_detail_missing_params)
            return
        }
        val request = BookingRequest(
            roomId = roomId,
            checkInDate = checkIn,
            checkOutDate = checkOut,
            numberOfGuests = guests,
            specialRequests = null
        )
        bookingViewModel.createBooking(request)
    }

    private fun observeBooking() {
        bookingViewModel.bookingResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    binding.stateError.isVisible = false
                    binding.stateSuccess.isVisible = true
                    binding.stateSuccess.text = getString(R.string.room_detail_booking_success, result.data?.bookingNumber ?: "")
                }
                is Resource.Error -> {
                    showLoading(false)
                    binding.stateError.isVisible = true
                    binding.stateError.text = result.message ?: getString(R.string.home_booking_error_generic)
                }
                else -> Unit
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.btnBook.isEnabled = !loading
    }

    companion object {
        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_ROOM_NUMBER = "extra_room_number"
        const val EXTRA_ROOM_TYPE = "extra_room_type"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_CAPACITY = "extra_capacity"
        const val EXTRA_DESC = "extra_desc"
        const val EXTRA_IMAGES = "extra_images"
        const val EXTRA_CHECK_IN = "extra_check_in"
        const val EXTRA_CHECK_OUT = "extra_check_out"
        const val EXTRA_GUESTS = "extra_guests"
    }
}
