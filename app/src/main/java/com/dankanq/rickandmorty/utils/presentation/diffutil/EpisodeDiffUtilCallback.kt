package com.dankanq.rickandmorty.utils.presentation.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.dankanq.rickandmorty.entity.episode.domain.Episode

object EpisodeDiffUtilCallback : DiffUtil.ItemCallback<Episode>() {
    override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem == newItem
    }
}