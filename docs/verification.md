# Verification — 2026-10-28

## Automated checks

JDK 17, Android SDK 36, Android Gradle Plugin 8.13.0, Gradle 8.13.

`./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug` completed successfully.

- 7 unit tests passed, 0 failed: conversation URL validation, status freshness
  boundaries, invalid snapshot fields, valid JSON import, unknown schema,
  unknown state, malformed/missing JSON fields.
- Android lint: 0 errors, 8 warnings. Remaining warnings are available
  Gradle/test-dependency updates and English-only inline UI text/localization advice.
- Debug APK built for `dev.sebastian.companion`, version 0.1.0 (1), minimum
  API 31, target API 36. No `uses-permission` entries in the packaged manifest.
- Gradle distribution SHA-256 pinned to the value from the official Gradle
  distribution endpoint.
- Reviewed the staged file list and source for credentials, private paths,
  conversation identifiers, copied reference bytes, and bundled product assets.
  Only synthetic test conversation IDs are included. Runtime data, local build
  files, signing keys, and environment files are excluded from Git.

## Runtime checks still required

The existing Pixel 8 Pro API 35 emulator could not start. It reported:
“Your device does not have enough disk space to run avd: Pixel_8_Pro_API_35.”
The host had approximately 1.1 GiB available. No unrelated data was deleted,
AVD configuration changed, or physical device installation performed.

Therefore activity launch, visual layout, animation timing, offline speech,
widget pinning/resizing, ChatGPT launch and app-link routing are **not yet
runtime verified**. The included `character-preview.svg` is a design export,
not an emulator screenshot.

Recommended acceptance checks on a device or emulator with sufficient space:

1. Launch Sebastian, inspect portrait/landscape layout and large font sizing.
2. Select each expression; check the “animation preview” label and reduced
   motion. Background the app and confirm motion/speech stop.
3. Trigger speech preview with an offline Android English voice installed.
4. Add/resize a widget and verify the disconnected label and tap handling.
5. Test ChatGPT installed and absent, then a user-supplied private conversation
   URL. Do not assume the URL opens the dot room until tested.
6. Import valid, expired, future, oversized and malformed status JSON. Verify
   manual labels, expiry, clear action, and widget snapshot timestamp.

## Integration status

No documented, authorized interface was established for exporting the current
dot runtime, exact model identity, memories, voice or proprietary animation
assets. No live connection is claimed. The original reference was inspected
privately and is excluded from this repository and APK.

## Project registration

The standalone directory and Git repository are independent from all existing
projects. Local app tools expose listing existing projects and moving threads
to existing projects, but no project-registration action. Native computer use
explicitly denied access to `com.openai.codex` for safety reasons. Sidebar
registration and moving the originating conversation remain uncompleted;
no application configuration files were modified to bypass that boundary.
