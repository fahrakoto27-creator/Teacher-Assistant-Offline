package com.fahendrena.teacherassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahendrena.teacherassistant.data.SchoolClass
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

@Composable
fun ClassesScreen(viewModel: AppViewModel) {
    val classes by viewModel.classes.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes classes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Ajouter") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(classes) { c ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(c.nom, style = MaterialTheme.typography.titleMedium)
                            Text("${c.matiere} . ${c.niveau} . ${c.effectif} eleves", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.deleteClass(c) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddClassDialog(onDismiss = { showDialog = false }) { newClass ->
            viewModel.addClass(newClass)
            showDialog = false
        }
    }
}

@Composable
private fun AddClassDialog(onDismiss: () -> Unit, onConfirm: (SchoolClass) -> Unit) {
    var nom by remember { mutableStateOf("") }
    var niveau by remember { mutableStateOf("") }
    var matiere by remember { mutableStateOf("") }
    var effectif by remember { mutableStateOf("") }
    var annee by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle classe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nom, { nom = it }, label = { Text("Nom (ex : 6eme IV)") })
                OutlinedTextField(niveau, { niveau = it }, label = { Text("Niveau (ex : 6eme)") })
                OutlinedTextField(matiere, { matiere = it }, label = { Text("Matiere") })
                OutlinedTextField(effectif, { effectif = it.filter { c -> c.isDigit() } }, label = { Text("Effectif") })
                OutlinedTextField(annee, { annee = it }, label = { Text("Annee scolaire") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nom.isNotBlank()) {
                    onConfirm(
                        SchoolClass(
                            nom = nom, niveau = niveau, matiere = matiere,
                            effectif = effectif.toIntOrNull() ?: 0, anneeScolaire = annee
                        )
                    )
                }
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
