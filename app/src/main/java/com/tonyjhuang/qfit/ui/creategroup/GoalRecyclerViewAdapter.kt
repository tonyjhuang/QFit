package com.tonyjhuang.qfit.ui.creategroup


import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tonyjhuang.qfit.R
import kotlinx.android.synthetic.main.create_goal_item.view.*


class GoalRecyclerViewAdapter(private val goals: List<CreateGoal>): RecyclerView.Adapter<GoalRecyclerViewAdapter.ViewHolder>() {

    private val inputForm: MutableMap<String, String> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_goal_item, parent, false)
        return ViewHolder(view, TextListener())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = goals[position]
        holder.listener.position = position
        holder.amount.setText(inputForm.getOrDefault(item.id, "0").toString(), TextView.BufferType.EDITABLE)
        holder.label.text = item.name
        holder.view.tag = item
    }

    override fun getItemCount(): Int = goals.size

    inner class ViewHolder(val view: View, val listener: TextListener) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.label
        var amount: EditText = view.amount.apply { addTextChangedListener(listener) }
    }

    inner class TextListener : TextWatcher {
        var position = 0

        override fun beforeTextChanged(charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) { }

        override fun onTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) {
            inputForm[goals[position].id] = charSequence.toString()
        }

        override fun afterTextChanged(editable: Editable) { }
    }
}
