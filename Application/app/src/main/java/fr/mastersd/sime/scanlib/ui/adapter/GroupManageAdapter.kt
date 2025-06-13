package fr.mastersd.sime.scanlib.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.mastersd.sime.scanlib.data.FavoriteGroup
import fr.mastersd.sime.scanlib.R

class GroupManageAdapter(
    private val groups: List<FavoriteGroup>,
    private val onSelect: (FavoriteGroup) -> Unit,
    private val onEdit: (FavoriteGroup) -> Unit,
    private val onDelete: (FavoriteGroup) -> Unit
) : RecyclerView.Adapter<GroupManageAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.groupNameTextView)
        val editBtn: ImageButton = view.findViewById(R.id.editGroupButton)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteGroupButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_manage, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.name.text = group.name
        holder.name.setOnClickListener { onSelect(group) }
        if (group.id == -1L) {
            holder.editBtn.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE
        } else {
            holder.editBtn.visibility = View.VISIBLE
            holder.deleteBtn.visibility = View.VISIBLE
            holder.editBtn.setOnClickListener { onEdit(group) }
            holder.deleteBtn.setOnClickListener { onDelete(group) }
        }
    }

    override fun getItemCount(): Int = groups.size
}
