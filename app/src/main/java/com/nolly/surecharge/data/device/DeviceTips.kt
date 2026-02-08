package com.nolly.surecharge.data.device

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

data class DeviceTip(
	val id: String,
	val title: String,
	val description: String,
	val learnMoreUrl: String? = null
)

object DeviceTipsCatalog {

	fun tipsForCurrentDevice(): List<DeviceTip> {
		val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
		val model = Build.MODEL?.trim().orEmpty()
		return tipsFor(manufacturer, model)
	}

	fun tipsFor(manufacturer: String, model: String): List<DeviceTip> {
		val m = manufacturer.lowercase(Locale.ROOT)
		val mdl = model.lowercase(Locale.ROOT)
		val tips = mutableListOf<DeviceTip>()

		when {
			m.contains("samsung") || mdl.startsWith("sm-") -> {
				tips += DeviceTip(
					id = "samsung_sleeping_apps",
					title = "Turn off \"Sleeping apps\" for SureCharge",
					description = "On many Samsung devices, background apps are put to sleep aggressively. " +
							"Make sure SureCharge is excluded from \"Sleeping apps\" and \"Deep sleeping apps\" " +
							"in Battery settings so alerts can fire on time.",
					learnMoreUrl = "https://dontkillmyapp.com/samsung"
				)
				tips += DeviceTip(
					id = "samsung_background_limits",
					title = "Check background usage limits",
					description = "In Settings › Battery and device care › Battery › Background usage limits, " +
							"ensure SureCharge is not limited so it can keep monitoring your battery reliably.",
					learnMoreUrl = "https://dontkillmyapp.com/samsung"
				)
			}

			m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") ||
					mdl.contains("xiaomi") || mdl.contains("redmi") || mdl.contains("poco") -> {
				tips += DeviceTip(
					id = "miui_autostart",
					title = "Enable autostart for SureCharge",
					description = "On MIUI devices (Xiaomi / Redmi / POCO), you often need to enable autostart " +
							"for apps that should run in the background. Enable autostart for SureCharge so the " +
							"monitoring service can restart after reboots.",
					learnMoreUrl = "https://dontkillmyapp.com/xiaomi"
				)
				tips += DeviceTip(
					id = "miui_battery_saver",
					title = "Disable extra battery saver for SureCharge",
					description = "In MIUI battery settings, make sure SureCharge is set to \"No restrictions\" or " +
							"similar, otherwise the system may silently stop its background service.",
					learnMoreUrl = "https://dontkillmyapp.com/xiaomi"
				)
			}

			m.contains("oneplus") || m.contains("oppo") || m.contains("realme") ||
					mdl.contains("oneplus") || mdl.contains("oppo") || mdl.contains("realme") -> {
				tips += DeviceTip(
					id = "oneplus_optimization",
					title = "Set optimization to \"Don't optimize\"",
					description = "On many OnePlus / OPPO / realme devices, you need to explicitly mark apps as " +
							"\"Don't optimize\" in battery optimization so they can run alarms reliably. " +
							"Set SureCharge to \"Don't optimize\" there.",
					learnMoreUrl = "https://dontkillmyapp.com/oneplus"
				)
				tips += DeviceTip(
					id = "oneplus_lock_recent",
					title = "Lock SureCharge in recents",
					description = "Locking SureCharge in the recent apps overview can prevent the system from " +
							"killing it too aggressively when memory is low.",
					learnMoreUrl = "https://dontkillmyapp.com/oneplus"
				)
			}

			m.contains("huawei") || m.contains("honor") ||
					mdl.contains("huawei") || mdl.contains("honor") -> {
				tips += DeviceTip(
					id = "huawei_protected",
					title = "Add SureCharge to protected apps",
					description = "On Huawei / Honor devices, add SureCharge to \"Protected apps\" or " +
							"similar lists so it can keep running when the screen is off.",
					learnMoreUrl = "https://dontkillmyapp.com/huawei"
				)
				tips += DeviceTip(
					id = "huawei_power_saving",
					title = "Exclude SureCharge from power saving",
					description = "Make sure SureCharge is excluded from any vendor-specific \"power saving\" " +
							"or \"app launch\" restrictions that might stop it in the background.",
					learnMoreUrl = "https://dontkillmyapp.com/huawei"
				)
			}

			m.contains("vivo") || m.contains("iqoo") ||
					mdl.contains("vivo") || mdl.contains("iqoo") -> {
				tips += DeviceTip(
					id = "vivo_autostart",
					title = "Allow auto-start for SureCharge",
					description = "On vivo / iQOO devices you may need to explicitly allow auto-start and " +
							"background running for apps like SureCharge.",
					learnMoreUrl = "https://dontkillmyapp.com/vivo"
				)
			}
		}

		if (tips.isEmpty()) {
			tips += DeviceTip(
				id = "generic_battery_optimizations",
				title = "Double-check battery optimizations",
				description = "Most Android devices have a battery optimization screen where you can allow apps " +
						"to run with fewer restrictions. Make sure SureCharge is not heavily optimized there.",
				learnMoreUrl = "https://dontkillmyapp.com/"
			)
		}

		return tips
	}
}

private const val DATASTORE_NAME = "device_tips_settings"

private val Context.deviceTipsDataStore by preferencesDataStore(name = DATASTORE_NAME)

class DeviceTipsStore(private val context: Context) {
	private object Keys {
		val DISMISSED_TIPS = stringSetPreferencesKey("dismissed_tips")
	}

	val dismissedTipsFlow: Flow<Set<String>> =
		context.deviceTipsDataStore.data.map { prefs ->
			prefs[Keys.DISMISSED_TIPS] ?: emptySet()
		}

	suspend fun dismissTip(id: String) {
		context.deviceTipsDataStore.edit { prefs ->
			val current = prefs[Keys.DISMISSED_TIPS] ?: emptySet()
			prefs[Keys.DISMISSED_TIPS] = current + id
		}
	}
}
