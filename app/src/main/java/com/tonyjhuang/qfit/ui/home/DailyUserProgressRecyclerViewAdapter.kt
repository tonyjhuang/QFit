package com.tonyjhuang.qfit.ui.home

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.list_item_daily_user_progress.view.*
import kotlinx.android.synthetic.main.list_item_group_target.view.*


class DailyUserProgressRecyclerViewAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<DailyUserProgressRecyclerViewAdapter.ViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    var dailyUserProgress: List<DailyUserProgress> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_daily_user_progress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dailyUserProgress[position]
        holder.currentProgress.text = "${item.userProgress} ${item.goalName}"

        holder.targetRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false).apply {
                    stackFromEnd = true
                }
            val viewAdapter =
                GroupTargetRecyclerViewAdapter(context, item.groupTargets, item.userProgress)
            adapter = viewAdapter

            setRecycledViewPool(viewPool)
        }

        if (item.finished) {
            holder.container.setBackgroundColor(holder.container.context.getColor(R.color.finishedColorBase))
        } else {
            holder.container.setBackgroundColor(holder.container.context.getColor(android.R.color.background_light))
        }
    }

    override fun getItemCount(): Int {
        return dailyUserProgress.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.container
        val targetRecyclerView: RecyclerView = view.group_target_container
        val currentProgress: TextView = view.current_progress
        val initiateProgressUpdateButton: Button = view.initiate_progress_update.apply {
            setOnClickListener {
                listener.initiateProgressUpdate(dailyUserProgress[absoluteAdapterPosition].goalId)
            }
        }
    }

    interface Listener {
        fun initiateProgressUpdate(goalId: String)

        fun onGroupClicked(groupId: String)
    }


    inner class GroupTargetRecyclerViewAdapter(
        private val context: Context,
        groupTargets: List<GroupTarget>,
        private val progressAmount: Int
    ) : RecyclerView.Adapter<GroupTargetRecyclerViewAdapter.ViewHolder>() {

        private val targets = groupTargets.sortedBy {
            it.goalAmount
        }.reversed()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_group_target, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = targets.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val groupTarget = targets[position]
            holder.label.text = "${groupTarget.goalAmount} ${groupTarget.groupName}"
            val achieved = groupTarget.goalAmount <= progressAmount
            if (achieved) {
                holder.label.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.textColorGoalAchieved
                    )
                )
                holder.label.setTypeface(null, Typeface.BOLD);
            } else {
                holder.label.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.textColorGoalUnachieved
                    )
                )
                holder.label.setTypeface(null, Typeface.NORMAL);
            }
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val label: TextView = view.group_target_label.apply {
                setOnClickListener {
                    listener.onGroupClicked(targets[absoluteAdapterPosition].id)
                }
            }
        }
    }
}


