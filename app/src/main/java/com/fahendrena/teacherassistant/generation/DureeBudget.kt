package com.fahendrena.teacherassistant.generation

/**
 * Repartition automatique du temps d'une seance entre les differentes
 * etapes du deroulement (cahier des charges, section "gestion du temps").
 * Les ratios reprennent l'exemple donne pour une seance de 2 heures et
 * sont mis a l'echelle de la duree reellement choisie.
 */
object DureeBudget {

    private val ETAPES_BASE = listOf(
        "Accueil" to 5,
        "Rappel" to 10,
        "Presentation" to 15,
        "Explication" to 20,
        "Pratique guidee" to 20,
        "Travail en groupe" to 20,
        "Exercices" to 15,
        "Correction" to 10,
        "Evaluation" to 5,
        "Synthese" to 5,
        "Devoir" to 5
    )

    /**
     * Genere un deroulement propose (texte libre, une etape par ligne),
     * dont la duree totale correspond exactement a [dureeMinutes].
     * Il s'agit toujours d'une PROPOSITION a ajuster par l'enseignant.
     */
    fun genererDeroulement(dureeMinutes: Int): String {
        val totalBase = ETAPES_BASE.sumOf { it.second }
        val brut = ETAPES_BASE.map { (nom, minutes) ->
            nom to (minutes.toDouble() / totalBase * dureeMinutes)
        }
        // arrondi au multiple de 5 minutes le plus proche (minimum 5)
        val arrondi = brut.map { (nom, minutes) ->
            val v = (Math.round(minutes / 5.0) * 5).toInt()
            nom to v.coerceAtLeast(5)
        }
        // correction de l'arrondi pour que le total corresponde exactement
        val ecart = dureeMinutes - arrondi.sumOf { it.second }
        val corrige = arrondi.toMutableList()
        if (corrige.isNotEmpty() && ecart != 0) {
            val indexMax = corrige.indices.maxByOrNull { corrige[it].second } ?: 0
            val (nom, minutes) = corrige[indexMax]
            corrige[indexMax] = nom to (minutes + ecart).coerceAtLeast(1)
        }

        return buildString {
            append("(Proposition de deroulement, a ajuster selon la seance reelle)\n")
            corrige.forEach { (nom, minutes) ->
                append("$nom : $minutes min\n")
            }
        }.trim()
    }
}
