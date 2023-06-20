package com.dankanq.rickandmorty.presentation.location.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.databinding.ItemLocationBinding
import com.dankanq.rickandmorty.entity.location.domain.Location

class LocationListAdapter() :
    PagingDataAdapter<Location, RecyclerView.ViewHolder>(LocationDiffUtilCallback) {
    var onLocationClick: ((Location) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val location = getItem(position)
        location?.let {
            with((holder as LocationViewHolder).binding) {
                with(it) {
                    tvName.text = location.name
                    tvType.text = location.type
                    tvDimension.text = location.dimension

                    root.setOnClickListener { onLocationClick?.invoke(this) }
                }
            }
        }
    }

    class LocationViewHolder(
        val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root)
}