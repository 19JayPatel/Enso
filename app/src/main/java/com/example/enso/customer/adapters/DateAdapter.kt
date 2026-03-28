package com.example.enso.customer.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.databinding.ItemDateCardBinding

data class DateModel(
    val day: String,
    val date: String,
    val duration: String,
    val isMoreDates: Boolean = false,
    var isSelected: Boolean = false
)

class DateAdapter(
    private val dates: List<DateModel>,
    private val onDateSelected: (Int) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition = -1

    inner class DateViewHolder(val binding: ItemDateCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dates[position]
        holder.binding.apply {
            if (date.isMoreDates) {
                tvDay.visibility = View.GONE
                tvDate.visibility = View.GONE
                tvDuration.visibility = View.GONE
                ivCalendar.visibility = View.VISIBLE
                tvMoreDates.visibility = View.VISIBLE
            } else {
                tvDay.visibility = View.VISIBLE
                tvDate.visibility = View.VISIBLE
                tvDuration.visibility = View.VISIBLE
                ivCalendar.visibility = View.GONE
                tvMoreDates.visibility = View.GONE

                tvDay.text = date.day
                tvDate.text = date.date
                tvDuration.text = date.duration
            }

            if (position == selectedPosition) {
                dateCard.strokeWidth = 2.px
                dateCard.setStrokeColor(ContextCompat.getColorStateList(root.context, R.color.primary_brown))
                dateCard.setCardBackgroundColor(root.context.getColor(R.color.white))
            } else {
                dateCard.strokeWidth = 0
                dateCard.setCardBackgroundColor(root.context.getColor(R.color.colorBackgroundCard))
            }

            root.setOnClickListener {
                if (holder.adapterPosition != RecyclerView.NO_POSITION && selectedPosition != holder.adapterPosition) {
                    val prev = selectedPosition
                    selectedPosition = holder.adapterPosition
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onDateSelected(selectedPosition)
                }
            }
        }
    }

    override fun getItemCount() = dates.size

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}