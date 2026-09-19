package com.fahendrena.teacherassistant.generation

import org.json.JSONArray
import org.json.JSONObject

/**
 * Reference vers l'endroit exact (document + page) d'ou provient une
 * information reutilisee dans une fiche generee. Permet a l'enseignant de
 * retrouver l'origine de chaque contenu (section "citation des sources"
 * du cahier des charges).
 */
data class Citation(
    val document: String,
    val page: Int,
    val categorie: String
) {
    fun toJson(): JSONObject = JSONObject()
        .put("document", document)
        .put("page", page)
        .put("categorie", categorie)

    companion object {
        fun listToJson(citations: List<Citation>): String {
            val arr = JSONArray()
            citations.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJson(json: String): List<Citation> {
            if (json.isBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    Citation(
                        document = o.optString("document"),
                        page = o.optInt("page"),
                        categorie = o.optString("categorie")
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}
