package com.nolly.surecharge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.profile.BatteryProfile

@Composable
fun ProfilesCard(
	rules: BatteryRules,
	profiles: List<BatteryProfile>,
	onApplyProfile: (BatteryProfile) -> Unit,
	onCreateProfileClick: () -> Unit,
	onDeleteProfile: (Long) -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "Smart profiles",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)
			Text(
				text = "Quickly switch between different alert setups. Built-in presets are editable and removable like any other profile.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)

			if (profiles.isEmpty()) {
				Text(
					text = "No profiles yet. Save your current rules as a profile to get started.",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
			} else {
				val activeProfileId = profiles.firstOrNull { profileMatchesRules(it, rules) }?.id

				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					profiles.forEach { profile ->
						ProfileRow(
							profile = profile,
							isActive = profile.id == activeProfileId,
							onApply = { onApplyProfile(profile) },
							onDelete = { onDeleteProfile(profile.id) }
						)
					}
				}
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(onClick = onCreateProfileClick) {
					Text("Save current as profile")
				}
			}
		}
	}
}

private fun profileMatchesRules(profile: BatteryProfile, rules: BatteryRules): Boolean {
	return profile.lowLevelEnabled == rules.lowLevelEnabled &&
			profile.lowLevelPercentage == rules.lowLevelPercentage &&
			profile.highLevelEnabled == rules.highLevelEnabled &&
			profile.highLevelPercentage == rules.highLevelPercentage &&
			profile.repeatIntervalMinutes == rules.repeatIntervalMinutes
}

@Composable
private fun ProfileRow(
	profile: BatteryProfile,
	isActive: Boolean,
	onApply: () -> Unit,
	onDelete: () -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = profile.name,
				style = MaterialTheme.typography.bodyMedium.copy(
					fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
				)
			)
			val summary = buildString {
				if (profile.lowLevelEnabled) append("Low ${profile.lowLevelPercentage}%")
				if (profile.highLevelEnabled) {
					if (isNotEmpty()) append(" · ")
					append("High ${profile.highLevelPercentage}%")
				}
				profile.repeatIntervalMinutes?.let {
					if (isNotEmpty()) append(" · ")
					append("Repeat ${it}min")
				}
				if (isEmpty()) append("No alerts")
			}
			Text(
				text = summary,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)
		}
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			TextButton(onClick = onApply) {
				Text(if (isActive) "Active" else "Use")
			}
			TextButton(onClick = onDelete) {
				Text("Delete")
			}
		}
	}
}
