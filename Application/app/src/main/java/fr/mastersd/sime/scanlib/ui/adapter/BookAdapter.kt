package fr.mastersd.sime.scanlib.ui.adapter

import android.view.LayoutInflater
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.mastersd.sime.scanlib.databinding.ViewHolderBookBinding
import fr.mastersd.sime.scanlib.data.Book

class BookAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    inner class BookViewHolder(private val binding: ViewHolderBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            Log.d("BookAdapter", "Book: ${book.title} | thumbnailUrl: ${book.thumbnailUrl}")
            binding.bookTitle.text = book.title
            binding.bookCoverImage.load(book.thumbnailUrl) {
                crossfade(true)
            }
            binding.root.setOnClickListener { onBookClick(book) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ViewHolderBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean = oldItem == newItem
}