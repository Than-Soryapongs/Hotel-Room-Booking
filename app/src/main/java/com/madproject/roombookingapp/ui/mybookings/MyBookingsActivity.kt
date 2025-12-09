package com.madproject.roombookingapp.ui.mybookings

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.databinding.ActivityMyBookingsBinding
import com.madproject.roombookingapp.ui.bookingdetail.BookingDetailActivity
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyBookingsActivity : AppCompatActivity() {

    private val bookingViewModel: BookingViewModel by viewModels()

    private lateinit var binding: ActivityMyBookingsBinding

    private val bookingsAdapter: MyBookingsAdapter by lazy {
        MyBookingsAdapter { booking ->
            openBookingDetail(booking.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecycler()
        observeBookings()
        bookingViewModel.getMyBookings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My bookings"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyBookingsActivity)
            adapter = bookingsAdapter
        }
    }

    private fun observeBookings() {
        bookingViewModel.myBookings.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> showBookings(resource.data?.content ?: emptyList())
                is Resource.Error -> showError(resource.message)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.tvEmptyMessage.isVisible = false
    }

    private fun showBookings(bookings: List<BookingResponse>) {
        binding.progressBar.isVisible = false
        val confirmed = bookings.filter { it.status.equals("CONFIRMED", ignoreCase = true) }
        if (confirmed.isEmpty()) {
            binding.tvEmptyMessage.isVisible = true
            binding.tvEmptyMessage.text = "No confirmed bookings yet"
            binding.recyclerView.isVisible = false
        } else {
            binding.tvEmptyMessage.isVisible = false
            binding.recyclerView.isVisible = true
            bookingsAdapter.submitList(confirmed)
        }
    }

    private fun showError(message: String?) {
        binding.progressBar.isVisible = false
        binding.tvEmptyMessage.isVisible = true
        binding.tvEmptyMessage.text = message ?: "Failed to load bookings"
        binding.recyclerView.isVisible = false
    }

    private fun openBookingDetail(id: Long) {
        val intent = Intent(this, BookingDetailActivity::class.java)
        intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, id)
        startActivity(intent)
    }
}
