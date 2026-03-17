# MotoÉire 🇮🇪 🚗

**MotoÉire** is a modern, privacy-focused vehicle management app built specifically for the Irish automotive landscape. From tracking your NCT and Motor Tax to calculating complex VRT imports with community-driven data, MotoÉire is the digital glovebox for every Irish driver.

---

## ✨ Features

### 🛡️ My Garage (Local-First)
Keep track of your fleet without compromising your privacy.
* **Vehicle Profiles:** Store Registration, Insurance Policy numbers, and provider details.
* **Smart Reminders:** Real-time tracking of **NCT** and **Motor Tax** renewal dates.
* **Actionable Alerts:** High-contrast Material 3 warnings when renewals are due, with direct "One-Tap" links to the official NCTS and Motor Tax payment portals.
* **Privacy by Design:** All garage data is stored strictly on-device using a local SQLite/Room database. **Your data never leaves your phone.**

### 📉 VRT & Import Calculator (Coming Soon)
Navigating Irish car imports made simple.
* **Smart Calculation:** Automatically calculates VRT, NOx levies, and Customs duties based on the latest Revenue Commissioner bands.
* **Crowdsourced Emissions:** A community-driven database for JDM (Japanese Domestic Market) and rare imports.
* **Consensus Engine:** User-submitted CO2/NOx data is verified by the community to ensure accurate tax estimates for cars not found in standard EU databases.

---

## 🎨 Design Language
Built with **Material 3 (M3) Expressive**.
* **Dynamic Color:** The app UI adapts to your device's wallpaper and theme.
* **Accessible UI:** Large touch targets, clear typography, and intuitive DatePickers.
* **Dark Mode:** Native support for high-contrast dark and light themes.

---

## 🛠️ Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Local Database:** Room Persistence Library
* **Architecture:** MVVM (Model-View-ViewModel)
* **Backend (Planned):** Firebase/Supabase for anonymous crowdsourced emissions data.

---

## 🚦 Getting Started
1. Clone the repository.
2. Open in **Android Studio (Ladybug or newer)**.
3. Build and run on an Android device or emulator (API 24+).

---

## ⚖️ Legal & Privacy
* **Data Sovereignty:** MotoÉire does not track your registration numbers or personal insurance details on our servers.
* **Compliance:** This app provides tax estimates for informational purposes. Users are encouraged to verify final figures with the Revenue Commissioners.
* **Trademarks:** MotoÉire is an independent project and is not affiliated with the NCTS, Department of Transport, or any car rental agency.

---

Built with ❤️ in Ireland.