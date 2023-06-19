package com.dankanq.rickandmorty.presentation.episode.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dankanq.rickandmorty.databinding.ItemEpisodeLargeBinding
import com.dankanq.rickandmorty.entity.episode.domain.Episode
import com.dankanq.rickandmorty.utils.presentation.diffutil.EpisodeDiffUtilCallback

class EpisodeListAdapter() :
    PagingDataAdapter<Episode, RecyclerView.ViewHolder>(EpisodeDiffUtilCallback) {
    var onEpisodeClick: ((Episode) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemEpisodeLargeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EpisodeLargeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val episode = getItem(position)
        episode?.let {
            with((holder as EpisodeLargeViewHolder).binding) {
                with(it) {
                    tvEpisodeName.text = episode.name
                    tvEpisode.text = episode.episode
                    tvAirDate.text = episode.airDate

                    root.setOnClickListener { onEpisodeClick?.invoke(this) }
                }
            }
        }
    }

    class EpisodeLargeViewHolder(
        val binding: ItemEpisodeLargeBinding
    ) : RecyclerView.ViewHolder(binding.root)
}