package com.madproject.roombookingapp.ui.bookingdetail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.madproject.roombookingapp.databinding.ActivityBookingDetailBinding
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private val bookingViewModel: BookingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        val bookingId = intent.getLongExtra(EXTRA_BOOKING_ID, -1L)
        if (bookingId <= 0) {
            finish()
            return
        }
        observeBooking()
        bookingViewModel.getBookingById(bookingId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "Booking detail"
    }

    private fun observeBooking() {
        bookingViewModel.bookingDetail.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { bindBooking(it) }
                }
                is Resource.Error -> {
                    showLoading(false)
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message ?: "Unable to load booking"
                }
            }
        }
    }

    private fun bindBooking(booking: com.madproject.roombookingapp.data.model.BookingResponse) {
        binding.tvError.isVisible = false
        binding.scrollContent.isVisible = true
        binding.contentGroup.isVisible = true
        binding.tvBookingNumber.text = booking.bookingNumber
        binding.tvStatus.text = booking.status
        binding.tvRoom.text = "Room ${booking.roomNumber} • ${booking.roomType}"
        binding.tvGuests.text = booking.numberOfGuests.toString()
        binding.tvDates.text = formatDates(booking.checkInDate, booking.checkOutDate)
        binding.tvTotal.text = booking.totalAmount
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.toPlainString()
            ?.let { "$${it}" }
            ?: "$0.00"
        binding.tvCheckInTime.text = booking.checkInTime ?: "—"
        binding.tvCheckOutTime.text = booking.checkOutTime ?: "—"
        binding.tvSpecialRequests.text = booking.specialRequests ?: "—"
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.scrollContent.isVisible = !loading
        binding.contentGroup.isVisible = !loading
        binding.tvError.isVisible = false
    }

    private fun formatDates(checkIn: String?, checkOut: String?): String {
        if (checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) return "—"
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return try {
            val start = apiFormat.parse(checkIn)
            val end = apiFormat.parse(checkOut)
            if (start != null && end != null) {
                "${displayFormat.format(start)} - ${displayFormat.format(end)}"
            } else {
                "$checkIn - $checkOut"
            }
        } catch (_: Exception) {
            "$checkIn - $checkOut"
        }
    }

    companion object {
        const val EXTRA_BOOKING_ID = "extra_booking_id"
    }
}
