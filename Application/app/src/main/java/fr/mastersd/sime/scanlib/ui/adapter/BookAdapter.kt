package fr.mastersd.sime.scanlib.ui.adapter

import android.view.LayoutInflater
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.mastersd.sime.scanlib.R
import fr.mastersd.sime.scanlib.databinding.ViewHolderBookBinding
import fr.mastersd.sime.scanlib.data.Book

class BookAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    // Dans BookAdapter
    var selectionMode = false
        private set

    private val selectedBooks = mutableSetOf<Book>()

    var onSelectionModeChanged: ((Boolean) -> Unit)? = null

    fun enterSelectionMode() {
        selectionMode = true
        notifyDataSetChanged()
        onSelectionModeChanged?.invoke(true)
    }

    fun exitSelectionMode() {
        selectionMode = false
        selectedBooks.clear()
        notifyDataSetChanged()
        onSelectionModeChanged?.invoke(false)
    }

    fun toggleBookSelection(book: Book) {
        if (selectedBooks.contains(book)) selectedBooks.remove(book) else selectedBooks.add(book)
        notifyDataSetChanged()
    }

    fun getSelectedBooks(): List<Book> = selectedBooks.toList()

    inner class BookViewHolder(private val binding: ViewHolderBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book, selectionMode: Boolean, isSelected: Boolean) {
            binding.bookTitle.text = book.title
            binding.bookCoverImage.load(book.thumbnailUrl)

            binding.selectionIndicator.visibility = if (selectionMode) View.VISIBLE else View.GONE

            if (selectionMode) {
                binding.selectionIndicator.setImageResource(
                    if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
                )
            }

            // Clic sur la couverture : sélectionne/désélectionne si en mode sélection, sinon détail
            binding.root.setOnClickListener {
                if (selectionMode) {
                    toggleBookSelection(book)
                } else {
                    onBookClick(book)
                }
            }

            // Long clic : active le mode sélection
            binding.root.setOnLongClickListener {
                if (!selectionMode) {
                    enterSelectionMode()
                    toggleBookSelection(book)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ViewHolderBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book, selectionMode, selectedBooks.contains(book))
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
}