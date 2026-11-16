package com.olafon.feature.challenges.question

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.challenge.QuestionChallenge
import com.olafon.feature.challenges.R

/** Screen 5 of the canvas: the question under a lime bar, the choices on a two-column grid. */
@Composable
fun QuestionChallengeScreen(
    challenge: QuestionChallenge,
    lastAnswerWrong: Boolean,
    onChoice: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(OlafonColors.Lime),
        )
        Spacer(Modifier.height(16.dp))
        Text(challenge.prompt, style = MaterialTheme.typography.headlineMedium)
        if (lastAnswerWrong) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.question_wrong),
                style = MaterialTheme.typography.bodySmall,
                color = OlafonColors.Error,
            )
        }
        Spacer(Modifier.weight(1f))
        challenge.choices.chunked(2).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { column, choice ->
                    val index = rowIndex * 2 + column
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 72.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(OlafonColors.SurfaceHigh)
                            .clickable(onClick = { onChoice(index) }, role = Role.RadioButton)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            choice,
                            style = MaterialTheme.typography.titleLarge,
                            color = OlafonColors.OnSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
