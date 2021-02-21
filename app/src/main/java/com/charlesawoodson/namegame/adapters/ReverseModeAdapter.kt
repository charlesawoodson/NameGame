package com.charlesawoodson.namegame.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.charlesawoodson.namegame.R
import com.charlesawoodson.namegame.api.model.Profile
import kotlinx.android.synthetic.main.list_item_reverse_mode.view.*

class ReverseModeAdapter(private val listener: OnProfileItemClickListener) :
    RecyclerView.Adapter<ReverseModeAdapter.ViewHolder>() {

    private val data = ArrayList<Profile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_reverse_mode, parent, false)
        return ViewHolder(view, data, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.profileTextView.text = "${data[position].firstName} ${data[position].lastName}"
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(
        view: View,
        data: ArrayList<Profile>,
        listener: OnProfileItemClickListener
    ) : RecyclerView.ViewHolder(view) {
        val profileTextView: TextView = itemView.profileTextView

        init {
            itemView.setOnClickListener {
                listener.onProfileItemClicked(data[adapterPosition].id, adapterPosition)
            }
        }
    }

    fun updateData(profiles: List<Profile>) {
        val diffCallback = ProfilesDiffCallback(data, profiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        data.clear()
        data.addAll(profiles)
        diffResult.dispatchUpdatesTo(this)
    }
}