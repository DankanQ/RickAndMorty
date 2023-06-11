package com.dankanq.rickandmorty.presentation.character.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dankanq.rickandmorty.R
import com.dankanq.rickandmorty.databinding.ItemCharacterBinding
import com.dankanq.rickandmorty.entity.character.domain.Character

class CharactersAdapter(
    private val context: Context,
) : PagingDataAdapter<Character, RecyclerView.ViewHolder>(CharacterDiffUtilCallback) {
    var onCharacterClick: ((Character) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemCharacterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val character = getItem(position)
        character?.let {
            with((holder as CharacterViewHolder).binding) {
                with(it) {
                    Glide.with(context)
                        .load(it.image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(sivImage)

                    val statusColorId = getStatusColorId(it.status)
                    val colorStateList = ContextCompat.getColorStateList(context, statusColorId)
                    vStatus.backgroundTintList = colorStateList
                    tvStatus.text = it.status

                    tvName.text = it.name

                    tvGender.text = it.gender
                    tvSpecies.text = it.species

                    root.setOnClickListener { onCharacterClick?.invoke(this) }
                }
            }
        }
    }

    private fun getStatusColorId(status: String): Int {
        return when (status) {
            "Alive" -> R.color.alive
            "Dead" -> R.color.dead
            else -> R.color.unknown
        }
    }
}