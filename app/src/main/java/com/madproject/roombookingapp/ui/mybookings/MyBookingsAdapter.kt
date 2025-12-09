package com.madproject.roombookingapp.ui.mybookings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.databinding.ItemBookingBinding
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyBookingsAdapter(
    private val onBookingClick: (BookingResponse) -> Unit
) : ListAdapter<BookingResponse, MyBookingsAdapter.BookingViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<BookingResponse>() {
        override fun areItemsTheSame(oldItem: BookingResponse, newItem: BookingResponse): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BookingResponse, newItem: BookingResponse): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding, onBookingClick)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookingViewHolder(
        private val binding: ItemBookingBinding,
        private val onBookingClick: (BookingResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(booking: BookingResponse) {
            binding.tvRoomNumber.text = "Room ${booking.roomNumber}"
            binding.tvRoomType.text = booking.roomType
            binding.tvStatus.text = booking.status
            binding.tvPrice.text = booking.totalAmount
                ?.setScale(2, RoundingMode.HALF_UP)
                ?.toPlainString()
                ?.let { "$${it}" }
                ?: "$0.00"
            binding.tvDates.text = formatDates(booking.checkInDate, booking.checkOutDate)
            binding.root.setOnClickListener { onBookingClick(booking) }
        }

        private fun formatDates(checkIn: String?, checkOut: String?): String {
            if (checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) return ""
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return try {
                val start: Date? = apiFormat.parse(checkIn)
                val end: Date? = apiFormat.parse(checkOut)
                if (start != null && end != null) {
                    "${displayFormat.format(start)} - ${displayFormat.format(end)}"
                } else {
                    "$checkIn - $checkOut"
                }
            } catch (_: Exception) {
                "$checkIn - $checkOut"
            }
        }
    }
}
