package com.olafon.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.olafon.core.designsystem.R

@OptIn(ExperimentalTextApi::class)
private fun sora(weight: FontWeight) =
    Font(R.font.sora, weight = weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

/** Sora (OFL), shipped as one variable font; see OFL-Sora.txt in this module. */
val Sora: FontFamily = FontFamily(
    sora(FontWeight.Normal),
    sora(FontWeight.Medium),
    sora(FontWeight.SemiBold),
    sora(FontWeight.Bold),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        lineHeight = 96.sp,
        letterSpacing = (-4).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 58.sp,
        lineHeight = 60.sp,
        letterSpacing = (-2).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 46.sp,
        letterSpacing = (-2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sora, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(
        fontFamily = Sora,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    ),
)
