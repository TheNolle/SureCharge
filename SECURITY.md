# Security Policy

SureCharge takes user trust seriously.  
This document explains which versions are supported, how to report vulnerabilities, and what is considered in scope.

---

## Supported versions

SureCharge is an Android application distributed via Google Play and GitHub releases.

Only the **latest published version** is guaranteed to receive security fixes.

| Version            | Supported |
|--------------------|----------|
| Latest release     | Yes    |
| Older releases     | No    |
| Development builds | No    |

Because SureCharge relies on background services, alarms, and local storage, users are strongly encouraged to keep the app up to date.

---

## Reporting a vulnerability

If you discover a security or privacy issue, **do not open a public GitHub issue** with full details.

Instead, please report it responsibly:

- Open a private issue in this repository with the label `security`

Include as much detail as possible:

- A clear description of the issue
- Affected app version(s)
- Android version and device model (if relevant)
- Steps to reproduce
- Potential impact (data exposure, bypass, crash, etc.)

You may also include:
- Logs (with personal data redacted)
- A suggested fix or patch

We aim to:
- Acknowledge reports within a reasonable timeframe
- Investigate and assess severity
- Ship a fix as soon as feasible
- Credit the reporter if they wish (optional)

---

## Scope

### In scope

- SureCharge Android application code in this repository
- Local data handling (Room database, DataStore, files)
- Background services, workers, and alarms
- Widgets and widget configuration activities
- Notification logic and permission handling
- Boot handling (`RECEIVE_BOOT_COMPLETED`)
- Foreground service usage and lifecycle

### Out of scope

- Modified or re-signed builds distributed by third parties
- Forks not maintained by the SureCharge author
- Issues caused purely by OEM firmware bugs outside app control
- Rooted-device–only attack vectors unless they expose app data beyond the device owner
- Denial-of-service caused by deliberate system misconfiguration (e.g. force-stopping the app)

---

## Data & privacy considerations

SureCharge is designed to be **offline-first** and **local-only**:

- No user accounts
- No cloud sync
- No analytics or tracking SDKs
- No network transmission of battery or usage data

Security issues of particular interest include:

- Unauthorized access to locally stored battery history or profiles
- Leaking data via logs, backups, or exported files
- Incorrect use of foreground services or notifications that could be abused
- Widget data exposure to other apps
- Misuse of system APIs that could weaken OS security guarantees

---

## Dependencies

SureCharge depends on standard Android and Jetpack libraries (e.g. Room, WorkManager, Compose).

If a vulnerability is clearly caused by a third-party dependency:

- Please still report it here
- Also consider reporting it upstream if not already disclosed

We regularly update dependencies when practical, but backward compatibility with very old Android versions may limit immediate upgrades.

---

## Responsible disclosure

Please follow responsible disclosure practices:

- Do not exploit vulnerabilities beyond what is necessary to demonstrate them
- Do not access or attempt to access other users’ data
- Do not publish details publicly before a fix or mitigation is available

Good-faith research is appreciated and welcomed.

---

## Security updates

Security fixes will be delivered through normal app updates.

In case of a critical issue:
- A patched release will be prioritized
- Release notes will mention a security fix (without exposing exploit details)

---

If you have any doubts about whether something counts as a security issue, **report it anyway**.  
False positives are better than silent bugs.
