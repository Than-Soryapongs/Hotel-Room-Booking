package com.madproject.roombookingapp.ui.travellog

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.databinding.ActivityTravelLogBinding
import com.madproject.roombookingapp.ui.bookingdetail.BookingDetailActivity
import com.madproject.roombookingapp.ui.mybookings.MyBookingsAdapter
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.BookingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.ZoneId
import java.util.Locale

@AndroidEntryPoint
class TravelLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTravelLogBinding
    private val bookingViewModel: BookingViewModel by viewModels()
    private var allBookings: List<BookingResponse> = emptyList()
    private var filterStart: LocalDate? = null
    private var filterEnd: LocalDate? = null

    private val adapter: MyBookingsAdapter by lazy {
        MyBookingsAdapter { booking -> openBookingDetail(booking.id) }
    }

    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravelLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupList()
        setupFilters()
        observeBookings()
        bookingViewModel.getMyBookings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.madproject.roombookingapp.R.string.travel_log_title)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        binding.filterToggle.check(binding.btnFilterAll.id)
        binding.filterToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.btnFilter30.id -> applyRelativeRange(daysBack = 30)
                binding.btnFilter6m.id -> applyRelativeRange(monthsBack = 6)
                binding.btnFilter12m.id -> applyRelativeRange(monthsBack = 12)
                binding.btnFilterAll.id -> {
                    filterStart = null
                    filterEnd = null
                    renderBookings()
                }
                binding.btnFilterCustom.id -> openCustomRangePicker()
            }
        }
    }

    private fun applyRelativeRange(daysBack: Long? = null, monthsBack: Long? = null) {
        val end = LocalDate.now()
        val start = when {
            daysBack != null -> end.minusDays(daysBack)
            monthsBack != null -> end.minusMonths(monthsBack)
            else -> null
        }
        filterStart = start
        filterEnd = end
        renderBookings()
    }

    private fun openCustomRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(com.madproject.roombookingapp.R.string.travel_log_filter_custom)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection?.first
            val endMillis = selection?.second
            if (startMillis != null && endMillis != null) {
                val zone = ZoneId.systemDefault()
                val start = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalDate()
                val end = Instant.ofEpochMilli(endMillis).atZone(zone).toLocalDate()
                filterStart = start
                filterEnd = end
                renderBookings()
            }
        }

        picker.show(supportFragmentManager, "TRAVEL_LOG_RANGE")
    }

    private fun observeBookings() {
        bookingViewModel.myBookings.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    allBookings = resource.data?.content.orEmpty()
                    renderBookings()
                }
                is Resource.Error -> showError(resource.message)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.tvEmpty.isVisible = false
        binding.recyclerView.isVisible = false
    }

    private fun showError(message: String?) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.tvEmpty.isVisible = true
        binding.tvEmpty.text = message ?: getString(com.madproject.roombookingapp.R.string.travel_log_error)
        clearMetrics()
    }

    private fun renderBookings() {
        val bookings = allBookings
        binding.progressBar.isVisible = false
        if (bookings.isEmpty()) {
            binding.recyclerView.isVisible = false
            binding.tvEmpty.isVisible = true
            binding.tvEmpty.text = getString(com.madproject.roombookingapp.R.string.travel_log_empty)
            clearMetrics()
            return
        }

        val filtered = applyCurrentFilter(bookings)
        if (filtered.isEmpty()) {
            binding.recyclerView.isVisible = false
            binding.tvEmpty.isVisible = true
            binding.tvEmpty.text = getString(com.madproject.roombookingapp.R.string.travel_log_empty)
            clearMetrics()
            renderChart(emptyList())
            return
        }

        binding.tvEmpty.isVisible = false
        binding.recyclerView.isVisible = true

        val sorted = filtered.sortedByDescending { parseDate(it.checkOutDate) }
        adapter.submitList(sorted)

        val totalTrips = filtered.size
        val completed = filtered.count { statusEquals(it.status, "CHECKED_OUT") || statusEquals(it.status, "COMPLETED") }
        val upcoming = filtered.count { statusEquals(it.status, "CONFIRMED") || statusEquals(it.status, "PENDING") }
        val totalNights = filtered.sumOf { nightsStayed(it) }
        val totalSpend = filtered.fold(BigDecimal.ZERO) { acc, booking ->
            acc + (booking.totalAmount ?: BigDecimal.ZERO)
        }
        val avgNightly = if (totalNights > 0) {
            totalSpend.divide(BigDecimal.valueOf(totalNights.toLong()), 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO

        binding.tvTotalTrips.text = totalTrips.toString()
        binding.tvTotalNights.text = totalNights.toString()
        binding.tvTotalSpend.text = formatCurrency(totalSpend)
        binding.tvCompleted.text = completed.toString()
        binding.tvUpcoming.text = upcoming.toString()
        binding.tvAvgNightly.text = formatCurrency(avgNightly)

        renderChart(filtered)
    }

    private fun applyCurrentFilter(bookings: List<BookingResponse>): List<BookingResponse> {
        val start = filterStart
        val end = filterEnd
        if (start == null && end == null) return bookings
        return bookings.filter { booking ->
            val date = parseDate(booking.checkInDate) ?: parseDate(booking.checkOutDate)
            if (date == null) return@filter false
            val afterStart = start?.let { !date.isBefore(it) } ?: true
            val beforeEnd = end?.let { !date.isAfter(it) } ?: true
            afterStart && beforeEnd
        }
    }

    private fun renderChart(bookings: List<BookingResponse>) {
        val monthTotals = bookings.groupBy { booking ->
            parseDate(booking.checkInDate)?.let { YearMonth.from(it) }
        }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { entry ->
                entry.value.fold(BigDecimal.ZERO) { acc, booking -> acc + (booking.totalAmount ?: BigDecimal.ZERO) }
            }
            .toSortedMap()

        val entries = monthTotals.entries.mapIndexed { index, (yearMonth, total) ->
            BarEntry(index.toFloat(), total.toFloat())
        }
        val labels = monthTotals.keys.map { ym -> ym.month.name.substring(0, 3).lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase(Locale.getDefault()) } }

        val dataSet = BarDataSet(entries, "").apply {
            color = ContextCompat.getColor(this@TravelLogActivity, com.madproject.roombookingapp.R.color.brand_primary)
            valueTextColor = ContextCompat.getColor(this@TravelLogActivity, com.madproject.roombookingapp.R.color.brand_text_primary)
            valueTextSize = 10f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.6f
        }

        binding.barChart.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            setScaleEnabled(false)
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            invalidate()
        }
    }

    private fun parseDate(value: String?): LocalDate? = try {
        value?.let { LocalDate.parse(it, apiDateFormatter) }
    } catch (_: Exception) {
        null
    }

    private fun nightsStayed(booking: BookingResponse): Int {
        val checkIn = parseDate(booking.checkInDate)
        val checkOut = parseDate(booking.checkOutDate)
        return if (checkIn != null && checkOut != null) {
            ChronoUnit.DAYS.between(checkIn, checkOut).toInt().coerceAtLeast(0)
        } else 0
    }

    private fun statusEquals(status: String?, target: String): Boolean =
        status?.equals(target, ignoreCase = true) == true

    private fun formatCurrency(amount: BigDecimal): String {
        return "$${amount.setScale(2, RoundingMode.HALF_UP).toPlainString()}"
    }

    private fun clearMetrics() {
        binding.tvTotalTrips.text = "0"
        binding.tvTotalNights.text = "0"
        binding.tvTotalSpend.text = "$0.00"
        binding.tvCompleted.text = "0"
        binding.tvUpcoming.text = "0"
        binding.tvAvgNightly.text = "$0.00"
    }

    private fun openBookingDetail(id: Long) {
        val intent = Intent(this, BookingDetailActivity::class.java)
        intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, id)
        startActivity(intent)
    }
}
