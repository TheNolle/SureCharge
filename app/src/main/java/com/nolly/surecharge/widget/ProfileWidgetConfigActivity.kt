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

class ProfileWidgetConfigActivity : ComponentActivity() {
	private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

	@SuppressLint("UseSwitchCompatOrMaterialCode")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setResult(RESULT_CANCELED)
		setContentView(R.layout.activity_profile_widget_config)

		appWidgetId =
			intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
				?: AppWidgetManager.INVALID_APPWIDGET_ID

		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}

		val styleGroup = findViewById<RadioGroup>(R.id.radio_group_profile_style)
		val radioSmall = findViewById<RadioButton>(R.id.radio_profile_style_small)
		val radioLarge = findViewById<RadioButton>(R.id.radio_profile_style_large)

		val switchShowSubtitle = findViewById<Switch>(R.id.switch_profile_show_subtitle)

		val buttonSave = findViewById<Button>(R.id.button_profile_save)

		radioLarge.isChecked = true
		switchShowSubtitle.isChecked = true

		radioSmall.setOnClickListener {
			styleGroup.check(radioSmall.id)
		}
		radioLarge.setOnClickListener {
			styleGroup.check(radioLarge.id)
		}

		buttonSave.setOnClickListener {
			val style = when (styleGroup.checkedRadioButtonId) {
				R.id.radio_profile_style_small -> ProfileWidgetStyle.SMALL
				else -> ProfileWidgetStyle.LARGE
			}

			val config = ProfileWidgetConfig(
				style = style,
				showSubtitle = switchShowSubtitle.isChecked
			)

			ProfileWidgetProvider.saveConfig(this, appWidgetId, config)

			val mgr = AppWidgetManager.getInstance(this)
			ProfileWidgetProvider().updateAppWidget(this, mgr, appWidgetId)

			val result = Intent().apply {
				putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
			}
			setResult(RESULT_OK, result)
			finish()
		}
	}
}
