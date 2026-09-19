package com.fahendrena.teacherassistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fahendrena.teacherassistant.ui.screens.ClassesScreen
import com.fahendrena.teacherassistant.ui.screens.HomeScreen
import com.fahendrena.teacherassistant.ui.screens.PreparationDetailScreen
import com.fahendrena.teacherassistant.ui.screens.PreparationFormScreen
import com.fahendrena.teacherassistant.ui.screens.PreparationListScreen
import com.fahendrena.teacherassistant.ui.screens.ProfileScreen
import com.fahendrena.teacherassistant.ui.screens.QuickGenerationScreen
import com.fahendrena.teacherassistant.ui.screens.ResourcesScreen
import com.fahendrena.teacherassistant.ui.screens.SearchScreen
import com.fahendrena.teacherassistant.viewmodel.AppViewModel

object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val CLASSES = "classes"
    const val RESOURCES = "resources"
    const val SEARCH = "search"
    const val QUICK_GEN = "quick_generation"
    const val PREP_LIST = "preparations"
    const val PREP_FORM = "preparation_form?id={id}"
    const val PREP_DETAIL = "preparation_detail/{id}"

    fun prepForm(id: Long? = null) = "preparation_form?id=${id ?: -1}"
    fun prepDetail(id: Long) = "preparation_detail/$id"
}

@Composable
fun TaoNavGraph(navController: NavHostController = rememberNavController()) {
    val viewModel: AppViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(viewModel) }
        composable(Routes.CLASSES) { ClassesScreen(viewModel) }
        composable(Routes.RESOURCES) { ResourcesScreen(viewModel) }
        composable(Routes.SEARCH) { SearchScreen(viewModel) }
        composable(Routes.QUICK_GEN) { QuickGenerationScreen(viewModel, navController) }
        composable(Routes.PREP_LIST) { PreparationListScreen(viewModel, navController) }
        composable(
            route = Routes.PREP_FORM,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            PreparationFormScreen(viewModel, navController, if (id == -1L) null else id)
        }
        composable(
            route = Routes.PREP_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            PreparationDetailScreen(viewModel, id)
        }
    }
}
