package com.fahendrena.teacherassistant.generation

import com.fahendrena.teacherassistant.data.AppDatabase
import com.fahendrena.teacherassistant.data.ResourceCategory
import com.fahendrena.teacherassistant.data.ResourceDocument

/** Resultat d'une generation : ce qui a ete trouve dans les ressources, avec ses sources. */
data class GeneratedDraft(
    val objectifGeneral: String,
    val contenuLecon: String,
    val citations: List<Citation>,
    val informationTrouvee: Boolean
)

private data class PageHit(val resource: ResourceDocument, val pageNumber: Int, val extrait: String, val score: Int)

/**
 * Recherche dans les ressources importees, en respectant la hierarchie des
 * sources du cahier des charges : Programme > Repartition > Fascicule >
 * Manuel > Documents personnels. Ne genere jamais d'information : ne fait
 * que retrouver et citer ce qui existe deja dans les documents de
 * l'enseignant (section 10 et 26 du cahier des charges : distinguer
 * information extraite et proposition generee).
 */
class LessonContentGenerator(private val db: AppDatabase) {

    // Ordre de l'enum ResourceCategory = ordre de priorite voulu (section 10).
    private val hierarchie = listOf(
        ResourceCategory.PROGRAMME,
        ResourceCategory.REPARTITION,
        ResourceCategory.FASCICULE,
        ResourceCategory.MANUEL,
        ResourceCategory.PERSONNEL
    )

    suspend fun generate(niveau: String, matiere: String, theme: String, maxExtraits: Int = 4): GeneratedDraft {
        val motsClefs = theme
            .split(Regex("[^\\p{L}0-9]+"))
            .map { it.trim() }
            .filter { it.length > 2 }
            .distinct()

        if (motsClefs.isEmpty()) {
            return GeneratedDraft("", "", emptyList(), false)
        }

        val hits = mutableListOf<PageHit>()

        for (categorie in hierarchie) {
            val resources = db.resourceDao().listByCategory(categorie)
                .filter { correspond(it.niveau, niveau) && correspond(it.matiere, matiere) }

            for (resource in resources) {
                val pages = db.resourcePageDao().pagesForResource(resource.id)
                for (page in pages) {
                    val score = scorePage(page.texte, motsClefs)
                    if (score > 0) {
                        hits += PageHit(resource, page.pageNumber, extraitAutour(page.texte, motsClefs), score)
                    }
                }
            }
            // Si des correspondances ont deja ete trouvees a un niveau de la
            // hierarchie, on ne descend pas plus bas — sauf s'il n'y en a pas.
            if (hits.isNotEmpty()) break
        }

        val meilleurs = hits.sortedByDescending { it.score }.take(maxExtraits)

        if (meilleurs.isEmpty()) {
            return GeneratedDraft("", "", emptyList(), false)
        }

        val citations = meilleurs.map { Citation(it.resource.titre, it.pageNumber, it.resource.categorie.name) }

        val contenu = meilleurs.joinToString("\n\n") { hit ->
            "[Extrait de ${hit.resource.titre} — page ${hit.pageNumber}]\n${hit.extrait}"
        }

        // Le premier extrait de la source la plus prioritaire sert de suggestion
        // pour l'objectif general — a valider/reformuler par l'enseignant.
        val objectif = meilleurs.first().extrait.take(200)

        return GeneratedDraft(
            objectifGeneral = objectif,
            contenuLecon = contenu,
            citations = citations,
            informationTrouvee = true
        )
    }

    private fun correspond(valeurRessource: String, valeurRecherchee: String): Boolean {
        if (valeurRessource.isBlank() || valeurRecherchee.isBlank()) return true
        return valeurRessource.contains(valeurRecherchee, ignoreCase = true) ||
            valeurRecherchee.contains(valeurRessource, ignoreCase = true)
    }

    private fun scorePage(texte: String, motsClefs: List<String>): Int =
        motsClefs.sumOf { mot ->
            Regex(Regex.escape(mot), RegexOption.IGNORE_CASE).findAll(texte).count()
        }

    private fun extraitAutour(texte: String, motsClefs: List<String>, contexte: Int = 250): String {
        val index = motsClefs.firstNotNullOfOrNull { mot ->
            val i = texte.indexOf(mot, ignoreCase = true)
            if (i >= 0) i else null
        } ?: 0
        val start = (index - contexte / 2).coerceAtLeast(0)
        val end = (index + contexte).coerceAtMost(texte.length)
        val extrait = texte.substring(start, end).trim().replace(Regex("\\s+"), " ")
        return if (start > 0) "…$extrait…" else "$extrait…"
    }
}
