package com.fahendrena.teacherassistant.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahendrena.teacherassistant.data.AppDatabase
import com.fahendrena.teacherassistant.data.AppRepository
import com.fahendrena.teacherassistant.data.Preparation
import com.fahendrena.teacherassistant.data.ResourceCategory
import com.fahendrena.teacherassistant.data.ResourceDocument
import com.fahendrena.teacherassistant.data.SchoolClass
import com.fahendrena.teacherassistant.data.TeacherProfile
import com.fahendrena.teacherassistant.generation.Citation
import com.fahendrena.teacherassistant.generation.DureeBudget
import com.fahendrena.teacherassistant.generation.LessonContentGenerator
import com.fahendrena.teacherassistant.search.SearchEngine
import com.fahendrena.teacherassistant.search.SearchResult
import com.fahendrena.teacherassistant.seed.AssetSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(application, db)
    private val searchEngine = SearchEngine(repository)
    private val contentGenerator = LessonContentGenerator(db)

    init {
        viewModelScope.launch { AssetSeeder.seedIfNeeded(application, repository) }
    }

    val profile = repository.profile
    val classes = repository.classes
    val resources = repository.resources
    val preparations = repository.preparations

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    fun saveProfile(profile: TeacherProfile) = viewModelScope.launch {
        repository.saveProfile(profile)
    }

    fun addClass(c: SchoolClass) = viewModelScope.launch { repository.addClass(c) }
    fun deleteClass(c: SchoolClass) = viewModelScope.launch { repository.deleteClass(c) }

    fun preparationById(id: Long) = repository.preparationById(id)

    fun importResource(uri: Uri, titre: String, categorie: ResourceCategory, niveau: String, matiere: String) =
        viewModelScope.launch { repository.importResource(uri, titre, categorie, niveau, matiere) }

    fun deleteResource(r: ResourceDocument) = viewModelScope.launch { repository.deleteResource(r) }

    fun savePreparation(p: Preparation, onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repository.savePreparation(p)
        onSaved(id)
    }

    fun deletePreparation(p: Preparation) = viewModelScope.launch { repository.deletePreparation(p) }

    fun search(query: String) = viewModelScope.launch {
        _searchResults.value = searchEngine.search(query)
    }

    /**
     * "Generation rapide" (cahier des charges, section 23) : l'enseignant
     * ecrit une ligne du type "6eme English - Greetings - 2 heures" ; on
     * identifie la classe, on interroge la hierarchie des sources deja
     * importees, et on cree un brouillon de fiche pre-rempli (avec ses
     * sources citees) que l'enseignant pourra completer et corriger dans le
     * formulaire habituel — rien n'est jamais considere comme definitif.
     */
    fun generateQuickPreparation(query: String, onCreated: (Long) -> Unit) = viewModelScope.launch {
        val classesActuelles = classes.first()
        val parsed = QuickQueryParser.parse(query, classesActuelles)

        val draft = contentGenerator.generate(
            niveau = parsed.niveau,
            matiere = parsed.matiere,
            theme = parsed.theme
        )

        val deroulement = DureeBudget.genererDeroulement(parsed.dureeMinutes)
        val contenuLecon = if (draft.informationTrouvee) {
            draft.contenuLecon
        } else {
            "Information non trouvee dans les ressources importees.\n" +
                "(Proposition generee — a completer par l'enseignant, aucune source disponible pour \"${parsed.theme}\")"
        }
        val objectif = draft.objectifGeneral

        val prep = Preparation(
            classeId = parsed.classe?.id ?: 0L,
            matiere = parsed.matiere,
            niveau = parsed.niveau,
            theme = parsed.theme,
            titreLecon = parsed.theme,
            dureeMinutes = parsed.dureeMinutes,
            objectifGeneral = objectif,
            contenuLecon = contenuLecon,
            deroulementJson = deroulement,
            sourcesJson = Citation.listToJson(draft.citations),
            statut = com.fahendrena.teacherassistant.data.PreparationStatut.EN_PREPARATION
        )
        val id = repository.savePreparation(prep)
        onCreated(id)
    }
}

private data class QuickQuery(
    val classe: com.fahendrena.teacherassistant.data.SchoolClass?,
    val niveau: String,
    val matiere: String,
    val theme: String,
    val dureeMinutes: Int
)

private object QuickQueryParser {
    fun parse(input: String, classes: List<com.fahendrena.teacherassistant.data.SchoolClass>): QuickQuery {
        val parts = input
            .replace("--", "|")
            .replace("–", "|")
            .replace(Regex("\\s-\\s"), "|")
            .split("|")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val tete = parts.getOrNull(0) ?: input
        val theme = parts.getOrNull(1) ?: input
        val dureeTexte = parts.getOrNull(2) ?: ""

        val classe = classes.maxByOrNull { c ->
            var score = 0
            if (tete.contains(c.niveau, ignoreCase = true)) score += 2
            if (tete.contains(c.matiere, ignoreCase = true)) score += 2
            score
        }?.takeIf { c -> tete.contains(c.niveau, ignoreCase = true) || tete.contains(c.matiere, ignoreCase = true) }

        val niveau = classe?.niveau ?: tete
        val matiere = classe?.matiere ?: tete

        val heures = Regex("(\\d+)\\s*h").find(dureeTexte)?.groupValues?.get(1)?.toIntOrNull()
        val minutesMatch = Regex("(\\d+)\\s*min").find(dureeTexte)?.groupValues?.get(1)?.toIntOrNull()
        val dureeMinutes = when {
            minutesMatch != null -> minutesMatch
            heures != null -> heures * 60
            else -> 60
        }

        return QuickQuery(classe, niveau, matiere, theme, dureeMinutes)
    }
}
