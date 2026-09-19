package com.fahendrena.teacherassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahendrena.teacherassistant.data.TeacherProfile
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val profile by viewModel.profile.collectAsState(initial = null)

    var nom by remember(profile) { mutableStateOf(profile?.nom ?: "") }
    var etablissement by remember(profile) { mutableStateOf(profile?.etablissement ?: "") }
    var matiere by remember(profile) { mutableStateOf(profile?.matierePrincipale ?: "") }
    var annee by remember(profile) { mutableStateOf(profile?.anneeScolaire ?: "") }
    var langue by remember(profile) { mutableStateOf(profile?.langueTravail ?: "Francais") }
    var duree by remember(profile) { mutableStateOf((profile?.dureeHabituelle ?: 60).toString()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Mon profil") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(nom, { nom = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(etablissement, { etablissement = it }, label = { Text("Etablissement") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(matiere, { matiere = it }, label = { Text("Matiere principale") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(annee, { annee = it }, label = { Text("Annee scolaire") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(langue, { langue = it }, label = { Text("Langue de travail") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                duree, { duree = it.filter { c -> c.isDigit() } },
                label = { Text("Duree habituelle d'une seance (min)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    viewModel.saveProfile(
                        TeacherProfile(
                            nom = nom, etablissement = etablissement, matierePrincipale = matiere,
                            anneeScolaire = annee, langueTravail = langue,
                            dureeHabituelle = duree.toIntOrNull() ?: 60
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enregistrer") }
        }
    }
}
