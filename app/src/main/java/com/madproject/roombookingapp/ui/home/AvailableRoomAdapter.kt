package com.madproject.roombookingapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.data.model.AvailableRoomResponse
import com.madproject.roombookingapp.databinding.ItemAvailableRoomBinding
import java.math.RoundingMode

class AvailableRoomAdapter(
    private val onClick: (AvailableRoomResponse) -> Unit
) : ListAdapter<AvailableRoomResponse, AvailableRoomAdapter.RoomViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemAvailableRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RoomViewHolder(
        private val binding: ItemAvailableRoomBinding,
        private val onClick: (AvailableRoomResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: AvailableRoomResponse) {
            binding.tvTitle.text = room.roomNumber
            binding.tvSubtitle.text = room.roomType
            binding.tvCapacity.text = binding.root.context.getString(R.string.available_room_capacity, room.capacity)
            binding.tvPrice.text = room.pricePerNight?.let {
                val price = it.setScale(2, RoundingMode.HALF_UP).toPlainString()
                binding.root.context.getString(R.string.available_room_price, price)
            } ?: binding.root.context.getString(R.string.available_room_price_unknown)

            val imageUrl = room.imageUrls?.firstOrNull()
            binding.ivPhoto.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.room_carousel_bg_1)
                error(R.drawable.room_carousel_bg_2)
            }

            binding.root.setOnClickListener { onClick(room) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<AvailableRoomResponse>() {
        override fun areItemsTheSame(oldItem: AvailableRoomResponse, newItem: AvailableRoomResponse): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AvailableRoomResponse, newItem: AvailableRoomResponse): Boolean =
            oldItem == newItem
    }
}
