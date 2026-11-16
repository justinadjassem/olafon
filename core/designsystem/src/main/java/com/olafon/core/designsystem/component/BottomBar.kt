package com.olafon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.R
import com.olafon.core.designsystem.theme.OlafonColors

/** The floating pill bar of the canvas: two tabs, the lime « + », two tabs. */
@Composable
fun BottomBar(leading: List<BottomTab>, trailing: List<BottomTab>, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(CircleShape)
            .background(OlafonColors.SurfaceHigh),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading.forEach { Tab(it, Modifier.weight(1f)) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(OlafonColors.Lime)
                    .clickable(onClick = onAdd, role = Role.Button),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "Nouvelle alarme",
                    tint = OlafonColors.OnLime,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        trailing.forEach { Tab(it, Modifier.weight(1f)) }
    }
}

@Composable
private fun Tab(tab: BottomTab, modifier: Modifier = Modifier) {
    val tint = when {
        tab.selected -> OlafonColors.Lime
        tab.enabled -> OlafonColors.Muted
        else -> OlafonColors.Dim
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = tab.enabled, onClick = tab.onClick, role = Role.Tab)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Icon(painterResource(tab.icon), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
