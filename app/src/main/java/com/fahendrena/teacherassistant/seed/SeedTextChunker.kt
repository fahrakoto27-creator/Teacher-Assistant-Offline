package com.fahendrena.teacherassistant.seed

/**
 * Decoupe un texte en "pages" pour permettre des citations precises
 * (document + numero de page), pour les ressources embarquees dans
 * l'application (assets). L'algorithme (regroupement de paragraphes
 * jusqu'a ~1800 caracteres) doit rester identique a celui utilise pour
 * construire les citations des fiches pre-remplies (seed_preparations.json) :
 * ne pas modifier sans regenerer ces fiches en consequence.
 */
object SeedTextChunker {
    private const val MAX_CHARS = 1800

    fun chunk(text: String, maxChars: Int = MAX_CHARS): List<String> {
        val paragraphs = text.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
        val pages = mutableListOf<String>()
        var current = mutableListOf<String>()
        var currentLen = 0
        for (p in paragraphs) {
            val pLen = p.length + 2
            if (current.isNotEmpty() && currentLen + pLen > maxChars) {
                pages += current.joinToString("\n\n")
                current = mutableListOf(p)
                currentLen = pLen
            } else {
                current += p
                currentLen += pLen
            }
        }
        if (current.isNotEmpty()) pages += current.joinToString("\n\n")
        return pages
    }
}
