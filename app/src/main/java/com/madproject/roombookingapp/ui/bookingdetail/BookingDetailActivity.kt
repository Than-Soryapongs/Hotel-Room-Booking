package com.madproject.roombookingapp.ui.bookingdetail

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import coil.load
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.databinding.ActivityBookingDetailBinding
import com.madproject.roombookingapp.util.Constants
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.viewmodel.BookingViewModel
import com.madproject.roombookingapp.viewmodel.RoomViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private val bookingViewModel: BookingViewModel by viewModels()
    private val roomViewModel: RoomViewModel by viewModels()
    private var currentBooking: BookingResponse? = null

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
        
        setupListeners()
        observeBooking()
        observeRoom()
        bookingViewModel.getBookingById(bookingId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.btnDownloadInvoice.setOnClickListener {
            currentBooking?.let { booking ->
                generateInvoicePdf(booking)
            }
        }
    }

    private fun observeBooking() {
        bookingViewModel.bookingDetail.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { 
                        currentBooking = it
                        bindBooking(it)
                        // Fetch room details to get the image
                        roomViewModel.getRoomById(it.roomId)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    binding.tvError.isVisible = true
                    binding.tvError.text = resource.message ?: "Unable to load booking"
                }
            }
        }
    }

    private fun observeRoom() {
        roomViewModel.roomDetail.observe(this) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { room ->
                    val imageUrl = room.imageUrls?.firstOrNull()
                    val fullImageUrl = if (imageUrl != null && !imageUrl.startsWith("http")) {
                        Constants.BASE_URL.trimEnd('/') + imageUrl
                    } else {
                        imageUrl
                    }
                    
                    binding.ivRoomImage.load(fullImageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.room_carousel_bg_1)
                        error(R.drawable.room_carousel_bg_2)
                    }
                }
            }
        }
    }

    private fun bindBooking(booking: BookingResponse) {
        binding.tvError.isVisible = false
        binding.scrollContent.isVisible = true
        binding.contentGroup.isVisible = true
        
        binding.tvBookingNumber.text = "#${booking.bookingNumber}"
        binding.chipStatus.text = booking.status
        
        // Set status color based on status
        val statusColor = when(booking.status.uppercase()) {
            "CONFIRMED" -> getColor(R.color.brand_primary)
            "CANCELLED" -> Color.RED
            "COMPLETED" -> Color.GREEN
            else -> Color.GRAY
        }
        binding.chipStatus.setChipBackgroundColorResource(
            if (booking.status.uppercase() == "CONFIRMED") R.color.brand_primary else android.R.color.darker_gray
        )

        binding.tvRoomType.text = booking.roomType
        binding.tvRoomNumber.text = "Room ${booking.roomNumber}"
        binding.tvGuests.text = "${booking.numberOfGuests} Guests"
        
        binding.tvCheckInDate.text = formatDate(booking.checkInDate)
        binding.tvCheckOutDate.text = formatDate(booking.checkOutDate)
        
        binding.tvCheckInTime.text = booking.checkInTime ?: "2:00 PM"
        binding.tvCheckOutTime.text = booking.checkOutTime ?: "11:00 AM"

        binding.tvTotalAmount.text = booking.totalAmount
            ?.setScale(2, RoundingMode.HALF_UP)
            ?.toPlainString()
            ?.let { "$${it}" }
            ?: "$0.00"
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.scrollContent.isVisible = !loading
        binding.tvError.isVisible = false
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "—"
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return try {
            val date = apiFormat.parse(dateStr)
            date?.let { displayFormat.format(it) } ?: dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    private fun generateInvoicePdf(booking: BookingResponse) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("INVOICE", 40f, 60f, paint)

        // Hotel Name
        paint.textSize = 16f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Hotel Room Booking App", 40f, 90f, paint)

        // Booking Details
        paint.textSize = 14f
        paint.color = Color.BLACK
        var y = 140f
        
        canvas.drawText("Booking Number: ${booking.bookingNumber}", 40f, y, paint)
        y += 30f
        canvas.drawText("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(java.util.Date())}", 40f, y, paint)
        y += 50f

        // Line separator
        paint.strokeWidth = 2f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 40f

        // Items
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("Description", 40f, y, paint)
        canvas.drawText("Amount", 450f, y, paint)
        y += 30f
        
        paint.isFakeBoldText = false
        canvas.drawText("Room Charge (${booking.roomType})", 40f, y, paint)
        canvas.drawText("${booking.totalAmount ?: 0.00}", 450f, y, paint)
        y += 30f
        
        canvas.drawText("${booking.numberOfGuests} Guests", 40f, y, paint)
        y += 50f

        // Total
        paint.strokeWidth = 2f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 40f
        
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("Total", 350f, y, paint)
        canvas.drawText("$${booking.totalAmount ?: 0.00}", 450f, y, paint)

        pdfDocument.finishPage(page)

        // Save file
        val fileName = "Invoice_${booking.bookingNumber}.pdf"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "Invoice saved to Documents: $fileName", Toast.LENGTH_LONG).show()
            
            // Open PDF
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)
            
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating invoice: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    companion object {
        const val EXTRA_BOOKING_ID = "extra_booking_id"
    }
}
