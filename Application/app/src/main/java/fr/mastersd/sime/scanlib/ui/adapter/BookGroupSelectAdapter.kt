package fr.mastersd.sime.scanlib.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import fr.mastersd.sime.scanlib.R
import android.widget.ImageView
import androidx.core.content.ContextCompat

class BookGroupSelectAdapter(
    private var _groups: List<FavoriteGroup>,
    private val _selectedGroupIds: MutableSet<Long>,
    private val onGroupCheckedChanged: (FavoriteGroup, Boolean) -> Unit
) : RecyclerView.Adapter<BookGroupSelectAdapter.GroupViewHolder>() {

    val selectedGroupIds: Set<Long> get() = _selectedGroupIds
    val groupsList: List<FavoriteGroup> get() = _groups

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkIcon: ImageView = view.findViewById(R.id.groupCheckIcon)
        val name: TextView = view.findViewById(R.id.groupNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_select, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = _groups[position]
        val isChecked = selectedGroupIds.contains(group.id)
        holder.name.text = group.name

        // Affiche le check à gauche
        holder.checkIcon.setImageResource(if (isChecked) R.drawable.ic_check else R.drawable.ic_circle)
        holder.checkIcon.setColorFilter(
            ContextCompat.getColor(
                holder.itemView.context,
                if (isChecked) R.color.primary_button else R.color.secondary_background
            )
        )
        // Sélection directe
        holder.itemView.setOnClickListener {
            if (isChecked) {
                _selectedGroupIds.remove(group.id)
                onGroupCheckedChanged(group, false)
            } else {
                _selectedGroupIds.add(group.id)
                onGroupCheckedChanged(group, true)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = _groups.size

    fun updateGroups(newGroups: List<FavoriteGroup>, newSelected: Set<Long>) {
        _groups = newGroups
        _selectedGroupIds.clear()
        _selectedGroupIds.addAll(newSelected)
        notifyDataSetChanged()
    }
}
