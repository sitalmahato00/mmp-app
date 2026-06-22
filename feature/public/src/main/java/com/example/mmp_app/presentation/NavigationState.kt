package com.example.mmp_app.presentation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.*

@Composable
fun rememberNavigationState(
    startRoute: Routes,
    topLevelRoutes: Set<Routes>
): NavigationState {
    val topLevelRoute = rememberSaveable {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key -> 
        val list = remember { mutableStateListOf<Routes>() }
        if (list.isEmpty()) list.add(key)
        list
    }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks
        )
    }
}

class NavigationState(
    val startRoute: Routes,
    topLevelRoute: MutableState<Routes>,
    val backStacks: Map<Routes, SnapshotStateList<Routes>>
) {
    var topLevelRoute: Routes by topLevelRoute
}

class Navigator(val state: NavigationState) {
    fun navigate(route: Routes) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun replace(route: Routes) {
        val stack = state.backStacks[state.topLevelRoute] ?: return
        stack.clear()
        stack.add(route)
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        if (currentStack.size <= 1) {
            if (state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
            }
        } else {
            currentStack.removeAt(currentStack.size - 1)
        }
    }
}
