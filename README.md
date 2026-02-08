# SureCharge

Smart, reliable battery alerts and history for Android – with no ads, no tracking, and no gimmicks.

SureCharge watches how you really charge your phone, helps you set better low/high battery alerts, shows your charging history and an estimated “battery health” score, and gives you home screen widgets that actually stay in sync.

---

## Features

### Smart battery alerts that actually fire

* Low-battery alerts with configurable threshold (e.g. 20%) so you plug in before it is too late.
* High-battery alerts with configurable threshold (e.g. 80%) so you don’t sit at 100% for hours.
* Optional repeat reminders so you don’t miss the first notification.
* Alerts work both when the phone is in use and when it is idle/charging on a desk or at night.

### Auto mode – tuned from your own habits

* Auto mode learns from your recent charge sessions.
* It looks at where you usually start and stop charging and suggests a health-friendly low/high range around that.
* You can:

    * Apply the suggested range once (“Apply once”), or
    * Turn auto mode on so SureCharge periodically re-tunes alerts based on new history.
* When there’s not enough history yet, SureCharge falls back to a balanced default (for example around 20–80%).

### Profiles and schedules

* Save the current alert configuration as a **profile** (e.g. “Workday”, “Overnight”, “Travel”).
* Each profile can have:

    * Its own low battery alert on/off and level
    * Its own high battery alert on/off and level
    * Its own repeat-reminder interval
* Switch profiles in a few taps from inside the app, or cycle through them from a widget quick action.
* Create **schedules** that automatically switch to a profile at certain days/times:

    * Different rules for weekdays vs weekends
    * “Night” or “office” profiles, etc.
* Schedules can be individually enabled/disabled and edited without touching the actual profiles.

### Charging history & battery health estimate

* SureCharge records charge sessions (start %, end %, timestamps).
* History view shows:

    * Recent sessions list (when you charged, and from/to which levels)
    * A timeline chart of where you typically unplug
    * Configurable history window (e.g. 7–90 days)
* From this, SureCharge computes an approximate **Battery health (estimate)**:

    * Estimated number of full charge cycles
    * How often you hit very high levels (≥ ~95%)
    * How often you go very low (≤ ~10%)
    * Typical charge speed (percentage per hour)
    * Rough age of the recorded history
    * A 0–100 score with a verdict label (“Great”, “Good”, “Worn”, “Degraded”)
    * Suggestions like “Unplug closer to 80–90%” or “Avoid going below 10% regularly” based on your actual usage.

> Important: this is an estimate based on behavior, not a hardware-level measurement. It cannot “repair” a worn battery.

### Reliability checklist & OEM-specific tips

Android and some manufacturers are aggressive about killing background apps. SureCharge brings this into the open:

* **Reliability checklist** screen shows:

    * Battery optimization status (OK vs action needed, with a direct path to the right system settings).
    * Exact alarm permission / scheduling status (when supported).
    * Background activity restrictions (unrestricted vs restricted vs info).
* Each item explains what it means and what you may need to change so alerts can fire on time.
* Device-specific tips for common OEMs (Samsung, Xiaomi/Redmi/POCO, OnePlus/OPPO/realme, Huawei/Honor, vivo/iQOO, etc.):

    * “Enable autostart”, “Set to Don’t optimize”, “Exclude from power saving”, etc.
    * Links to external documentation such as dontkillmyapp for more details.
* Tips can be dismissed and won’t keep nagging you.

### Home screen widgets

SureCharge ships multiple widgets that work on the classic Android home screen:

1. **Main SureCharge widget**

    * Shows live battery percentage.
    * Displays current alert rules summary (low/high thresholds, repeat interval).
    * Optional “next alert” text, e.g. when the next reminder is expected.
    * Extended style can also show:

        * Which profile is active
        * Which schedule (if any) is currently in effect.
    * Configurable styles:

        * Compact
        * Standard
        * Extended

2. **History widget**

    * Small sparkline of recent unplug levels.
    * Summary like “Avg start 28% · end 81%”.
    * Label for the visible window (“Last 14 days”, etc.).
    * Two layout styles: compact and large.
    * Clicking opens directly into the in-app history view.

3. **Profile widget**

    * Shows currently active profile name and a short description.
    * “Next” action cycles through saved profiles.
    * “Open” action jumps into the app.

All widgets support quick actions like snoozing alerts for 1 hour, opening the app, or cycling profiles (depending on configuration).

### Snoozing

* Quickly snooze all alerts for 1 hour (e.g. while gaming, navigating, or debugging).
* Toggle again to clear snooze earlier.
* Integrated into both the main app and widgets (via quick actions).

### Info, tips & legal built in

* **FAQ** tab explaining:

    * What SureCharge does and does not do
    * Why battery percentages sometimes differ slightly from the system UI
    * Why specific permissions are requested
    * That the app cannot alter hardware charging behavior or magically fix battery wear.
