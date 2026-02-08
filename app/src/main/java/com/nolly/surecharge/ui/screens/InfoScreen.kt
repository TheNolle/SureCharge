package com.nolly.surecharge.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
	onClose: () -> Unit,
	modifier: Modifier = Modifier
) {
	var selectedTab by remember { mutableIntStateOf(0) }

	Surface(
		modifier = modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
	) {
		Column {
			TopAppBar(
				title = {
					Text(
						text = "Info & help",
						style = MaterialTheme.typography.titleLarge.copy(
							fontWeight = FontWeight.SemiBold
						)
					)
				},
				navigationIcon = {
					TextButton(onClick = onClose) {
						Text("Back")
					}
				}
			)

			SecondaryTabRow(selectedTabIndex = selectedTab) {
				Tab(
					selected = selectedTab == 0,
					onClick = { selectedTab = 0 },
					text = { Text("FAQ") }
				)
				Tab(
					selected = selectedTab == 1,
					onClick = { selectedTab = 1 },
					text = { Text("Tips") }
				)
				Tab(
					selected = selectedTab == 2,
					onClick = { selectedTab = 2 },
					text = { Text("Credits") }
				)
				Tab(
					selected = selectedTab == 3,
					onClick = { selectedTab = 3 },
					text = { Text("Legal") }
				)
			}

			when (selectedTab) {
				0 -> InfoFaqTab(
					modifier = Modifier
						.weight(1f)
						.fillMaxSize()
				)

				1 -> InfoTipsTab(
					modifier = Modifier
						.weight(1f)
						.fillMaxSize()
				)

				2 -> InfoCreditsTab(
					modifier = Modifier
						.weight(1f)
						.fillMaxSize()
				)

				3 -> InfoLegalTab(
					modifier = Modifier
						.weight(1f)
						.fillMaxSize()
				)
			}
		}
	}
}

