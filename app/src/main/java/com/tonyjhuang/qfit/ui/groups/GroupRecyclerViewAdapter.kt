package com.tonyjhuang.qfit.ui.groups


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.group_item.view.*


class GroupRecyclerViewAdapter(
    private val clickListener: (GroupItem) -> Unit
) : RecyclerView.Adapter<GroupRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    private var _values: List<GroupItem> = emptyList()
    var values: List<GroupItem>
        get() = _values
        set(value) {
            val oldValues = _values
            _values = value
            val diffResult =
                DiffUtil.calculateDiff(MyDiffCallback(oldValues, value))
            diffResult.dispatchUpdatesTo(this)
        }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as GroupItem
            clickListener(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.mIdView.text = item.name
        holder.mContentView.text = item.totalMembers.toString() + " members"

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }

    class MyDiffCallback(
        private val newGroupItems: List<GroupItem>,
        private val oldGroupItems: List<GroupItem>
    ) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldGroupItems.size
        }

        override fun getNewListSize(): Int {
            return newGroupItems.size
        }

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldGroupItems[oldItemPosition].id === newGroupItems[newItemPosition].id
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldGroupItems[oldItemPosition] == newGroupItems[newItemPosition]
        }
    }
}
