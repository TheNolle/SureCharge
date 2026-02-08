package com.nolly.surecharge.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.activity.ComponentActivity
import com.nolly.surecharge.R

class SureChargeWidgetConfigActivity : ComponentActivity() {
	private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

	@SuppressLint("UseSwitchCompatOrMaterialCode")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setResult(RESULT_CANCELED)

		setContentView(R.layout.activity_widget_config)

		appWidgetId =
			intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
				?: AppWidgetManager.INVALID_APPWIDGET_ID

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}

		val styleGroup = findViewById<RadioGroup>(R.id.radio_group_style)
		val radioCompact = findViewById<RadioButton>(R.id.radio_style_compact)
		val radioStandard = findViewById<RadioButton>(R.id.radio_style_standard)
		val radioExtended = findViewById<RadioButton>(R.id.radio_style_extended)

		val switchShowNext = findViewById<Switch>(R.id.switch_show_next)
		val switchShowProfile = findViewById<Switch>(R.id.switch_show_profile)

		val quickGroup = findViewById<RadioGroup>(R.id.radio_group_quick_action)
		val radioQuickNone = findViewById<RadioButton>(R.id.radio_quick_none)
		val radioQuickSnooze = findViewById<RadioButton>(R.id.radio_quick_snooze)
		val radioQuickOpen = findViewById<RadioButton>(R.id.radio_quick_open)

		val buttonSave = findViewById<Button>(R.id.button_save)

		radioStandard.isChecked = true
		switchShowNext.isChecked = true
		switchShowProfile.isChecked = true
		radioQuickSnooze.isChecked = true

		radioCompact.setOnClickListener {
			styleGroup.check(radioCompact.id)
		}
		radioStandard.setOnClickListener {
			styleGroup.check(radioStandard.id)
		}
		radioExtended.setOnClickListener {
			styleGroup.check(radioExtended.id)
		}

		radioQuickNone.setOnClickListener {
			quickGroup.check(radioQuickNone.id)
		}
		radioQuickSnooze.setOnClickListener {
			quickGroup.check(radioQuickSnooze.id)
		}
		radioQuickOpen.setOnClickListener {
			quickGroup.check(radioQuickOpen.id)
		}

		buttonSave.setOnClickListener {
			val style = when (styleGroup.checkedRadioButtonId) {
				R.id.radio_style_compact -> WidgetStyle.COMPACT
				R.id.radio_style_extended -> WidgetStyle.EXTENDED
				else -> WidgetStyle.STANDARD
			}

			val quick = when (quickGroup.checkedRadioButtonId) {
				R.id.radio_quick_none -> WidgetQuickAction.NONE
				R.id.radio_quick_open -> WidgetQuickAction.OPEN_APP
				else -> WidgetQuickAction.SNOOZE_1H
			}

			val config = WidgetConfig(
				style = style,
				showNextEvent = switchShowNext.isChecked,
				showActiveProfile = switchShowProfile.isChecked,
				quickAction = quick
			)

			SureChargeWidgetProvider.saveConfig(this, appWidgetId, config)

			val appWidgetManager = AppWidgetManager.getInstance(this)
			SureChargeWidgetProvider().run {
				updateAppWidget(this@SureChargeWidgetConfigActivity, appWidgetManager, appWidgetId)
			}

			val result = Intent().apply {
				putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
			}
			setResult(RESULT_OK, result)
			finish()
		}
	}
}
