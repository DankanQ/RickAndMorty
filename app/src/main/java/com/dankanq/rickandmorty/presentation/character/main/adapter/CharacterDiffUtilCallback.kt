package com.dankanq.rickandmorty.presentation.character.main.adapter

import androidx.recyclerview.widget.DiffUtil
import com.dankanq.rickandmorty.entity.character.domain.Character

object CharacterDiffUtilCallback : DiffUtil.ItemCallback<Character>() {
    override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
        return oldItem == newItem
    }
}