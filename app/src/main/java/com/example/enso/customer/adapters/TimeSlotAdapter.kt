package com.example.enso.customer.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.example.enso.databinding.ItemTimeSlotBinding

data class TimeSlotModel(
    val time: String,
    var isSelected: Boolean = false
)

// WHY: Adapter needs locked slots to update UI
// WHAT: Added lockedSlotsList to constructor to receive booked times from Firebase
// HOW: Passing this list allows the adapter to check each slot against already booked ones
class TimeSlotAdapter(
    private val timeSlots: List<TimeSlotModel>,
    private val lockedSlotsList: List<String>,
    private val onTimeSelected: (Int) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedPosition = -1

    inner class TimeSlotViewHolder(val binding: ItemTimeSlotBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.binding.apply {
            
            // WHY: Slot disabled because already reserved by another user
            // WHAT: Checks if current slot time exists in the lockedSlotsList from Firebase
            // HOW: If it exists, we change the text to "Booked!" and disable clicking to prevent double booking
            if (lockedSlotsList.contains(timeSlot.time)) {
                tvTime.text = "${timeSlot.time}                                            Booked!"
                root.isEnabled = false
                root.alpha = 0.5f // Make it look disabled
                timeCard.setCardBackgroundColor(root.context.getColor(R.color.colorBackgroundCard))
                timeCard.strokeWidth = 0
            } else {
                // WHY: Available slots remain selectable for booking
                // WHAT: Resets the text and enables clicking for slots not in the locked list
                // HOW: This allows users to select from the remaining available times
                tvTime.text = timeSlot.time
                root.isEnabled = true
                root.alpha = 1.0f

                if (position == selectedPosition) {
                    timeCard.strokeWidth = 2.px
                    timeCard.setStrokeColor(ContextCompat.getColorStateList(root.context, R.color.primary_brown))
                    timeCard.setCardBackgroundColor(root.context.getColor(R.color.white))
                } else {
                    timeCard.strokeWidth = 0
                    timeCard.setCardBackgroundColor(root.context.getColor(R.color.colorBackgroundCard))
                }
            }

            root.setOnClickListener {
                if (holder.adapterPosition != RecyclerView.NO_POSITION && selectedPosition != holder.adapterPosition) {
                    val prev = selectedPosition
                    selectedPosition = holder.adapterPosition
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onTimeSelected(selectedPosition)
                }
            }
        }
    }

    override fun getItemCount() = timeSlots.size

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}
