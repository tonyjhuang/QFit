package com.tonyjhuang.qfit.ui.viewgroup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tonyjhuang.qfit.R

class UserPhotosRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<UserPhotosRecyclerViewAdapter.ViewHolder>() {

    var users: List<GroupMember> = emptyList()
        set(value) {
            val oldValues = field
            field = value
            val diffResult =
                DiffUtil.calculateDiff(GroupMemberDiffCallback(oldValues, value))
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_item_user_photo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = users[position]
        Glide.with(context)
            .load(item.userPhotoUrl)
            .into(holder.imageView)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view as ImageView
    }
}

class GroupMemberDiffCallback(
    private val oldItems: List<GroupMember>,
    private val newItems: List<GroupMember>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition].userId === newItems[newItemPosition].userId
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}