package com.nolly.surecharge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.presentation.ChecklistState

@Composable
fun ReliabilityItem(
	title: String,
	description: String,
	label: String,
	state: ChecklistState,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val (containerColor, labelColor) = when (state) {
		ChecklistState.Ok -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
		ChecklistState.Warning -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.error
		ChecklistState.Pending -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
		ChecklistState.Info -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.outline
	}

	Card(
		onClick = onClick,
		modifier = modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
				)
				AssistChip(
					onClick = onClick,
					label = { Text(label) },
					colors = AssistChipDefaults.assistChipColors(
						containerColor = containerColor,
						labelColor = labelColor
					)
				)
			}
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
	}
}
