# Contributing to SureCharge

Thanks for considering contributing to SureCharge.  
This document explains how the project is organized, what kinds of contributions are welcome, and the rules to follow so collaboration stays clean and predictable.

---

## Project goals (important)

SureCharge aims to be:

- **Reliable**: battery alerts must fire when configured.
- **Simple**: no feature bloat, no “battery booster” myths.
- **Privacy-first**: local-only data, no tracking, no cloud by default.
- **Maintainable**: clear separation between UI, domain logic, and system code.

Any contribution should align with these goals.

---

## What contributions are welcome

### Good candidates

- Bug fixes (logic, lifecycle, edge cases, OEM quirks).
- Reliability improvements (alarms, services, boot handling).
- UI/UX improvements that reduce confusion or clicks.
- Performance or battery-usage improvements.
- Documentation improvements (README, in-app explanations).
- Accessibility improvements.
- Refactors that **reduce complexity** without changing behavior.

### Usually not accepted

- Features that promise impossible things (e.g. “stop charging at 80%” at hardware level).
- Ads, analytics, tracking, telemetry, or remote services.
- “Battery optimization / booster” myths.
- Large rewrites without prior discussion.
- Cosmetic-only changes that add maintenance cost without user value.

If unsure, open an issue first.

---

## Before you start

1. **Search existing issues**  
   Make sure your idea or bug isn’t already discussed.

2. **Open an issue (recommended)**  
   Especially for:
   - New features  
   - Behavior changes  
   - Non-trivial refactors  

   Describe:
   - The problem you’re solving  
   - Why it matters to users  
   - Any constraints or alternatives you considered  

This avoids wasted work.

---

## Development setup

### Requirements

- Android Studio (Giraffe or newer recommended)
- Android SDK as prompted by Android Studio
- Java 11+ (handled automatically by Android Studio)

### Getting the code

```bash
git clone https://github.com/thenolle/SureCharge.git
cd SureCharge
````

Open the project in Android Studio and let Gradle sync.

---

## Project structure (recap)

Inside `app/src/main/java/com/nolly/surecharge`:

* `data/`
  Persistence and repositories (Room, DataStore).
* `system/`
  Battery monitoring, health analysis, background logic.
* `presentation/`
  ViewModels and UI state.
* `ui/`
  Jetpack Compose screens, components, theming.
* `widget/`
  AppWidget providers, update logic, config activities.

Try to keep logic in the **lowest appropriate layer**:

* UI should not talk directly to system APIs.
* System logic should not depend on Compose/UI.

---

## Coding guidelines

### Kotlin & Android

* Kotlin only (no Java additions unless strictly required).
* Prefer immutability and data classes.
* Avoid global state unless clearly justified.
* Use coroutines/Flow for async and reactive logic.
* Keep background work explicit (WorkManager, foreground services).

### UI (Compose)

* Keep composables small and focused.
* No heavy logic inside composables.
* State flows from ViewModel → UI.
* Prefer Material 3 components unless there is a clear reason not to.

### Battery & system behavior

Be extremely careful when touching:

* Alarm scheduling
* Foreground services
* Boot receivers
* Battery optimization checks

Small mistakes here can break alerts entirely on some OEMs.

If you change system behavior:

* Test on at least one **real device** if possible.
* Document the behavior change in the PR description.

---

## Commits

* Use clear, descriptive commit messages.
* One logical change per commit when possible.

Good examples:

* `Fix high battery alert not firing after reboot`
* `Refactor BatteryHealthAnalyzer scoring logic`
* `Improve reliability checklist explanations`

Avoid:

* `fix stuff`
* `update`
* `wip`

---

## Pull request process

1. Fork the repository.
2. Create a branch from `main`:

   ```bash
   git checkout -b feature/my-change
   ```
3. Make your changes.
4. Test:

    * App launches
    * Alerts still fire
    * No crashes on basic flows
5. Open a pull request.

### PR description should include

* What changed
* Why it was needed
* Any user-visible behavior changes
* Any limitations or follow-ups

Screenshots are welcome for UI changes.

---

## Code style & review

* Keep code readable over clever.
* Avoid unnecessary abstractions.
* Expect feedback; it’s about the code, not you.
* Maintainer may request:

    * Simplifications
    * Renaming
    * Splitting a PR
    * Additional explanation

PRs may be rejected if they significantly increase complexity without clear benefit.

---

## Security & privacy

If your contribution touches:

* Permissions
* Background services
* Data storage
* Widgets
* Exports or logs

Be explicit in the PR about:

* What data is accessed
* Where it is stored
* Whether exposure risk changes

For vulnerabilities, follow `SECURITY.md` instead of opening a public issue.

---

## License

By contributing, you agree that your contributions will be licensed under the **MIT License**, the same license as the rest of the project.

---

## Final note

SureCharge is intentionally opinionated:
**reliability, honesty, and user trust come before feature count**.

If your change strengthens those, it’s likely welcome.
