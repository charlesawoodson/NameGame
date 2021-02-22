package com.charlesawoodson.namegame.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.charlesawoodson.namegame.R
import com.charlesawoodson.namegame.api.model.Profile
import kotlinx.android.synthetic.main.list_item_profile.view.*

class ProfileAdapter(private val listener: OnProfileItemClickListener) :
    RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    private val data = ArrayList<Profile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_profile, parent, false)
        return ViewHolder(view, data, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(holder.context)
        circularProgressDrawable.start()

        Glide.with(holder.context)
            .load("http:" + data[position].headshot.url)
            .placeholder(circularProgressDrawable)
            .circleCrop()
            .into(holder.profileImageView)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(
        view: View,
        data: ArrayList<Profile>,
        listener: OnProfileItemClickListener
    ) : RecyclerView.ViewHolder(view) {

        val context: Context = itemView.context
        val profileImageView: ImageView = itemView.answerProfileImageView

        init {
            itemView.setOnClickListener {
                if (adapterPosition != -1) {
                    listener.onProfileItemClicked(data[adapterPosition].id, adapterPosition)
                }
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