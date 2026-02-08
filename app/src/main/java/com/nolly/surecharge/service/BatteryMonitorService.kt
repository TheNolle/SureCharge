package com.nolly.surecharge.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nolly.surecharge.MainActivity
import com.nolly.surecharge.R
import com.nolly.surecharge.data.SnoozeManager
import com.nolly.surecharge.data.battery.BatteryRules
import com.nolly.surecharge.data.battery.BatteryRulesStore
import com.nolly.surecharge.data.history.ChargeHistoryRepository
import com.nolly.surecharge.data.history.ChargeSessionTracker
import com.nolly.surecharge.data.profile.BatteryProfile
import com.nolly.surecharge.data.profile.BatteryProfilesRepository
import com.nolly.surecharge.data.schedule.EffectiveRulesResult
import com.nolly.surecharge.data.schedule.Schedule
import com.nolly.surecharge.data.schedule.ScheduleResolver
import com.nolly.surecharge.data.schedule.SchedulesRepository
import com.nolly.surecharge.ui.notifications.Notifications
import com.nolly.surecharge.util.AppLogger
import com.nolly.surecharge.widget.SureChargeWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BatteryMonitorService : Service() {
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	private lateinit var rulesStore: BatteryRulesStore
	private lateinit var snoozeManager: SnoozeManager
	private lateinit var historyRepository: ChargeHistoryRepository
	private lateinit var sessionTracker: ChargeSessionTracker
	private lateinit var profilesRepository: BatteryProfilesRepository
	private lateinit var schedulesRepository: SchedulesRepository
	private var lastIsCharging: Boolean? = null
	private var hasFiredLowThisCycle: Boolean = false
	private var hasFiredHighThisCycle: Boolean = false

	@Volatile
	private var baseRules: BatteryRules = BatteryRules()

	@Volatile
	private var schedules: List<Schedule> = emptyList()

	@Volatile
	private var profiles: List<BatteryProfile> = emptyList()

	private val batteryReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
				val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
				val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
				val status = intent.getIntExtra(
					BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN
				)

				val isCharging =
					status == BatteryManager.BATTERY_STATUS_CHARGING ||
							status == BatteryManager.BATTERY_STATUS_FULL

				val percent =
					if (level >= 0 && scale > 0) level * 100 / scale else -1

				AppLogger.d(
					TAG,
					"Battery broadcast: level=$level scale=$scale -> percent=$percent, " +
							"isCharging=$isCharging status=$status"
				)

				if (percent >= 0) {
					handleBatteryUpdate(percent, isCharging)
				}
			}
		}
	}

	override fun onCreate() {
		super.onCreate()
		AppLogger.i(TAG, "onCreate: BatteryMonitorService starting")

		val appContext = applicationContext

		rulesStore = BatteryRulesStore(appContext)
		snoozeManager = SnoozeManager.from(appContext)
		historyRepository = ChargeHistoryRepository(appContext)
		sessionTracker = ChargeSessionTracker(appContext, historyRepository, serviceScope)
		profilesRepository = BatteryProfilesRepository(appContext)
		schedulesRepository = SchedulesRepository(appContext)

		serviceScope.launch {
			rulesStore.rulesFlow.collectLatest { rules ->
				baseRules = rules
				AppLogger.d(
					TAG,
					"Rules updated: lowEnabled=${rules.lowLevelEnabled} low=${rules.lowLevelPercentage}, " +
							"highEnabled=${rules.highLevelEnabled} high=${rules.highLevelPercentage}, " +
							"repeat=${rules.repeatIntervalMinutes}"
				)
				SureChargeWidgetProvider.updateAllWidgets(appContext)
			}
		}

		serviceScope.launch {
			profilesRepository.profilesFlow.collectLatest { list ->
				profiles = list
				AppLogger.d(TAG, "Profiles updated: count=${list.size}")
				SureChargeWidgetProvider.updateAllWidgets(appContext)
			}
		}

		serviceScope.launch {
			schedulesRepository.schedulesFlow.collectLatest { list ->
				schedules = list
				AppLogger.d(TAG, "Schedules updated: count=${list.size}")
				SureChargeWidgetProvider.updateAllWidgets(appContext)
			}
		}

		registerReceiver(
			batteryReceiver,
			IntentFilter(Intent.ACTION_BATTERY_CHANGED)
		)
		AppLogger.d(TAG, "Registered battery receiver")

		val notification = buildForegroundNotification()
		AppLogger.d(TAG, "Starting foreground with notification id=$MONITOR_NOTIFICATION_ID")
		startForeground(
			MONITOR_NOTIFICATION_ID,
			notification
		)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		AppLogger.d(TAG, "onStartCommand: startId=$startId intent=$intent flags=$flags")
		return START_STICKY
	}

	override fun onDestroy() {
		AppLogger.i(TAG, "onDestroy: BatteryMonitorService shutting down")
		super.onDestroy()
		try {
			unregisterReceiver(batteryReceiver)
			AppLogger.d(TAG, "Battery receiver unregistered")
		} catch (t: IllegalArgumentException) {
			AppLogger.w(TAG, "Battery receiver was not registered or already unregistered", t)
		}
		serviceScope.cancel()
	}

	override fun onBind(intent: Intent?): IBinder? = null

	private fun handleBatteryUpdate(levelPercent: Int, isCharging: Boolean) {
		AppLogger.d(TAG, "handleBatteryUpdate: level=$levelPercent isCharging=$isCharging")

		sessionTracker.onBatteryUpdate(levelPercent, isCharging)

		if (snoozeManager.isSnoozed()) {
			AppLogger.d(TAG, "Snoozed: skipping alerts for this update")
			SureChargeWidgetProvider.updateAllWidgets(this)
			return
		}

		val previousCharging = lastIsCharging
		if (previousCharging != null && previousCharging != isCharging) {
			AppLogger.d(
				TAG,
				"Charging state changed: $previousCharging -> $isCharging. Resetting cycle flags."
			)
			hasFiredLowThisCycle = false
			hasFiredHighThisCycle = false
		}
		lastIsCharging = isCharging

		val effective: EffectiveRulesResult = ScheduleResolver.resolve(
			baseRules = baseRules,
			schedules = schedules,
			profiles = profiles,
			nowMillis = System.currentTimeMillis()
		)

		val rules = effective.rules
		val sourceLabel = effective.activeSchedule?.let {
			"schedule(id=${it.id}, name=\"${it.name}\")"
		} ?: "baseRules"

		AppLogger.d(
			TAG,
			"Effective rules: lowEnabled=${rules.lowLevelEnabled} low=${rules.lowLevelPercentage}, " +
					"highEnabled=${rules.highLevelEnabled} high=${rules.highLevelPercentage}, " +
					"repeat=${rules.repeatIntervalMinutes}, source=$sourceLabel"
		)

		if (!isCharging && rules.lowLevelEnabled) {
			if (!hasFiredLowThisCycle && levelPercent <= rules.lowLevelPercentage) {
				AppLogger.i(
					TAG,
					"Low alert condition met: level=$levelPercent <= low=${rules.lowLevelPercentage}"
				)
				try {
					Notifications.showLowBatteryAlert(this, levelPercent)
					hasFiredLowThisCycle = true
				} catch (t: Throwable) {
					AppLogger.e(TAG, "Failed to show low battery notification", t)
				}
			}
		}

		if (isCharging && rules.highLevelEnabled) {
			if (!hasFiredHighThisCycle && levelPercent >= rules.highLevelPercentage) {
				AppLogger.i(
					TAG,
					"High alert condition met: level=$levelPercent >= high=${rules.highLevelPercentage}"
				)
				try {
					Notifications.showHighChargeAlert(this, levelPercent)
					hasFiredHighThisCycle = true
				} catch (t: Throwable) {
					AppLogger.e(TAG, "Failed to show high charge notification", t)
				}
			}
		}

		SureChargeWidgetProvider.updateAllWidgets(this)
	}

	private fun buildForegroundNotification(): Notification {
		ensureMonitorChannel()

		val intent = Intent(this, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		val pendingIntent = PendingIntent.getActivity(
			this,
			0,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)

		return NotificationCompat.Builder(this, MONITOR_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle("SureCharge is active")
			.setContentText("Monitoring your battery for reliable alerts.")
			.setOngoing(true)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setContentIntent(pendingIntent)
			.build()
	}

	private fun ensureMonitorChannel() {
		val manager = getSystemService(NotificationManager::class.java)
		val channel = NotificationChannel(
			MONITOR_CHANNEL_ID,
			"SureCharge monitoring",
			NotificationManager.IMPORTANCE_LOW
		)
		manager.createNotificationChannel(channel)
		AppLogger.d(TAG, "Monitor notification channel ensured: id=$MONITOR_CHANNEL_ID")
	}

	companion object {
		private const val TAG = "BatteryMonitorService"
		private const val MONITOR_CHANNEL_ID = "surecharge_monitor"
		private const val MONITOR_NOTIFICATION_ID = 1
	}
}
