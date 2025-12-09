package com.madproject.roombookingapp.ui.mybookings

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.databinding.ActivityHistoryBookingsBinding
import com.madproject.roombookingapp.ui.bookingdetail.BookingDetailActivity
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class HistoryBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBookingsBinding
    private val bookingViewModel: BookingViewModel by viewModels()

    private val adapter: MyBookingsAdapter by lazy {
        MyBookingsAdapter { booking -> openBookingDetail(booking.id) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecycler()
        observeBookings()
        bookingViewModel.getMyBookings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "History"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun observeBookings() {
        bookingViewModel.myBookings.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> showData(resource.data?.content.orEmpty())
                is Resource.Error -> showError(resource.message)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.tvEmptyMessage.isVisible = false
    }

    private fun showData(bookings: List<BookingResponse>) {
        binding.progressBar.isVisible = false
        val checkedOut = bookings.filter { booking ->
            val status = booking.status.uppercase(Locale.getDefault())
            status == "CHECKED_OUT" || status == "COMPLETED"
        }
        if (checkedOut.isEmpty()) {
            binding.tvEmptyMessage.isVisible = true
            binding.tvEmptyMessage.text = "No past stays yet"
            binding.recyclerView.isVisible = false
        } else {
            binding.tvEmptyMessage.isVisible = false
            binding.recyclerView.isVisible = true
            adapter.submitList(checkedOut)
        }
    }

    private fun showError(message: String?) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.tvEmptyMessage.isVisible = true
        binding.tvEmptyMessage.text = message ?: "Failed to load bookings"
    }

    private fun openBookingDetail(id: Long) {
        val intent = Intent(this, BookingDetailActivity::class.java)
        intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, id)
        startActivity(intent)
    }
}
