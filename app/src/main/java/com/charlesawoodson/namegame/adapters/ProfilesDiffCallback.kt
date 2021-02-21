package com.charlesawoodson.namegame.adapters

import androidx.recyclerview.widget.DiffUtil
import com.charlesawoodson.namegame.api.model.Profile

class ProfilesDiffCallback(
    private val oldList: List<Profile>,
    private val newList: List<Profile>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}