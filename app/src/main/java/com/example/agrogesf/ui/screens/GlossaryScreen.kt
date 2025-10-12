// ui/screens/GlossaryScreen.kt
package com.example.agrogesf.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.agrogesf.R
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.ui.theme.*
import com.example.agrogesf.ui.viewmodels.HomeViewModel
import java.io.File

@Composable
fun GlossaryScreen(
    pestType: PestType,
    onPestClick: (String) -> Unit,
    onNavigateToOtherType: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val pests by if (pestType == PestType.PRAGA) {
        viewModel.pragues.collectAsState()
    } else {
        viewModel.diseases.collectAsState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Header com título e ícone
        GlossaryHeader(pestType = pestType)

        // Grid de pragas/doenças
        if (pests.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pests) { pest ->
                    PestGridItem(
                        pest = pest,
                        onClick = { onPestClick(pest.id) }
                    )
                }
            }
        } else {
            EmptyGlossaryState(pestType = pestType)
        }

        // Botão para alternar entre Pragas e Doenças
        NavigationButton(
            pestType = pestType,
            onClick = onNavigateToOtherType
        )
    }
}

@Composable
private fun GlossaryHeader(pestType: PestType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGold)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(
                    id = if (pestType == PestType.PRAGA) R.drawable.ic_bug else R.drawable.ic_disease
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = if (pestType == PestType.PRAGA) "Praga" else "Doença",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PestGridItem(
    pest: Pest,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = if (isPressed) 12.dp else 6.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardPrague)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (pest.images.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(pest.images.first())),
                        contentDescription = pest.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder se não houver imagem
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GreenPrimary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_image_placeholder),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Nome
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardPrague)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pest.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun EmptyGlossaryState(pestType: PestType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (pestType == PestType.PRAGA) R.drawable.ic_bug else R.drawable.ic_disease
                ),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GreenPrimary.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nenhuma ${if (pestType == PestType.PRAGA) "praga" else "doença"} cadastrada",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conecte-se ao WiFi AGRO_GESF_DATA para receber informações",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NavigationButton(
    pestType: PestType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GreenDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (pestType == PestType.PRAGA) "Doenças" else "Pragas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = YellowAccent
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = "Navegar",
                tint = YellowAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}