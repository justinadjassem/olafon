package com.olafon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors

/** The big rounded surface of the canvas: 32 dp corners, 20/22 dp padding. */
@Composable
fun OlafonCard(
    modifier: Modifier = Modifier,
    color: Color = OlafonColors.Surface,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color)
            .padding(contentPadding),
        content = content,
    )
}

/** A card whose bottom edge runs off the screen, like the alarm list of the home screen. */
val TopRoundedShape: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
