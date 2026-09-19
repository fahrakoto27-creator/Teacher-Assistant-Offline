package com.fahendrena.teacherassistant.search

import com.fahendrena.teacherassistant.data.AppRepository
import com.fahendrena.teacherassistant.data.Preparation
import com.fahendrena.teacherassistant.data.ResourceDocument

data class SearchResult(
    val type: String,
    val titre: String,
    val details: String,
    val extrait: String
)

/**
 * Recherche 100% locale (aucun reseau) sur les ressources importees
 * et les preparations deja creees par l'enseignant.
 */
class SearchEngine(private val repository: AppRepository) {

    suspend fun search(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        val results = mutableListOf<SearchResult>()

        repository.searchResources(query).forEach { r: ResourceDocument ->
            results += SearchResult(
                type = "Ressource",
                titre = r.titre,
                details = "${r.categorie} . ${r.niveau} . ${r.matiere}",
                extrait = extrait(r.texteExtrait, query)
            )
        }
        repository.searchPreparations(query).forEach { p: Preparation ->
            results += SearchResult(
                type = "Preparation",
                titre = p.titreLecon,
                details = "${p.matiere} . ${p.niveau} . ${p.theme}",
                extrait = extrait(p.contenuLecon, query)
            )
        }
        return results
    }

    private fun extrait(texte: String, query: String, contexte: Int = 60): String {
        if (texte.isBlank()) return ""
        val index = texte.indexOf(query, ignoreCase = true)
        if (index == -1) return texte.take(120)
        val start = (index - contexte).coerceAtLeast(0)
        val end = (index + query.length + contexte).coerceAtMost(texte.length)
        return "..." + texte.substring(start, end) + "..."
    }
}
