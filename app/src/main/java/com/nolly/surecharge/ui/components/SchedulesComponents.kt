package com.nolly.surecharge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nolly.surecharge.data.profile.BatteryProfile
import com.nolly.surecharge.data.schedule.Schedule
import com.nolly.surecharge.presentation.SchedulesUiState
import kotlin.math.roundToInt
import java.util.Locale

enum class Weekday(val label: String, val bitIndex: Int) {
	Sunday("Sun", 0),
	Monday("Mon", 1),
	Tuesday("Tue", 2),
	Wednesday("Wed", 3),
	Thursday("Thu", 4),
	Friday("Fri", 5),
	Saturday("Sat", 6)
}

fun allDaysMask(): Int = (1 shl 7) - 1

fun weekdaysMask(): Int =
	(1 shl Weekday.Monday.bitIndex) or
			(1 shl Weekday.Tuesday.bitIndex) or
			(1 shl Weekday.Wednesday.bitIndex) or
			(1 shl Weekday.Thursday.bitIndex) or
			(1 shl Weekday.Friday.bitIndex)

fun weekendMask(): Int =
	(1 shl Weekday.Sunday.bitIndex) or
			(1 shl Weekday.Saturday.bitIndex)

fun toggleDay(mask: Int, day: Weekday): Int {
	val bit = 1 shl day.bitIndex
	return if (mask and bit != 0) {
		mask and bit.inv()
	} else {
		mask or bit
	}
}

fun isDaySelected(mask: Int, day: Weekday): Boolean {
	val bit = 1 shl day.bitIndex
	return (mask and bit) != 0
}

fun daysMaskLabel(mask: Int): String {
	return when (mask) {
		allDaysMask() -> "Every day"
		weekdaysMask() -> "Weekdays"
		weekendMask() -> "Weekend"
		0 -> "No days selected"
		else -> {
			val selectedLabels = Weekday.entries
				.filter { isDaySelected(mask, it) }
				.joinToString(separator = ", ") { it.label }
			selectedLabels
		}
	}
}

fun minutesToTimeString(minutes: Int): String {
	val m = ((minutes % (24 * 60)) + 24 * 60) % (24 * 60)
	val h = m / 60
	val min = m % 60
	return String.format(Locale.getDefault(), "%02d:%02d", h, min)
}

fun createDefaultSchedule(): Schedule {
	return Schedule(
		id = 0,
		name = "",
		enabled = true,
		daysMask = weekdaysMask(),
		startMinutes = 22 * 60,
		endMinutes = 7 * 60,
		useProfileId = null,
		overrideLowEnabled = null,
		overrideLowPercent = null,
		overrideHighEnabled = null,
		overrideHighPercent = null,
		overrideRepeatMinutes = null,
		priority = 0
	)
}

@Composable
fun SchedulesCard(
	schedulesState: SchedulesUiState,
	onAddSchedule: () -> Unit,
	onEditSchedule: (Schedule) -> Unit,
	onDeleteSchedule: (Long) -> Unit,
	onToggleScheduleEnabled: (Long, Boolean) -> Unit
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
				text = "Schedules",
				style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
			)
			Text(
				text = "Change how alerts behave depending on the time and day. Night schedules, weekend behavior, and more.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
			)

			if (schedulesState.schedules.isEmpty()) {
				Text(
					text = "No schedules yet. Create one to use a different profile or custom thresholds in a time window.",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
			} else {
				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					schedulesState.schedules.forEach { schedule ->
						ScheduleRow(
							schedule = schedule,
							onToggleEnabled = { enabled ->
								onToggleScheduleEnabled(schedule.id, enabled)
							},
							onEdit = { onEditSchedule(schedule) },
							onDelete = { onDeleteSchedule(schedule.id) }
						)
					}
				}
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				TextButton(onClick = onAddSchedule) {
					Text("Add schedule")
				}
			}
		}
	}
}

@Composable
private fun ScheduleRow(
	schedule: Schedule,
	onToggleEnabled: (Boolean) -> Unit,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
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
					text = schedule.name.ifBlank { "Unnamed schedule" },
					style = MaterialTheme.typography.bodyMedium.copy(
						fontWeight = if (schedule.enabled) FontWeight.SemiBold else FontWeight.Medium
					)
				)
				val timeLabel =
					"${minutesToTimeString(schedule.startMinutes)} – ${minutesToTimeString(schedule.endMinutes)}"
				val daysLabel = daysMaskLabel(schedule.daysMask)
				val mode = if (schedule.useProfileId != null) {
					"Uses profile"
				} else {
					"Custom rules"
				}
				Text(
					text = "$daysLabel · $timeLabel · $mode",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
			}
			Switch(
				checked = schedule.enabled,
				onCheckedChange = onToggleEnabled
			)
		}
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			TextButton(onClick = onEdit) {
				Text("Edit")
			}
			TextButton(onClick = onDelete) {
				Text("Delete")
			}
		}
	}
}

