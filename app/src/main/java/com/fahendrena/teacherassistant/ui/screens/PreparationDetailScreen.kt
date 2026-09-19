package com.fahendrena.teacherassistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fahendrena.teacherassistant.generation.Citation
import com.fahendrena.teacherassistant.pdf.PreparationPdfExporter
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

@Composable
fun PreparationDetailScreen(viewModel: AppViewModel, preparationId: Long) {
    val context = LocalContext.current
    val preparationFlow = remember(preparationId) { viewModel.preparationById(preparationId) }
    val preparation by preparationFlow.collectAsState(initial = null)
    val profile by viewModel.profile.collectAsState(initial = null)
    val classes by viewModel.classes.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text(preparation?.titreLecon ?: "Preparation") }) }) { padding ->
        val p = preparation
        if (p == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailBlock(
                    "Informations generales",
                    "Matiere : ${p.matiere}\nNiveau : ${p.niveau}\nDate : ${p.date}\nDuree : ${p.dureeMinutes} min"
                )
                DetailBlock("Objectif general", p.objectifGeneral)
                DetailBlock("Objectifs specifiques", p.objectifsSpecifiques)
                DetailBlock("Competences", p.competences)
                DetailBlock("Deroulement", p.deroulementJson)
                DetailBlock("Contenu de la lecon", p.contenuLecon)
                DetailBlock("Vocabulaire", p.vocabulaireJson)
                DetailBlock("Grammaire", p.grammaire)
                DetailBlock("Activites", p.activites)
                DetailBlock("Exercices", p.exercices)
                DetailBlock("Corrige", p.corrige)
                DetailBlock("Evaluation", p.evaluation)
                DetailBlock("Devoir", p.devoir)

                val citations = remember(p.sourcesJson) { Citation.listFromJson(p.sourcesJson) }
                if (citations.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Sources", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            citations.forEach { c ->
                                Text(
                                    "Source : ${c.document} — page ${c.page} (${c.categorie})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val uri = PreparationPdfExporter.export(
                            context, p, profile, classes.find { it.id == p.classeId }
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Exporter en PDF") }
            }
        }
    }
}

@Composable
private fun DetailBlock(title: String, content: String) {
    if (content.isBlank()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
