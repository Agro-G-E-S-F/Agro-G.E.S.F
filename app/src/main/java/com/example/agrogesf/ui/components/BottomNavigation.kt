package com.example.agrogesf.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.agrogesf.ui.theme.YellowAccent

@Composable
fun CustomBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    items: List<BottomNavItem>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = YellowAccent,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavItem(
                    icon = item.icon,
                    isSelected = selectedIndex == index,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

data class BottomNavItem(
    val icon: ImageVector,
    val route: String
)