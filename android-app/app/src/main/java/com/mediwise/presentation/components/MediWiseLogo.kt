package com.mediwise.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediwise.presentation.theme.BrandBlue
import com.mediwise.presentation.theme.SubtitleGray

/**
 * Custom modern medical emblem for MediWise matching the reference mockup.
 * Composed of four rounded petals/leaf elements forming a medical cross/clover.
 */
@Composable
fun MediWiseEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    tint: Color = BrandBlue
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val centerX = w / 2f
        val centerY = h / 2f

        // Draw 4 rounded leaf petals forming the cross clover
        val petalWidth = w * 0.32f
        val petalHeight = h * 0.38f

        for (angle in listOf(0f, 90f, 180f, 270f)) {
            rotate(degrees = angle, pivot = Offset(centerX, centerY)) {
                val path = Path().apply {
                    // Start from center
                    moveTo(centerX - petalWidth * 0.45f, centerY - petalHeight * 0.15f)
                    // Curve outward and up to form smooth rounded top of petal
                    cubicTo(
                        centerX - petalWidth * 0.7f, centerY - petalHeight * 0.65f,
                        centerX - petalWidth * 0.35f, centerY - petalHeight * 1.05f,
                        centerX, centerY - petalHeight * 1.05f
                    )
                    cubicTo(
                        centerX + petalWidth * 0.35f, centerY - petalHeight * 1.05f,
                        centerX + petalWidth * 0.7f, centerY - petalHeight * 0.65f,
                        centerX + petalWidth * 0.45f, centerY - petalHeight * 0.15f
                    )
                    close()
                }
                drawPath(path = path, color = tint)
            }
        }

        // Center circular core junction
        drawCircle(
            color = tint,
            radius = w * 0.16f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * Complete MediWise brand logo with emblem, title, and tagline.
 */
@Composable
fun MediWiseLogo(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 88.dp,
    isWhiteTheme: Boolean = false,
    showText: Boolean = true,
    tagline: String = "Clinical Consultation Center"
) {
    val primaryColor = if (isWhiteTheme) Color.White else BrandBlue
    val secondaryColor = if (isWhiteTheme) Color.White.copy(alpha = 0.82f) else SubtitleGray

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MediWiseEmblem(
            size = emblemSize,
            tint = primaryColor
        )

        if (showText) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MediWise",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
            if (tagline.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tagline,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryColor,
                    letterSpacing = 0.2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
