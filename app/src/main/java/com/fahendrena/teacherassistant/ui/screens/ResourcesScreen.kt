package com.fahendrena.teacherassistant.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahendrena.teacherassistant.data.ResourceCategory
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

@Composable
fun ResourcesScreen(viewModel: AppViewModel) {
    val resources by viewModel.resources.collectAsState(initial = emptyList())
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var showMetaDialog by remember { mutableStateOf(false) }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingUri = uri
            showMetaDialog = true
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes ressources") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                pickFile.launch(
                    arrayOf(
                        "application/pdf",
                        "text/plain",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
            }) { Icon(Icons.Default.Add, contentDescription = "Importer") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(resources) { r ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(r.titre, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${r.categorie} . ${r.niveau} . ${r.matiere} . .${r.typeFichier}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { viewModel.deleteResource(r) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            }
        }
    }

    if (showMetaDialog && pendingUri != null) {
        ImportMetaDialog(
            onDismiss = { showMetaDialog = false; pendingUri = null },
            onConfirm = { titre, categorie, niveau, matiere ->
                viewModel.importResource(pendingUri!!, titre, categorie, niveau, matiere)
                showMetaDialog = false
                pendingUri = null
            }
        )
    }
}

@Composable
private fun ImportMetaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ResourceCategory, String, String) -> Unit
) {
    var titre by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf(ResourceCategory.PERSONNEL) }
    var niveau by remember { mutableStateOf("") }
    var matiere by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importer un document") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(titre, { titre = it }, label = { Text("Titre") })
                Text("Categorie", style = MaterialTheme.typography.labelMedium)
                ResourceCategory.entries.forEach { cat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = categorie == cat, onClick = { categorie = cat })
                        Text(cat.name)
                    }
                }
                OutlinedTextField(niveau, { niveau = it }, label = { Text("Niveau (ex : 6eme)") })
                OutlinedTextField(matiere, { matiere = it }, label = { Text("Matiere") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (titre.isNotBlank()) onConfirm(titre, categorie, niveau, matiere)
            }) { Text("Importer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
