package com.tonyjhuang.qfit.ui.viewgroup

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tonyjhuang.qfit.QLog
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.list_item_group_goal_user_progress.view.*

class DailyUserProgressRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<DailyUserProgressRecyclerViewAdapter.ViewHolder>() {

    var userProgress: List<CurrentUserProgress> = emptyList()
        set(value) {
            val oldValues = field
            field = value
            val diffResult =
                DiffUtil.calculateDiff(MyDiffCallback(oldValues, value))
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater
            .inflate(R.layout.list_item_group_goal_user_progress, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = userProgress.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = userProgress[position]
        holder.userInfo.text = "${item.userName} (${item.userProgressAmount})"
        if (item.inProgress) {
            holder.userInfo.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.textColorGoalAchieved
                )
            )
            holder.userPhoto.alpha = 1f
        } else {
            holder.userInfo.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.secondary_text_dark
                )
            )
            holder.userPhoto.alpha = 0.5f
        }

        holder.container.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (item.finished) R.color.finishedColorBase else R.color.plainBackground
            )
        )
        if (item.isCurrentUser) {
            holder.userInfo.setTypeface(null, Typeface.BOLD);
        } else {
            holder.userInfo.setTypeface(null, Typeface.NORMAL);
        }
        Glide.with(context)
            .load(item.userPhotoUrl)
            .into(holder.userPhoto)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        QLog.d("new onBindViewHolder position: $position ")
        onBindViewHolder(holder, position)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val container = view.container
        val userInfo = view.user_info
        val userPhoto = view.user_photo
    }


    class MyDiffCallback(
        private val oldItems: List<CurrentUserProgress>,
        private val newItems: List<CurrentUserProgress>
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

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}