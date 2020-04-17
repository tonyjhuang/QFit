package com.tonyjhuang.qfit.ui.home

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.list_item_daily_user_progress.view.*
import kotlinx.android.synthetic.main.list_item_group_target.view.*


class DailyUserProgressRecyclerViewAdapter(
    private val context: Context,
    private val listener: (goalId: String, updateAmount: Int) -> Unit
) :
    RecyclerView.Adapter<DailyUserProgressRecyclerViewAdapter.ViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    var dailyUserProgress: List<DailyUserProgress> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var activeProgressEditPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_daily_user_progress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dailyUserProgress[position]
        holder.currentProgress.text = "${item.userProgress} ${item.goalName}"
        holder.progressUpdateAmount.setText("0", TextView.BufferType.EDITABLE)

        if (position == activeProgressEditPosition) {
            holder.progressUpdateContainer.visibility = View.VISIBLE
            holder.initiateProgressUpdateButton.visibility = View.INVISIBLE
        } else {
            holder.progressUpdateContainer.visibility = View.INVISIBLE
            holder.initiateProgressUpdateButton.visibility = View.VISIBLE
        }

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
    }

    fun onSaveProgressButtonClicked(position: Int, updateAmount: Int) {
        activeProgressEditPosition = -1
        listener(dailyUserProgress[position].goalId, updateAmount)
        Handler().post {
             notifyItemChanged(position)
        }
    }

    fun onInitiateProgressUpdateButtonClicked(position: Int) {
        val oldPosition = activeProgressEditPosition
        activeProgressEditPosition = position
        Handler().post {
            notifyItemChanged(oldPosition)
            notifyItemChanged(activeProgressEditPosition)
        }
    }

    override fun getItemCount(): Int {
        return dailyUserProgress.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val targetRecyclerView: RecyclerView = view.group_target_container
        val currentProgress = view.current_progress
        val initiateProgressUpdateButton = view.initiate_progress_update.apply {
            setOnClickListener {
                progressUpdateAmount.setText("0", TextView.BufferType.EDITABLE)
                progressUpdateAmount.requestFocus()
                onInitiateProgressUpdateButtonClicked(absoluteAdapterPosition)
            }
        }
        val progressUpdateContainer = view.progress_update_container
        val progressUpdateAmount = view.progress_update_amount
        val saveProgressButton = view.save_progress_update.apply {
            setOnClickListener {
                onSaveProgressButtonClicked(
                    absoluteAdapterPosition,
                    progressUpdateAmount.text.toString().toIntOrNull() ?: 0
                )
            }
        }
    }
}

class GroupTargetRecyclerViewAdapter(
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
        val label = view.group_target_label
    }
}

