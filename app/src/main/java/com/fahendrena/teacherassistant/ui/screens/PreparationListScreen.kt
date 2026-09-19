package com.fahendrena.teacherassistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fahendrena.teacherassistant.ui.navigation.Routes
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

@Composable
fun PreparationListScreen(viewModel: AppViewModel, navController: NavHostController) {
    val preparations by viewModel.preparations.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes preparations") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.prepForm()) }) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle preparation")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(preparations) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Routes.prepDetail(p.id)) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(p.titreLecon, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${p.matiere} . ${p.niveau} . ${p.theme} . ${p.statut}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
