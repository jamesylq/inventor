package com.example.inventor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FadedLazyColumn(
    modifier: Modifier = Modifier,
    fadeHeight: Dp = 20.dp,
    topFade: Boolean = true,
    bottomFade: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: LazyListScope.() -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            content = content,
            verticalArrangement = verticalArrangement
        )

        if (topFade) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(backgroundColor, Color.Transparent)
                        )
                    )
                    .align(Alignment.TopCenter)
            )
        }

        if (bottomFade) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, backgroundColor)
                        )
                    )
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
