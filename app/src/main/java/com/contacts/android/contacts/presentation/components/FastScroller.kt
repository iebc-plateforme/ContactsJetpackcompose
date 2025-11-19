package com.contacts.android.contacts.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun FastScroller(
    listState: LazyListState,
    sections: List<Char>,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf<Char?>(null) }
    val density = LocalDensity.current

    // Auto-hide when not scrolling
    val isScrolling = listState.isScrollInProgress
    val showScroller by remember {
        derivedStateOf { isScrolling || isDragging }
    }

    Box(
        modifier = modifier.fillMaxHeight()
    ) {
        // Fast scroller track
        AnimatedVisibility(
            visible = showScroller || sections.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp)
                    .padding(end = 8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                selectedSection = null
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val y = change.position.y
                                val sectionHeight = size.height / sections.size.toFloat()
                                val index = (y / sectionHeight).toInt().coerceIn(0, sections.size - 1)
                                selectedSection = sections[index]
                                scope.launch {
                                    onSectionSelected(index)
                                }
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    sections.forEach { section ->
                        Text(
                            text = section.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Section indicator bubble
        if (isDragging && selectedSection != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-60).dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedSection.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberFastScrollerState(
    sections: Map<Char, List<*>>
): FastScrollerState {
    return remember(sections) {
        FastScrollerState(sections)
    }
}

class FastScrollerState(
    val sections: Map<Char, List<*>>
) {
    val sectionKeys: List<Char> = sections.keys.sorted()

    fun getIndexForSection(sectionIndex: Int): Int {
        if (sectionIndex < 0 || sectionIndex >= sectionKeys.size) return 0

        val targetSection = sectionKeys[sectionIndex]
        var index = 0

        for (section in sectionKeys) {
            if (section == targetSection) break
            index += sections[section]?.size ?: 0
        }

        return index
    }
}
