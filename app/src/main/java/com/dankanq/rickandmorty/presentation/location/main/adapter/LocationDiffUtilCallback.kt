package com.dankanq.rickandmorty.presentation.location.main.adapter

import androidx.recyclerview.widget.DiffUtil
import com.dankanq.rickandmorty.entity.location.domain.Location

object LocationDiffUtilCallback : DiffUtil.ItemCallback<Location>() {
    override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem == newItem
    }
}