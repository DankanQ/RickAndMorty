package com.dankanq.rickandmorty.presentation.character.detail.adapters

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.R
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

internal class InfoSection(
    private val infoLabel: String,
    private val infoValue: String,
    private val context: Context
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_info)
        .build()
) {

    override fun getContentItemsTotal(): Int {
        return itemsTotal
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return InfoViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val infoViewHolder: InfoViewHolder = holder as InfoViewHolder
        with(infoViewHolder) {
            tvInfoLabel.text = infoLabel
            tvInfoValue.text = infoValue
            if (infoLabel == context.getString(R.string.Status)) {
                when (tvInfoValue.text) {
                    context.getString(R.string.alive) -> tvInfoValue.setTextColor(
                        ContextCompat.getColor(context, R.color.alive)
                    )
                    context.getString(R.string.dead) -> tvInfoValue.setTextColor(
                        ContextCompat.getColor(context, R.color.dead)
                    )
                    context.getString(R.string.unknown) -> tvInfoValue.setTextColor(
                        ContextCompat.getColor(context, R.color.unknown)
                    )
                }
            }
        }
    }

    class InfoViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        val tvInfoLabel: TextView = view.findViewById(R.id.tv_info_label)
        val tvInfoValue: TextView = view.findViewById(R.id.tv_info_value)
    }

    companion object {
        private const val itemsTotal = 1
    }
}