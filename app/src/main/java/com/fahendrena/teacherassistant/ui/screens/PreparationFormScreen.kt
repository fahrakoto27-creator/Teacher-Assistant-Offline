package com.fahendrena.teacherassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fahendrena.teacherassistant.data.Preparation
import com.fahendrena.teacherassistant.data.PreparationStatut
import com.fahendrena.teacherassistant.data.SchoolClass
import com.fahendrena.teacherassistant.ui.navigation.Routes
import com.fahendrena.teacherassistant.viewmodel.AppViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun PreparationFormScreen(viewModel: AppViewModel, navController: NavHostController, preparationId: Long?) {
    val classes by viewModel.classes.collectAsState(initial = emptyList())
    val existingFlow = remember(preparationId) {
        preparationId?.let { viewModel.preparationById(it) } ?: flowOf(null)
    }
    val existing by existingFlow.collectAsState(initial = null)

    var selectedClass by remember { mutableStateOf<SchoolClass?>(null) }
    var matiere by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf("") }
    var titreLecon by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var duree by remember { mutableStateOf("60") }
    var numeroSeance by remember { mutableStateOf("1") }
    var objectifGeneral by remember { mutableStateOf("") }
    var objectifsSpecifiques by remember { mutableStateOf("") }
    var competences by remember { mutableStateOf("") }
    var prerequis by remember { mutableStateOf("") }
    var materiel by remember { mutableStateOf("") }
    var methode by remember { mutableStateOf("") }
    var deroulement by remember { mutableStateOf("") }
    var contenuLecon by remember { mutableStateOf("") }
    var vocabulaire by remember { mutableStateOf("") }
    var grammaire by remember { mutableStateOf("") }
    var activites by remember { mutableStateOf("") }
    var exercices by remember { mutableStateOf("") }
    var corrige by remember { mutableStateOf("") }
    var evaluation by remember { mutableStateOf("") }
    var devoir by remember { mutableStateOf("") }

    LaunchedEffect(existing, classes) {
        val p = existing ?: return@LaunchedEffect
        selectedClass = classes.find { it.id == p.classeId }
        matiere = p.matiere; theme = p.theme; titreLecon = p.titreLecon
        date = p.date; duree = p.dureeMinutes.toString(); numeroSeance = p.numeroSeance.toString()
        objectifGeneral = p.objectifGeneral; objectifsSpecifiques = p.objectifsSpecifiques
        competences = p.competences; prerequis = p.prerequis; materiel = p.materiel
        methode = p.methodePedagogique; deroulement = p.deroulementJson
        contenuLecon = p.contenuLecon; vocabulaire = p.vocabulaireJson; grammaire = p.grammaire
        activites = p.activites; exercices = p.exercices; corrige = p.corrige
        evaluation = p.evaluation; devoir = p.devoir
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (preparationId == null) "Nouvelle preparation" else "Modifier la preparation") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Classe", style = MaterialTheme.typography.labelLarge)
            Column {
                classes.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedClass?.id == c.id,
                            onClick = { selectedClass = c; matiere = c.matiere }
                        )
                        Text("${c.nom} (${c.matiere})")
                    }
                }
            }

            SectionField("Theme", theme) { theme = it }
            SectionField("Titre de la lecon", titreLecon) { titreLecon = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(date, { date = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
                OutlinedTextField(duree, { duree = it.filter { c -> c.isDigit() } }, label = { Text("Duree (min)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(numeroSeance, { numeroSeance = it.filter { c -> c.isDigit() } }, label = { Text("Seance n.") }, modifier = Modifier.weight(1f))
            }

            SectionDivider("Objectifs et competences")
            SectionField("Objectif general", objectifGeneral) { objectifGeneral = it }
            SectionField("Objectifs specifiques (un par ligne)", objectifsSpecifiques, minLines = 3) { objectifsSpecifiques = it }
            SectionField("Competences", competences, minLines = 2) { competences = it }
            SectionField("Prerequis", prerequis) { prerequis = it }
            SectionField("Materiel", materiel) { materiel = it }
            SectionField("Methode pedagogique", methode) { methode = it }

            SectionDivider("Deroulement de la seance")
            SectionField("Deroulement (une etape par ligne : etape - duree - activites)", deroulement, minLines = 5) { deroulement = it }

            SectionDivider("Contenu de la lecon")
            SectionField("Contenu de la lecon", contenuLecon, minLines = 4) { contenuLecon = it }
            SectionField("Vocabulaire (mot : sens : exemple, un par ligne)", vocabulaire, minLines = 3) { vocabulaire = it }
            SectionField("Grammaire", grammaire, minLines = 2) { grammaire = it }

            SectionDivider("Activites, exercices, evaluation")
            SectionField("Activites", activites, minLines = 3) { activites = it }
            SectionField("Exercices", exercices, minLines = 3) { exercices = it }
            SectionField("Corrige", corrige, minLines = 3) { corrige = it }
            SectionField("Evaluation", evaluation, minLines = 3) { evaluation = it }
            SectionField("Devoir", devoir, minLines = 2) { devoir = it }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val prep = Preparation(
                        id = preparationId ?: 0L,
                        classeId = selectedClass?.id ?: 0L,
                        matiere = matiere,
                        niveau = selectedClass?.niveau ?: "",
                        theme = theme,
                        titreLecon = titreLecon,
                        date = date,
                        dureeMinutes = duree.toIntOrNull() ?: 60,
                        numeroSeance = numeroSeance.toIntOrNull() ?: 1,
                        objectifGeneral = objectifGeneral,
                        objectifsSpecifiques = objectifsSpecifiques,
                        competences = competences,
                        prerequis = prerequis,
                        materiel = materiel,
                        methodePedagogique = methode,
                        deroulementJson = deroulement,
                        contenuLecon = contenuLecon,
                        vocabulaireJson = vocabulaire,
                        grammaire = grammaire,
                        activites = activites,
                        exercices = exercices,
                        corrige = corrige,
                        evaluation = evaluation,
                        devoir = devoir,
                        statut = existing?.statut ?: PreparationStatut.EN_PREPARATION,
                        dateCreation = existing?.dateCreation ?: System.currentTimeMillis(),
                        dateModification = System.currentTimeMillis()
                    )
                    viewModel.savePreparation(prep) { id ->
                        navController.navigate(Routes.prepDetail(id)) {
                            popUpTo(Routes.PREP_LIST)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = titreLecon.isNotBlank() && selectedClass != null
            ) { Text("Generer la preparation") }
        }
    }
}

@Composable
private fun SectionField(label: String, value: String, minLines: Int = 1, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        minLines = minLines,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SectionDivider(title: String) {
    Spacer(Modifier.height(4.dp))
    Text(title, style = MaterialTheme.typography.titleSmall)
    HorizontalDivider()
}
