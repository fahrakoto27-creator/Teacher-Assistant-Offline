package com.fahendrena.teacherassistant.seed

import android.content.Context
import com.fahendrena.teacherassistant.data.AppRepository
import com.fahendrena.teacherassistant.data.Preparation
import com.fahendrena.teacherassistant.data.PreparationStatut
import com.fahendrena.teacherassistant.data.ResourceCategory
import com.fahendrena.teacherassistant.data.SchoolClass
import org.json.JSONArray

/**
 * Pre-remplit l'application, au tout premier lancement, avec :
 *  - les ressources officielles deja fournies par l'enseignant pour
 *    l'Anglais (Programme d'Etudes, Repartition Annuelle, Fascicule de
 *    Ressources Pedagogiques), pour les niveaux 6eme a 3eme ;
 *  - un jeu de fiches de preparation suivant l'ordre du programme : une
 *    fiche completement redigee par unite (avec ses sources citees) et
 *    une fiche "a faire" pour chaque autre unite, afin que l'enseignant
 *    ait d'emblee sa progression annuelle en Anglais.
 *
 * Ne s'execute qu'une seule fois (drapeau SharedPreferences) : si
 * l'enseignant supprime ensuite une ressource ou une fiche generee,
 * elle ne sera pas reimportee automatiquement.
 *
 * Volontairement absents de ce pre-remplissage : les manuels
 * Bescherelle et Bled (Hatier/Hachette), qui sont des ouvrages publies
 * sous droit d'auteur — l'enseignant peut toujours les importer
 * lui-meme via "Mes ressources" pour un usage personnel.
 */
object AssetSeeder {

    private const val PREFS = "tao_seed_prefs"
    private const val KEY_SEED_V1_DONE = "seed_v1_done"

    suspend fun seedIfNeeded(context: Context, repository: AppRepository) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEED_V1_DONE, false)) return

        val manifestJson = context.assets.open("seed/manifest.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val manifest = JSONArray(manifestJson)

        for (i in 0 until manifest.length()) {
            val entry = manifest.getJSONObject(i)
            repository.importFromAsset(
                assetPath = "seed/${entry.getString("file")}",
                titre = entry.getString("titre"),
                categorie = ResourceCategory.valueOf(entry.getString("categorie")),
                niveau = entry.getString("niveau"),
                matiere = entry.getString("matiere")
            )
        }

        val classesParNiveau = mapOf(
            "6ème" to ensureClass(repository, "6ème", "English"),
            "5ème" to ensureClass(repository, "5ème", "English"),
            "4ème" to ensureClass(repository, "4ème", "English"),
            "3ème" to ensureClass(repository, "3ème", "English")
        )

        val prepsJson = context.assets.open("seed/seed_preparations.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val preps = JSONArray(prepsJson)

        for (i in 0 until preps.length()) {
            val o = preps.getJSONObject(i)
            val niveau = o.getString("niveau")
            val prep = Preparation(
                classeId = classesParNiveau[niveau] ?: 0L,
                matiere = o.getString("matiere"),
                niveau = niveau,
                theme = o.getString("theme"),
                titreLecon = o.getString("titreLecon"),
                numeroSeance = o.getInt("numeroSeance"),
                dureeMinutes = o.getInt("dureeMinutes"),
                objectifGeneral = o.optString("objectifGeneral"),
                objectifsSpecifiques = o.optString("objectifsSpecifiques"),
                competences = o.optString("competences"),
                materiel = o.optString("materiel"),
                methodePedagogique = o.optString("methodePedagogique"),
                vocabulaireJson = o.optString("vocabulaireJson"),
                grammaire = o.optString("grammaire"),
                activites = o.optString("activites"),
                exercices = o.optString("exercices"),
                corrige = o.optString("corrige"),
                sourcesJson = o.optString("sourcesJson", "[]"),
                statut = PreparationStatut.valueOf(o.getString("statut"))
            )
            repository.savePreparation(prep)
        }

        prefs.edit().putBoolean(KEY_SEED_V1_DONE, true).apply()
    }

    /** Cree une classe par defaut pour le niveau (l'enseignant pourra la renommer ensuite). */
    private suspend fun ensureClass(repository: AppRepository, niveau: String, matiere: String): Long {
        return repository.addClass(
            SchoolClass(nom = "$niveau (English)", niveau = niveau, matiere = matiere)
        )
    }
}