@Composable
fun ScheduleEditorDialog(
	initialSchedule: Schedule,
	profiles: List<BatteryProfile>,
	onDismiss: () -> Unit,
	onSave: (Schedule) -> Unit
) {
	var working by remember(initialSchedule) { mutableStateOf(initialSchedule) }

	var isProfileMode by remember(initialSchedule) {
		mutableStateOf(initialSchedule.useProfileId != null)
	}

	var profileMenuExpanded by remember { mutableStateOf(false) }
	val selectedProfile = profiles.firstOrNull { it.id == working.useProfileId }

	androidx.compose.material3.AlertDialog(
		onDismissRequest = onDismiss,
		shape = RoundedCornerShape(16.dp),
		title = {
			Text(
				text = if (initialSchedule.id == 0L) "Add schedule" else "Edit schedule",
				style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
			)
		},
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 420.dp)
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				OutlinedTextField(
					value = working.name,
					onValueChange = { working = working.copy(name = it) },
					label = { Text("Schedule name") },
					placeholder = { Text("Weeknights · Health-first") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)

				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text(
						text = "Days",
						style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
					)

					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						val presets = listOf(
							"All" to allDaysMask(),
							"Weekdays" to weekdaysMask(),
							"Weekend" to weekendMask()
						)
						presets.forEach { (label, mask) ->
							val selectedPreset = working.daysMask == mask
							AssistChip(
								onClick = { working = working.copy(daysMask = mask) },
								label = { Text(label) },
								colors = AssistChipDefaults.assistChipColors(
									containerColor = if (selectedPreset)
										MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
									else
										MaterialTheme.colorScheme.surface,
									labelColor = if (selectedPreset)
										MaterialTheme.colorScheme.primary
									else
										MaterialTheme.colorScheme.onSurface
								)
							)
						}
					}

					val orderedDays = listOf(
						Weekday.Monday,
						Weekday.Tuesday,
						Weekday.Wednesday,
						Weekday.Thursday,
						Weekday.Friday,
						Weekday.Saturday,
						Weekday.Sunday
					)

					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						orderedDays.take(4).forEach { day ->
							val selected = isDaySelected(working.daysMask, day)
							FilterChip(
								selected = selected,
								onClick = {
									val newMask = toggleDay(working.daysMask, day)
									working = working.copy(daysMask = newMask)
								},
								label = { Text(day.label) },
								colors = FilterChipDefaults.filterChipColors(
									selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
										alpha = 0.12f
									),
									selectedLabelColor = MaterialTheme.colorScheme.primary
								)
							)
						}
					}
					Row(
						horizontalArrangement = Arrangement.spacedBy(4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						orderedDays.drop(4).forEach { day ->
							val selected = isDaySelected(working.daysMask, day)
							FilterChip(
								selected = selected,
								onClick = {
									val newMask = toggleDay(working.daysMask, day)
									working = working.copy(daysMask = newMask)
								},
								label = { Text(day.label) },
								colors = FilterChipDefaults.filterChipColors(
									selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
										alpha = 0.12f
									),
									selectedLabelColor = MaterialTheme.colorScheme.primary
								)
							)
						}
					}

					Text(
						text = daysMaskLabel(working.daysMask),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
					)
				}

				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text(
						text = "Time window",
						style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
					)

					Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
						Text(
							text = "Start: ${minutesToTimeString(working.startMinutes)}",
							style = MaterialTheme.typography.bodySmall
						)
						Slider(
							value = working.startMinutes.toFloat(),
							onValueChange = { value ->
								val snapped = ((value / 15f).roundToInt() * 15)
									.coerceIn(0, 24 * 60)
								working = working.copy(startMinutes = snapped)
							},
							valueRange = 0f..(24 * 60).toFloat()
						)
					}

					Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
						Text(
							text = "End: ${minutesToTimeString(working.endMinutes)}",
							style = MaterialTheme.typography.bodySmall
						)
						Slider(
							value = working.endMinutes.toFloat(),
							onValueChange = { value ->
								val snapped = ((value / 15f).roundToInt() * 15)
									.coerceIn(0, 24 * 60)
								working = working.copy(endMinutes = snapped)
							},
							valueRange = 0f..(24 * 60).toFloat()
						)
					}

					Text(
						text = "You can span overnight (e.g. 22:00 → 07:00). 00:00 → 00:00 means all day.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
					)
				}

				Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
					Text(
						text = "Behavior",
						style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
					)
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						FilterChip(
							selected = isProfileMode,
							onClick = {
								if (!isProfileMode) {
									isProfileMode = true
									if (working.useProfileId == null && profiles.isNotEmpty()) {
										working = working.copy(useProfileId = profiles.first().id)
									}
								}
							},
							label = { Text("Use profile") },
							colors = FilterChipDefaults.filterChipColors(
								selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
									alpha = 0.12f
								),
								selectedLabelColor = MaterialTheme.colorScheme.primary
							)
						)
						FilterChip(
							selected = !isProfileMode,
							onClick = {
								if (isProfileMode) {
									isProfileMode = false
									working = working.copy(useProfileId = null)
								}
							},
							label = { Text("Custom rules") },
							colors = FilterChipDefaults.filterChipColors(
								selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
									alpha = 0.12f
								),
								selectedLabelColor = MaterialTheme.colorScheme.primary
							)
						)
					}

					if (isProfileMode) {
						if (profiles.isEmpty()) {
							Text(
								text = "No profiles available yet. Create one above to use it in schedules.",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
							)
						} else {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Text(
									text = "Profile",
									style = MaterialTheme.typography.bodySmall
								)
								Column(horizontalAlignment = Alignment.End) {
									TextButton(onClick = { profileMenuExpanded = true }) {
										Text(
											text = selectedProfile?.name ?: "Choose profile",
											style = MaterialTheme.typography.bodySmall
										)
									}
									DropdownMenu(
										expanded = profileMenuExpanded,
										onDismissRequest = { profileMenuExpanded = false }
									) {
										profiles.forEach { profile ->
											DropdownMenuItem(
												text = { Text(profile.name) },
												onClick = {
													working =
														working.copy(useProfileId = profile.id)
													profileMenuExpanded = false
												}
											)
										}
									}
								}
							}
							Text(
								text = "During this window, alerts will follow the selected profile exactly.",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
							)
						}
					} else {
						Column(
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							Text(
								text = "Custom rules for this period",
								style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
							)
							Text(
								text = "Only fields you enable here will override your base rules.",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
							)

							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween,
								modifier = Modifier.fillMaxWidth()
							) {
								Text(
									text = "Low alert override",
									style = MaterialTheme.typography.bodySmall
								)
								Switch(
									checked = working.overrideLowPercent != null || working.overrideLowEnabled != null,
									onCheckedChange = { enabled ->
										if (!enabled) {
											working = working.copy(
												overrideLowEnabled = null,
												overrideLowPercent = null
											)
										} else {
											if (working.overrideLowPercent == null) {
												working = working.copy(
													overrideLowEnabled = true,
													overrideLowPercent = 20
												)
											}
										}
									}
								)
							}
							if (working.overrideLowPercent != null) {
								Text(
									text = "Low %: ${working.overrideLowPercent}",
									style = MaterialTheme.typography.bodySmall
								)
								Slider(
									value = working.overrideLowPercent!!.toFloat(),
									onValueChange = { value ->
										working = working.copy(
											overrideLowPercent = value.toInt().coerceIn(5, 50)
										)
									},
									valueRange = 5f..50f
								)
							}

							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween,
								modifier = Modifier.fillMaxWidth()
							) {
								Text(
									text = "High alert override",
									style = MaterialTheme.typography.bodySmall
								)
								Switch(
									checked = working.overrideHighPercent != null || working.overrideHighEnabled != null,
									onCheckedChange = { enabled ->
										if (!enabled) {
											working = working.copy(
												overrideHighEnabled = null,
												overrideHighPercent = null
											)
										} else {
											if (working.overrideHighPercent == null) {
												working = working.copy(
													overrideHighEnabled = true,
													overrideHighPercent = 80
												)
											}
										}
									}
								)
							}
							if (working.overrideHighPercent != null) {
								Text(
									text = "High %: ${working.overrideHighPercent}",
									style = MaterialTheme.typography.bodySmall
								)
								Slider(
									value = working.overrideHighPercent!!.toFloat(),
									onValueChange = { value ->
										working = working.copy(
											overrideHighPercent = value.toInt().coerceIn(60, 100)
										)
									},
									valueRange = 60f..100f
								)
							}

							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween,
								modifier = Modifier.fillMaxWidth()
							) {
								Text(
									text = "Repeat interval override",
									style = MaterialTheme.typography.bodySmall
								)
								Switch(
									checked = working.overrideRepeatMinutes != null,
									onCheckedChange = { enabled ->
										working = working.copy(
											overrideRepeatMinutes = if (enabled) 15 else null
										)
									}
								)
							}
							working.overrideRepeatMinutes?.let { repeat ->
								Text(
									text = "Repeat every $repeat min",
									style = MaterialTheme.typography.bodySmall
								)
								Slider(
									value = repeat.toFloat(),
									onValueChange = { value ->
										working = working.copy(
											overrideRepeatMinutes = value.toInt().coerceIn(5, 60)
										)
									},
									valueRange = 5f..60f
								)
							}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					var normalized = working

					if (isProfileMode) {
						if (normalized.useProfileId == null && profiles.isNotEmpty()) {
							normalized = normalized.copy(useProfileId = profiles.first().id)
						}
					} else {
						normalized = normalized.copy(useProfileId = null)
					}

					val start = normalized.startMinutes.coerceIn(0, 24 * 60 - 1)
					val end = normalized.endMinutes.coerceIn(0, 24 * 60 - 1)

					val finalSchedule = normalized.copy(
						startMinutes = start,
						endMinutes = end
					)
					onSave(finalSchedule)
				}
			) {
				Text("Save")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Cancel")
			}
		}
	)
}
