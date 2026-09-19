package com.fahendrena.teacherassistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fahendrena.teacherassistant.ui.navigation.Routes

private data class MenuItem(val label: String, val emoji: String, val route: String)

@Composable
fun HomeScreen(navController: NavHostController) {
    val items = listOf(
        MenuItem("Nouvelle preparation", "\uD83D\uDCDD", Routes.prepForm()),
        MenuItem("Generation rapide", "\u26A1", Routes.QUICK_GEN),
        MenuItem("Mes ressources", "\uD83D\uDCDA", Routes.RESOURCES),
        MenuItem("Mes preparations", "\uD83D\uDCCB", Routes.PREP_LIST),
        MenuItem("Recherche locale", "\uD83D\uDD0D", Routes.SEARCH),
        MenuItem("Mes classes", "\uD83D\uDC68\u200D\uD83C\uDFEB", Routes.CLASSES),
        MenuItem("Mon profil", "\u2699\uFE0F", Routes.PROFILE)
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Teacher Assistant Offline") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(item.route) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(16.dp))
                        Text(item.label, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
