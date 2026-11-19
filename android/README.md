# Achievement Tracker (Android wrapper)

A small app that opens your Achievement Tracker web page full screen, with no address bar. It is the "option 1" wrapper: all the logic still runs in the Apps Script web app.

## Get the APK (no Android Studio needed)
1. Put this project in a GitHub repository (the `android` folder and `.github/workflows/build-apk.yml`).
2. On GitHub open **Actions → Build Android app → Run workflow**.
3. When it finishes, download the **AchievementTracker-apk** artifact, unzip it and copy `app-debug.apk` to your phone.
4. Open it on the phone and allow "Install unknown apps" for the app you opened it from.

## Or build in Android Studio
Open the `android` folder, let it sync, then Build → Build APK(s).

## First launch
Paste your web app link (the one from Tools → Mobile page (link and setup)). It is saved on the phone. If the page can't load, the app offers to change the link.

## Notes
- Sign in with the same Google account that owns the script. Google sometimes refuses sign-in inside an embedded browser; the app hides the WebView marker to avoid this, but if it still refuses, tell me and I'll switch the sign-in to Chrome.
- Store links open in your normal browser.
- After you deploy a new script version no reinstall is needed; the app just loads the page.
- This is a debug-signed build for your own phone, not for the Play Store.
