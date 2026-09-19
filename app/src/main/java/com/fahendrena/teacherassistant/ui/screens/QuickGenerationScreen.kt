package com.fahendrena.teacherassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fahendrena.teacherassistant.ui.navigation.Routes
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

/**
 * "Generation rapide" (cahier des charges, section 23) : l'enseignant
 * decrit une seance en une ligne, l'application cherche dans les
 * ressources deja importees (dans l'ordre programme > repartition >
 * fascicule > manuel > personnel) et prepare un brouillon de fiche,
 * toujours modifiable ensuite.
 */
@Composable
fun QuickGenerationScreen(viewModel: AppViewModel, navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var generating by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Generation rapide") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Decrivez la seance en une ligne, par exemple :",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "6eme English - Greetings - 2 heures",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Classe - theme - duree") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "L'application va chercher dans vos ressources deja importees " +
                    "(programme, repartition, fascicule, manuel...) et preparer un " +
                    "brouillon avec les sources citees. Rien n'est genere sans que " +
                    "vous puissiez le verifier et le modifier ensuite.",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        generating = true
                        viewModel.generateQuickPreparation(query) { id ->
                            generating = false
                            navController.navigate(Routes.prepForm(id)) {
                                popUpTo(Routes.PREP_LIST)
                            }
                        }
                    }
                },
                enabled = query.isNotBlank() && !generating,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (generating) "Generation en cours..." else "Generer la preparation") }
        }
    }
}
