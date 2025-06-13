package fr.mastersd.sime.scanlib.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.mastersd.sime.scanlib.data.Book
import fr.mastersd.sime.scanlib.databinding.ViewHolderScanResultBinding

class ScanResultAdapter(
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<ScanResultAdapter.ScanResultViewHolder>() {

    private val selectedBooks = mutableSetOf<Book>()
    private var bookList: List<Book> = emptyList()
    var onSelectionChanged: ((Set<Book>) -> Unit)? = null

    fun submitList(list: List<Book>) {
        bookList = list
        notifyDataSetChanged()
    }

    fun setSelectedBooks(books: Set<Book>) {
        selectedBooks.clear()
        selectedBooks.addAll(books)
        notifyDataSetChanged()
    }

    fun toggleBookSelection(book: Book) {
        if (selectedBooks.contains(book)) selectedBooks.remove(book) else selectedBooks.add(book)
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedBooks)
    }


    fun getSelectedBooks(): List<Book> = selectedBooks.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewHolderScanResultBinding.inflate(inflater, parent, false)
        return ScanResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
        val book = bookList[position]
        holder.bind(book)
    }

    override fun getItemCount(): Int = bookList.size

    inner class ScanResultViewHolder(private val binding: ViewHolderScanResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) = with(binding) {
            bookCoverImage.load(book.thumbnailUrl)
            bookTitle.text = book.title
            bookAuthor.text = book.authors.joinToString()
            bookPublisher.text = book.publisher ?: "Éditeur inconnu"

            // Sélection checkbox (évite double déclenchement)
            bookCheckBox.setOnCheckedChangeListener(null)
            bookCheckBox.isChecked = selectedBooks.contains(book)
            bookCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedBooks.add(book)
                else selectedBooks.remove(book)
            }

            // Clic global : toujours actif
            root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}
