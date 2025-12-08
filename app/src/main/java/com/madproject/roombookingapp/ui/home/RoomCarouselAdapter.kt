package com.madproject.roombookingapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.madproject.roombookingapp.R
import com.madproject.roombookingapp.databinding.ItemRoomCarouselBinding

class RoomCarouselAdapter(
    private val items: List<RoomCarouselItem>
) : RecyclerView.Adapter<RoomCarouselAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRoomCarouselBinding.inflate(inflater, parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class RoomViewHolder(
        private val binding: ItemRoomCarouselBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoomCarouselItem) = with(binding) {
            ivRoomBackground.setImageResource(item.backgroundRes)
            tvRoomTitle.text = item.title
            tvRoomSubtitle.text = item.subtitle
            tvRoomPrice.text = item.priceRange

            btnRoomDetails.setOnClickListener {
                Snackbar.make(root, root.context.getString(R.string.home_carousel_message), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