@Composable
private fun InfoFaqTab(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.padding(16.dp)
	) {
		SectionTitle("Frequently asked questions")

		Text(
			text = "A quick overview of how SureCharge behaves so you always know what the app is (and is not) doing.",
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.padding(bottom = 12.dp)
		)

		FaqCard(
			question = "Does SureCharge control how my phone charges or change hardware behavior?",
			answer = "No. SureCharge cannot modify how your phone’s hardware charges the battery. It observes the battery information that Android exposes and uses that to analyze your habits, show statistics, and send smart reminders. All charging decisions are still fully managed by your device and charger."
		)

		FaqCard(
			question = "Can SureCharge improve battery health or slow down wear?",
			answer = "SureCharge cannot repair a worn-out battery or override hardware limitations. However, by helping you avoid long periods at 100%, excessive heat, and unnecessary fast charging, it can encourage habits that are widely considered healthier for lithium batteries."
		)

		FaqCard(
			question = "Why is the battery percentage sometimes different from what Android shows?",
			answer = "SureCharge reads the same battery information that Android provides. In rare cases, the system may update in steps (for example from 97% directly to 100%), while SureCharge’s graphs or timing estimates may appear smoother or slightly delayed."
		)

		FaqCard(
			question = "Why do I get unplug reminders when I’m still using the phone?",
			answer = "SureCharge primarily looks at charging level, duration, and whether the phone seems idle or used. Sometimes you might still be using your device while it’s at 100% and plugged in. If reminders bother you in those situations, you can adjust the high-level rule or disable that alert in the settings."
		)

		FaqCard(
			question = "Will using SureCharge void my warranty?",
			answer = "No. SureCharge does not perform any operations that should affect your hardware warranty. It does not overcharge, undervolt, root, or modify system files. It only reads battery status information that Android makes available and sends you recommendations and reminders."
		)

		FaqCard(
			question = "Does SureCharge collect or share any of my personal data?",
			answer = "No. SureCharge never collects analytics, never uploads your data to a server, and never reads your personal content (photos, messages, files). Charging history and statistics stay locally on your device. The only way anything ever leaves your phone is if you explicitly decide to share a screenshot or diagnostic report yourself."
		)

		FaqCard(
			question = "Why does SureCharge need notification permission?",
			answer = "SureCharge uses notifications to tell you when your battery reaches the smart charging limit, when it has been full for too long, or when unusual charging behavior is detected. Without notification access, the app cannot warn you in real time."
		)

		FaqCard(
			question = "Does SureCharge run all the time and drain my battery?",
			answer = "SureCharge is designed to be lightweight. It relies on system signals about charging and battery state instead of constantly waking the CPU. Some monitoring is necessary to detect charging events, but the overhead is kept minimal and should be negligible compared to normal phone usage."
		)

		FaqCard(
			question = "Can I use SureCharge with wireless charging or power banks?",
			answer = "Yes. SureCharge works with any charging method that Android reports, including wired chargers, wireless pads, and power banks. The app only cares about what the system reports about battery level, state, and temperature."
		)

		FaqCard(
			question = "How do I reset my statistics and start fresh?",
			answer = "Go to the app’s settings and look for the option to clear history or reset data. This will erase all recorded charging sessions and statistics, allowing you to start fresh. Note that this action cannot be undone, so make sure you want to lose your current data before confirming."
		)

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Composable
private fun InfoTipsTab(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.padding(16.dp)
	) {
		SectionTitle("Tips & best practices")

		InfoCard(
			title = "Everyday use",
			body = {
				Bullet("Avoid staying at 100% for many hours, especially overnight.")
				Bullet("High temperatures accelerate battery wear; avoid charging under blankets or on hot surfaces.")
				Bullet("Frequent short top-ups (for example 40% → 80%) are generally less stressful than constant 0% → 100% cycles.")
			}
		)

		InfoCard(
			title = "Charging style",
			body = {
				Bullet("Fast charging is convenient but can generate more heat; use it when needed, not all the time.")
				Bullet("If you often charge overnight, consider using a smart limit around 70%–85% to reduce time at 100%.")
			}
		)

		InfoCard(
			title = "Device care",
			body = {
				Bullet("Keep your device and apps updated so battery reporting stays accurate.")
				Bullet("Avoid using your phone for heavy gaming or navigation while it is already very hot and charging.")
			}
		)

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Composable
private fun InfoCreditsTab(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.padding(16.dp)
	) {
		SectionTitle("App & development")

		InfoCard(
			title = "Built for Android",
			body = {
				Text(
					text = "SureCharge is designed specifically for Android devices, using Kotlin and modern Jetpack libraries.",
					style = MaterialTheme.typography.bodyMedium
				)
			}
		)

		InfoCard(
			title = "Concept & development",
			body = {
				Text(
					text = "Design & development: Nolly",
					style = MaterialTheme.typography.bodyMedium
				)
			}
		)

		InfoCard(
			title = "Tools & libraries",
			body = {
				Text(
					text = "SureCharge uses official AndroidX and Jetpack components. Additional open-source libraries, if any, are listed in the in-app licenses section (if provided) or in the project README.",
					style = MaterialTheme.typography.bodyMedium
				)
			}
		)

		InfoCard(
			title = "Icons & design assets",
			body = {
				Text(
					text = "Core visuals are created specifically for SureCharge, and may include Material icons used under their respective license terms.",
					style = MaterialTheme.typography.bodyMedium
				)
			}
		)

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Composable
private fun InfoLegalTab(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.padding(16.dp)
	) {
		SectionTitle("Privacy summary")

		InfoCard(
			title = "How your data is handled",
			body = {
				Text(
					text = "SureCharge does not access or read your personal files, messages, photos, or application data. Battery and charging data are read from Android’s system APIs and stored locally on your device. No analytics, tracking, or cloud sync is used.",
					style = MaterialTheme.typography.bodyMedium
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "The only way any information leaves your phone is if you manually choose to share something (for example, a screenshot or a diagnostic report).",
					style = MaterialTheme.typography.bodyMedium
				)
			}
		)

		SectionTitle("Terms of use")

		InfoCard(
			title = "What SureCharge promises (and doesn’t)",
			body = {
				Bullet("SureCharge is provided “as is” without guarantees of accuracy or battery health improvement.")
				Bullet("The app offers suggestions and information only; final charging decisions are your responsibility.")
				Bullet("We are not responsible for hardware issues, battery failures, or data loss.")
				Bullet("You must comply with your device manufacturer’s and OS vendor’s warranties and policies.")
				Bullet("Do not use SureCharge in situations where inaccurate battery information could lead to harm or damage.")
			}
		)

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Composable
private fun SectionTitle(text: String) {
	Text(
		text = text,
		style = MaterialTheme.typography.titleMedium.copy(
			fontWeight = FontWeight.SemiBold
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp)
	)
}

@Composable
private fun FaqCard(
	question: String,
	answer: String
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 10.dp),
		shape = MaterialTheme.shapes.medium,
		tonalElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
		) {
			Text(
				text = question,
				style = MaterialTheme.typography.titleSmall.copy(
					fontWeight = FontWeight.SemiBold
				)
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = answer,
				style = MaterialTheme.typography.bodyMedium
			)
		}
	}
}

@Composable
private fun InfoCard(
	title: String,
	body: @Composable ColumnScope.() -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 10.dp),
		shape = MaterialTheme.shapes.medium,
		tonalElevation = 2.dp
	) {
		Column(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall.copy(
					fontWeight = FontWeight.SemiBold
				),
				modifier = Modifier.padding(bottom = 4.dp)
			)
			body()
		}
	}
}

@Composable
private fun Bullet(text: String) {
	Text(
		text = "• $text",
		style = MaterialTheme.typography.bodyMedium
	)
	Spacer(modifier = Modifier.height(4.dp))
}
