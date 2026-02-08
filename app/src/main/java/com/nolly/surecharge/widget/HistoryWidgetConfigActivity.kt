package com.nolly.surecharge.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.ComponentActivity
import com.nolly.surecharge.R

class HistoryWidgetConfigActivity : ComponentActivity() {
	private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setResult(RESULT_CANCELED)
		setContentView(R.layout.activity_history_widget_config)

		appWidgetId =
			intent?.extras?.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID
			)
				?: AppWidgetManager.INVALID_APPWIDGET_ID

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}

		val styleGroup = findViewById<RadioGroup>(R.id.radio_group_history_style)
		val radioCompact = findViewById<RadioButton>(R.id.radio_history_style_compact)
		val radioLarge = findViewById<RadioButton>(R.id.radio_history_style_large)

		val windowGroup = findViewById<RadioGroup>(R.id.radio_group_history_window)
		val radio7 = findViewById<RadioButton>(R.id.radio_history_7)
		val radio14 = findViewById<RadioButton>(R.id.radio_history_14)
		val radio30 = findViewById<RadioButton>(R.id.radio_history_30)

		val buttonSave = findViewById<Button>(R.id.button_history_save)

		radioLarge.isChecked = true
		radio14.isChecked = true

		radioCompact.setOnClickListener {
			styleGroup.check(radioCompact.id)
		}
		radioLarge.setOnClickListener {
			styleGroup.check(radioLarge.id)
		}

		radio7.setOnClickListener {
			windowGroup.check(radio7.id)
		}
		radio14.setOnClickListener {
			windowGroup.check(radio14.id)
		}
		radio30.setOnClickListener {
			windowGroup.check(radio30.id)
		}

		buttonSave.setOnClickListener {
			val style = when (styleGroup.checkedRadioButtonId) {
				R.id.radio_history_style_compact -> HistoryWidgetStyle.COMPACT
				else -> HistoryWidgetStyle.LARGE
			}

			val days = when (windowGroup.checkedRadioButtonId) {
				R.id.radio_history_7 -> 7
				R.id.radio_history_30 -> 30
				else -> 14
			}

			val config = HistoryWidgetConfig(
				style = style,
				daysWindow = days
			)

			HistoryWidgetProvider.saveConfig(this, appWidgetId, config)

			val mgr = AppWidgetManager.getInstance(this)
			HistoryWidgetProvider().updateAppWidget(this, mgr, appWidgetId)

			val result = Intent().apply {
				putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
			}
			setResult(RESULT_OK, result)
			finish()
		}
	}
}