* **Tips** tab with practical best practices (charging style, heat, overnight charging, etc.).
* **Credits** tab (development, tools, icons).
* **Legal** tab summarizing privacy and terms of use (see also “Privacy” below).

---

## What SureCharge does NOT do

To be explicit:

* It does **not** control or modify how your phone charges.
* It does **not** change voltages, currents, or fast-charging logic.
* It does **not** root your device or modify system files.
* It does **not** guarantee any particular battery lifetime or health outcome.

SureCharge:

* Reads battery state from standard Android system APIs.
* Stores that data locally on your device.
* Uses it to show statistics and send notifications.

All actual charging behavior is controlled by your phone and charger.

---

## Privacy

SureCharge is built to be private by design:

* No analytics SDKs, no tracking, no ads.
* No network calls for your battery data – everything stays on the device.
* App does not read your messages, photos, files, or app data.
* Charging sessions and statistics are stored locally in a Room database.
* The only way anything leaves your phone is if you manually share something (e.g. a screenshot, or a log you choose to export yourself).

A more detailed privacy summary and terms of use are available in the in-app “Info & help” screen.

---

## Permissions

SureCharge asks for the minimum runtime permissions needed for its job:

* **Post notifications**

    * Used to show low/high battery alerts, repeat reminders, and background status notifications when monitoring.
* **Foreground service (data sync / monitoring)**

    * Used to keep a lightweight monitoring service alive when required so alerts are delivered on time.
* **Receive boot completed**

    * Lets SureCharge resume monitoring and schedules after you reboot your device.

On some devices, you may also need to:

* Exclude SureCharge from aggressive battery optimizations.
* Allow exact alarms (where applicable).
* Enable auto-start or remove vendor-specific background limits.

The app’s Reliability checklist guides you through these steps.

---

## Installation

### Play Store

SureCharge is available on Google Play as a paid app (no ads, no IAP) to support development.
> Link will be added here once the listing is live.

### From source / GitHub release

You can also build and sideload the APK yourself:

1. Clone the repository:

   ```bash
   git clone https://github.com/thenolle/SureCharge.git
   cd SureCharge
   ```
2. Open the project in **Android Studio** (Giraffe+ recommended).
3. Let Gradle sync.
4. Select the `app` run configuration and a device/emulator.
5. Build and run.

---

## Building from source

### Requirements

* Recent Android Studio (Giraffe or newer).
* Android SDK and build-tools as prompted by Android Studio.
* Java 11+ (handled automatically by modern Android Studio).

### Modules & tech stack

* Single-module Android app (`app/`).
* Written in **Kotlin**.
* UI built with **Jetpack Compose** + Material 3.
* Uses:

    * ViewModels + Kotlin coroutines/Flows.
    * Room database for history, profiles, and schedules.
    * WorkManager for periodic auto-tuning.
    * App widgets via classic RemoteViews and XML `appwidget-provider` definitions.

---

## Project structure (high level)

Inside `app/src/main/java/com/nolly/surecharge`:

* `data/`

    * `battery/` – alert rules model + storage.
    * `history/` – charge session entities, DAO, repository.
    * `profile/` – profiles entities, DAO, repository.
    * `schedule/` – schedules entities, DAO, repository.
    * Various `*Store` classes for DataStore-based settings.
* `presentation/`

    * ViewModels for rules, history, profiles, schedules, etc.
    * `HistoryUiState`, `ProfilesUiState`, `SchedulesUiState`, etc.
* `ui/`

    * `screens/` – main app surfaces (Rules, History, Settings, Info, Reliability).
    * `components/` – reusable Compose cards (AutoCard, BatteryHealthCard, History charts, etc.).
    * `theme/` – colors, typography, theming utilities.
* `system/`

    * `BatteryHealthAnalyzer`, `AutoRules`, `ReliabilityStatus`, etc.
    * Worker and background logic.
* `widget/`

    * AppWidget providers, configuration activities, and update logic for main, history, and profile widgets.

Resources under `app/src/main/res`:

* `layout/` – RemoteViews layouts for widgets.
* `values/` – strings, themes, dimensions.
* `xml/` – widget providers, backup rules, data extraction rules.
* `mipmap/` – launcher icons.

---

## Development status

* The app is functional and used for real-world battery monitoring.
* APIs and internal storage formats may still evolve.
* Backward compatibility is best-effort but not guaranteed until a 1.0 tag is declared.

---

## Contributing

Issues and pull requests are welcome.

If you want to contribute:

1. Fork the repository.
2. Create a feature branch:

   ```bash
   git checkout -b feature/my-change
   ```
3. Make your changes (Kotlin, Compose, etc.).
4. Run and test on at least one physical device if possible.
5. Open a pull request with a clear description of what you changed and why.

---

## License

This project is licensed under the **MIT License** – see the `LICENSE` file for details.
