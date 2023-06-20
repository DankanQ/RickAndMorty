package com.dankanq.rickandmorty.presentation.episode.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.databinding.ItemEpisodeBinding
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.dankanq.rickandmorty.utils.presentation.diffutil.EpisodeDiffUtilCallback
import com.dankanq.rickandmorty.utils.presentation.viewholder.EpisodeViewHolder

class EpisodeListAdapter() :
    PagingDataAdapter<Episode, RecyclerView.ViewHolder>(EpisodeDiffUtilCallback) {
    var onEpisodeClick: ((Episode) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val episode = getItem(position)
        episode?.let {
            with((holder as EpisodeViewHolder).binding) {
                with(it) {
                    tvEpisodeName.text = episode.name
                    tvEpisode.text = episode.episode
                    tvAirDate.text = episode.airDate

                    root.setOnClickListener { onEpisodeClick?.invoke(this) }
                }
            }
        }
    }
}