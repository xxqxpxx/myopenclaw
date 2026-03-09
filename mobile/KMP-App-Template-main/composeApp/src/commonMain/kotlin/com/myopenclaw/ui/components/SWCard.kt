package com.myopenclaw.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.Dimensions

@Composable
fun SWCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimensions.cardElevation
            ),
            shape = MaterialTheme.shapes.medium,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.cardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimensions.cardElevation
            ),
            shape = MaterialTheme.shapes.medium,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.cardPadding),
                content = content
            )
        }
    }
}

@Composable
fun SWPremiumBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFFFD700),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "PRO",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
