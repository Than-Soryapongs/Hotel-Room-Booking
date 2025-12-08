package com.madproject.roombookingapp.ui.home

import androidx.annotation.DrawableRes

data class RoomCarouselItem(
    @DrawableRes val backgroundRes: Int,
    val title: String,
    val subtitle: String,
    val priceRange: String
)
