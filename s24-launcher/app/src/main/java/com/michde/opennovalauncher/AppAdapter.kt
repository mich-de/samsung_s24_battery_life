package com.michde.opennovalauncher

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michde.opennovalauncher.databinding.ItemAppBinding

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

sealed class LauncherItem {
    abstract val label: String

    data class SingleApp(val app: AppInfo) : LauncherItem() {
        override val label: String get() = app.label
    }

    data class Folder(
        val folderId: String,
        var name: String,
        val apps: MutableList<AppInfo> = mutableListOf()
    ) : LauncherItem() {
        override val label: String get() = name
    }
}

class AppAdapter(
    private val context: Context,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit,
    private val onFolderClick: (LauncherItem.Folder) -> Unit,
    private val onFolderLongClick: (LauncherItem.Folder) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private var fullList: List<LauncherItem> = emptyList()
    private var filteredList: List<LauncherItem> = emptyList()

    fun setItems(items: List<LauncherItem>) {
        this.fullList = items
        this.filteredList = items
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                when (it) {
                    is LauncherItem.SingleApp -> it.app.label.contains(query, ignoreCase = true)
                    is LauncherItem.Folder -> it.name.contains(query, ignoreCase = true) || it.apps.any { a -> a.label.contains(query, ignoreCase = true) }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun getItemCountTotal(): Int = filteredList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        when (val item = filteredList[position]) {
            is LauncherItem.SingleApp -> {
                holder.binding.appIcon.setImageDrawable(item.app.icon)
                holder.binding.appLabel.text = item.app.label
                holder.itemView.setOnClickListener {
                    onAppClick(item.app)
                }
                holder.itemView.setOnLongClickListener {
                    onAppLongClick(item.app)
                    true
                }
            }
            is LauncherItem.Folder -> {
                holder.binding.appIcon.setImageResource(R.drawable.ic_folder)
                holder.binding.appLabel.text = "${item.name} (${item.apps.size})"
                holder.itemView.setOnClickListener {
                    onFolderClick(item)
                }
                holder.itemView.setOnLongClickListener {
                    onFolderLongClick(item)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = filteredList.size

    class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)
}
