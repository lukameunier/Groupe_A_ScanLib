package fr.mastersd.sime.scanlib.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.mastersd.sime.scanlib.databinding.ViewHolderBookBinding
import fr.mastersd.sime.scanlib.domain.model.Book

/**
 * Adapter pour afficher une liste de livres dans une RecyclerView
 *
 * Utilisé dans [HomeFragment] pour représenter chaque [Book] sous forme de carte
 *
 * @param books Liste de livres à afficher
 * @param onBookClick Callback exécuté lorsqu’un livre est sélectionné
 *
 * @see Book pour les données affichées
 * @see HomeFragment pour l’utilisation de cet adapter
 */
class BookAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    /**
     * ViewHolder représentant une vue de livre unique
     *
     * Utilise ViewBinding pour accéder aux éléments de l’interface
     */
    inner class BookViewHolder(private val binding: ViewHolderBookBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Lie les données d’un [Book] à la vue correspondante
         *
         * @param book Livre à afficher
         */
        fun bind(book: Book) {
            binding.bookTitle.text = book.title
            book.thumbnailUrl?.let {
                binding.bookCoverImage.load(it)
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    /**
     * Crée une nouvelle instance de ViewHolder
     *
     * @param parent Le conteneur parent
     * @param viewType Type de vue
     * @return Un [BookViewHolder] prêt à afficher un livre
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ViewHolderBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    /**
     * Associe un livre à un ViewHolder existant
     *
     * @param holder Le ViewHolder cible
     * @param position Position du livre dans la liste
     */
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    /**
     * Retourne le nombre total d’éléments à afficher
     *
     * @return Nombre de livres dans la liste
     */
    override fun getItemCount(): Int = books.size

//================================================================================
//================================================================================
// ?: injection dynamique des data, dynamiser les mises à jour
// ?: afficher le score sur la couverture des livres ---> possible ou pas ?
// !: gérer les reherches
//================================================================================
//================================================================================

}
