package com.madproject.roombookingapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.databinding.ActivityHomeBinding
import com.madproject.roombookingapp.ui.travellog.TravelLogActivity
import com.madproject.roombookingapp.ui.mybookings.HistoryBookingsActivity
import com.madproject.roombookingapp.ui.mybookings.MyBookingsActivity
import com.madproject.roombookingapp.ui.profile.ProfileActivity
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.RoomViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var checkInDate: LocalDate? = null
    private var checkOutDate: LocalDate? = null
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @Inject
    lateinit var repository: AuthRepository

    private val roomViewModel: RoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()
        setupClickListeners()
        setupBookingForm()
        setupRoomCarousel()
        observeRoomSearch()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = repository.getUserData()
            user?.let {
                val fullName = listOfNotNull(it.firstName, it.lastName)
                    .map { part -> part.trim() }
                    .filter { part -> part.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { it.username }
                binding.tvGreeting.text = getString(R.string.home_greeting, fullName)
                binding.tvSubtitle.text = getString(R.string.home_subtitle)
                binding.tvMembershipTier.text = it.roles.firstOrNull()
                    ?.removePrefix("ROLE_")
                    ?.lowercase(Locale.getDefault())
                    ?.replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
                    }
                    ?: getString(R.string.home_membership_tier)
            }
        }
    }

    private fun setupClickListeners() {
        val openProfileListener = View.OnClickListener {
            openProfile()
        }
        binding.ivAvatar.setOnClickListener(openProfileListener)

        binding.cardMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        binding.cardTravelLog.setOnClickListener {
            startActivity(Intent(this, TravelLogActivity::class.java))
        }

        binding.btnPlanStay.setOnClickListener {
            binding.homeScroll.post {
                binding.homeScroll.smoothScrollTo(0, binding.cardBooking.top)
            }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.homeScroll.smoothScrollTo(0, 0)
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryBookingsActivity::class.java))
                    true
                }
                R.id.nav_location, R.id.nav_membership -> {
                    Snackbar.make(binding.root, getString(R.string.home_nav_placeholder, item.title), Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBookingForm() {
        val dateFieldListener = View.OnClickListener {
            openDateRangePicker()
        }
        binding.etCheckIn.apply {
            keyListener = null
            setOnClickListener(dateFieldListener)
        }
        binding.etCheckOut.apply {
            keyListener = null
            setOnClickListener(dateFieldListener)
        }

        binding.btnSearchRooms.setOnClickListener {
            val checkIn = checkInDate
            val checkOut = checkOutDate
            val guests = binding.etGuests.text?.toString()?.toIntOrNull() ?: 1
            if (checkIn == null || checkOut == null) {
                Snackbar.make(binding.root, R.string.home_booking_missing_dates, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val request = com.madproject.roombookingapp.data.model.RoomSearchRequest(
                minCapacity = guests,
                checkInDate = checkIn.format(isoFormatter),
                checkOutDate = checkOut.format(isoFormatter)
            )
            roomViewModel.searchRooms(request)
        }
    }

    private fun setupRoomCarousel() {
        val rooms = listOf(
            RoomCarouselItem(
                backgroundRes = R.drawable.room_carousel_bg_1,
                title = getString(R.string.home_carousel_title_executive),
                subtitle = getString(R.string.home_carousel_subtitle_executive),
                priceRange = getString(R.string.home_carousel_price_executive)
            ),
            RoomCarouselItem(
                backgroundRes = R.drawable.room_carousel_bg_2,
                title = getString(R.string.home_carousel_title_coastal),
                subtitle = getString(R.string.home_carousel_subtitle_coastal),
                priceRange = getString(R.string.home_carousel_price_coastal)
            ),
            RoomCarouselItem(
                backgroundRes = R.drawable.room_carousel_bg_3,
                title = getString(R.string.home_carousel_title_loft),
                subtitle = getString(R.string.home_carousel_subtitle_loft),
                priceRange = getString(R.string.home_carousel_price_loft)
            )
        )

        val carouselAdapter = RoomCarouselAdapter(rooms)
        val pageMargin = resources.getDimensionPixelSize(R.dimen.home_carousel_page_margin)
        binding.carouselRooms.apply {
            adapter = carouselAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = rooms.size
            setPageTransformer(MarginPageTransformer(pageMargin))
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        TabLayoutMediator(binding.carouselIndicator, binding.carouselRooms) { tab, _ ->
            tab.setCustomView(R.layout.item_carousel_indicator)
        }.attach()
    }

    private fun openDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.home_date_picker_title)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val start = selection?.first
            val end = selection?.second
            if (start != null && end != null) {
                checkInDate = Instant.ofEpochMilli(start)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                checkOutDate = Instant.ofEpochMilli(end)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                updateBookingFields()
            }
        }

        picker.show(supportFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun updateBookingFields() {
        binding.etCheckIn.setText(checkInDate?.format(displayDateFormatter).orEmpty())
        binding.etCheckOut.setText(checkOutDate?.format(displayDateFormatter).orEmpty())
    }

    private fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun observeRoomSearch() {
        roomViewModel.searchResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnSearchRooms.isEnabled = false
                }
                is Resource.Success -> {
                    binding.btnSearchRooms.isEnabled = true
                    val rooms = result.data.orEmpty()
                    if (rooms.isEmpty()) {
                        Snackbar.make(binding.root, R.string.home_booking_no_rooms, Snackbar.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(this, AvailableRoomsActivity::class.java).apply {
                            putExtra(AvailableRoomsActivity.EXTRA_CHECK_IN, checkInDate?.format(isoFormatter))
                            putExtra(AvailableRoomsActivity.EXTRA_CHECK_OUT, checkOutDate?.format(isoFormatter))
                            putExtra(AvailableRoomsActivity.EXTRA_GUESTS, binding.etGuests.text?.toString()?.toIntOrNull() ?: 1)
                        }
                        startActivity(intent)
                    }
                }
                is Resource.Error -> {
                    binding.btnSearchRooms.isEnabled = true
                    Snackbar.make(
                        binding.root,
                        result.message ?: getString(R.string.home_booking_error_generic),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    binding.btnSearchRooms.isEnabled = true
                }
            }
        }
    }

}