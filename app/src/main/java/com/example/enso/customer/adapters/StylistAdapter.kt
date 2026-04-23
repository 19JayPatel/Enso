package com.example.enso.customer.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.enso.R
import com.google.android.material.card.MaterialCardView

data class Stylist(
    val id: Int,
    val name: String,
    val role: String,
    val imageRes: Int,
    val isAny: Boolean = false
)

class StylistAdapter(
    private val stylists: List<Stylist>,
    private val onStylistSelected: (Boolean) -> Unit
) : RecyclerView.Adapter<StylistAdapter.StylistViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stylist, parent, false)
        return StylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: StylistViewHolder, position: Int) {
        val stylist = stylists[position]
        holder.bind(stylist, position == selectedPosition)

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onStylistSelected(true)
            }
        }
    }

    override fun getItemCount(): Int = stylists.size

    class StylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.rootCard)
        private val ivStylist: ImageView = itemView.findViewById(R.id.ivStylist)
        private val ivStylistContainer: MaterialCardView = itemView.findViewById(R.id.ivStylistContainer)
        private val tvName: TextView = itemView.findViewById(R.id.tvStylistName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvStylistRole)

        fun bind(stylist: Stylist, isSelected: Boolean) {
            tvName.text = stylist.name
            tvRole.text = stylist.role
            
            ivStylist.setImageResource(stylist.imageRes)

            if (stylist.isAny) {
                ivStylist.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
                ivStylist.setPadding(32, 32, 32, 32)
                ivStylist.setColorFilter(ContextCompat.getColor(itemView.context, R.color.primary_brown))
                ivStylistContainer.setCardBackgroundColor(Color.parseColor("#FDF7F2"))
            } else {
                ivStylist.setScaleType(ImageView.ScaleType.CENTER_CROP)
                ivStylist.setPadding(0, 0, 0, 0)
                ivStylist.clearColorFilter()
                ivStylistContainer.setCardBackgroundColor(Color.TRANSPARENT)
            }

            if (isSelected) {
                card.strokeWidth = (2 * itemView.resources.displayMetrics.density).toInt()
                card.setStrokeColor(ContextCompat.getColorStateList(itemView.context, R.color.primary_brown))
            } else {
                card.strokeWidth = 0
            }
        }
    }
}