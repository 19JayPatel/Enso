<div align="center">

<img src="app/src/main/res/drawable/splash_logo.png" alt="Enso Logo" width="120"/>

# ✂️ Enso

### *Your Premier Salon Booking Experience*

**Enso** is a full-stack Android marketplace app that connects customers with verified salons — enabling seamless service discovery, stylist selection, appointment booking, and real-time salon management across three distinct user roles.

<br/>

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-33.7.0-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Status](https://img.shields.io/badge/Status-Active-success?style=for-the-badge)](https://github.com/19JayPatel/Enso)
[![Version](https://img.shields.io/badge/Version-1.0-blue?style=for-the-badge)](https://github.com/19JayPatel/Enso/releases)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

<br/>

[📱 Features](#-features) • [🏗️ Architecture](#%EF%B8%8F-architecture) • [🛠️ Tech Stack](#%EF%B8%8F-tech-stack) • [📁 Folder Structure](#-folder-structure) • [🔥 Firebase Structure](#-firebase-database-structure) • [🚀 Installation](#-installation-guide) • [🗺️ Roadmap](#%EF%B8%8F-future-roadmap)

</div>

---

## 📖 Project Overview

**Enso** is a multi-role Android salon booking platform built with **Kotlin** and powered by **Firebase Realtime Database**. It bridges the gap between beauty consumers and salon businesses through a streamlined, role-based mobile experience.

### 🎯 Problem It Solves

> Finding and booking salon appointments is still largely manual — phone calls, walk-ins, no visibility into stylist availability or services. For salon owners, managing bookings and showcasing services is fragmented and inefficient.

**Enso solves this by:**
- Giving **customers** a seamless discovery-to-booking flow with stylist selection, date/time picking, and digital receipts with QR codes
- Empowering **salon owners** with a dedicated dashboard to manage services, track bookings, and monitor upcoming appointments
- Providing **admins** with a centralized control panel for platform governance, salon approval workflows, and user management

### 👥 User Roles

| Role | Access Level | Entry Point |
|------|-------------|-------------|
| 🧑‍💼 **Admin** | Platform-wide control | `AdminMainActivity` |
| 💈 **Salon Owner** | Salon-scoped management | `SalonOwnerMainActivity` |
| 👤 **Customer** | Service discovery & booking | `MainActivity` |

---

## ✨ Features

### 👤 Customer Features

- 🏠 **Home Feed** — Browse approved, active salons with name, location, and rating displayed in a real-time RecyclerView feed
- 🔍 **Search Salons** — Full-text search with keyword-based salon filtering powered by Firebase queries; recent search history UI included
- 📋 **Salon Details** — Detailed salon view with services, stylist list, address, and ratings
- 🗺️ **Map View** — Visual map screen with filter bottom sheet for location-aware browsing
- 💇 **Stylist Selection** — Browse and choose from available stylists at a selected salon
- 📅 **Date & Time Picker** — Interactive date carousel adapter and time slot grid for appointment scheduling
- ✅ **Booking Confirmation** — Full booking summary screen with service details, duration, pricing breakdown, subtotal, and discount
- 🧾 **Digital Receipt** — Auto-generated booking receipt with dynamic QR code (via ZXing) fetched from Firebase
- 📆 **My Bookings** — Tabbed view of Upcoming, Completed, and Cancelled bookings with real-time Firebase sync
- ❤️ **Favourites** — Save and manage favourite salons with a remove-via-bottom-sheet confirmation dialog
- 👤 **Profile Management** — View name, email, and profile initial; edit profile details; secure logout
- 🔒 **Session Guard** — Automatic redirect to login if Firebase Auth session is expired or null

---

### 💈 Salon Owner Features

- 📊 **Owner Dashboard** — Real-time overview of upcoming appointments with salon name and location display
- 🛎️ **Service Management** — Full CRUD flow for salon services across a multi-step wizard:
  - Step 1: Add service name, description, and category (Hair, Nails, Skin Care, Spa & Wellness, Makeup)
  - Step 2: Set pricing and duration
  - Step 3: Review before publishing
  - ✅ Publish confirmation screen on success
- 🔄 **Service Toggle** — Enable/disable individual services with a live switch directly from the services list
- 📅 **Bookings View** — See all customer bookings associated with the owner's salon
- 👤 **Owner Profile** — View and manage salon owner profile details
- 🔐 **Approval Gate** — Account status (`pending` / `active`) enforced at login; new owners are redirected to complete salon registration before gaining dashboard access

---

### 🧑‍💼 Admin Features

- 📈 **Admin Dashboard** — Live platform statistics: total bookings, registered salons, and total users — all fetched in real-time from Firebase
- 🏪 **Salon Management** — Full salon listing with filter chips: All, Active, Pending, Suspended
  - Approve or suspend salon registrations
  - View salon details including owner name, booking count, services count, and rating
- 👥 **User Management** — Complete user list with role and status summaries:
  - Counts for Total, Active, Inactive, and Banned users
  - Per-user role badge display
- ➕ **Add Salon** — Admin can manually register salons on behalf of owners with full form: salon name, description, category, owner details, and full address (street, city, state, zip, country)
- 👤 **Admin Profile** — Dedicated admin profile view
- 🔒 **Role-Based Routing** — Login flow routes users to the correct portal (`admin`, `customer`, `salon_owner`) based on their Firebase role field

---

## 🏗️ Architecture

Enso follows a **component-based Activity + Fragment architecture** organized by user role. While it does not use MVVM with ViewModels, it applies several clean architecture principles including a singleton session manager, Firebase-backed real-time data binding, and ViewBinding throughout the admin and owner modules.

```
┌────────────────────────────────────────────────────────────────┐
│                        ENSO APPLICATION                        │
├─────────────────┬──────────────────────┬───────────────────────┤
│   AUTH LAYER    │   CUSTOMER PORTAL    │   OWNER / ADMIN       │
│                 │                      │   PORTALS             │
│  SplashActivity │  MainActivity        │  SalonOwnerMain       │
│  LoginActivity  │  ├── HomeFragment    │  AdminMainActivity    │
│  SignupActivity │  ├── SearchFragment  │                       │
│                 │  ├── BookingsFragment│  Fragment-based tabs  │
│  Role Router ──►│  ├── HeartFragment   │  for dashboard,       │
│  (Firebase)     │  └── ProfileFragment │  services, bookings,  │
│                 │                      │  profile, users,      │
│                 │  + Activity Stack:   │  and salons           │
│                 │  SalonDetails        │                       │
│                 │  ChooseStylist       │                       │
│                 │  DateTime            │                       │
│                 │  ConfirmBooking      │                       │
│                 │  Receipt             │                       │
└─────────────────┴──────────────────────┴───────────────────────┘
                              │
                    ┌─────────▼──────────┐
                    │  BookingSession    │
                    │  Manager (Singleton│
                    │  across booking   │
                    │  flow steps)       │
                    └─────────┬──────────┘
                              │
                    ┌─────────▼──────────┐
                    │  Firebase Realtime │
                    │  Database          │
                    │  Auth (Email/Pass) │
                    └────────────────────┘
```

### 🔑 Key Architecture Decisions

| Pattern | Implementation |
|---------|---------------|
| **Navigation** | Fragment transactions with `supportFragmentManager` + `BottomNavigationView` per role portal |
| **State Management** | `BookingSessionManager` singleton holds multi-step booking state across Activities |
| **Data Binding** | ViewBinding enabled for Admin and Owner modules; `findViewById` used in Customer |
| **Real-time Data** | `addValueEventListener` for live updates on bookings, salons, and users |
| **Security** | Role-based login routing + owner approval gate + Firebase Auth session checks |
| **Animations** | Splash screen uses `ObjectAnimator` + `AnimatorSet`; screen transitions use custom `anim/` XML resources |

---

## 🛠️ Tech Stack

| Technology | Version | Usage |
|------------|---------|-------|
| **Kotlin** | 2.0.21 | Primary development language |
| **Android SDK** | API 24–35 | Minimum Android 7.0 (Nougat) support |
| **XML Layouts** | — | All UI built with XML (no Compose) |
| **Firebase Auth** | BOM 33.7.0 | Email/password authentication |
| **Firebase Realtime DB** | BOM 33.7.0 | Live data sync for bookings, salons, users, services |
| **Glide** | 4.16.0 | Image loading and caching |
| **ZXing Android Embedded** | 4.3.0 | QR code generation for booking receipts |
| **Material Components** | 1.12.0 | MaterialButton, MaterialCardView, BottomNavigationView, BottomSheetDialog |
| **ViewBinding** | — | Type-safe view access (enabled in Admin & Owner modules) |
| **RecyclerView** | — | All list UIs — salons, services, bookings, users |
| **ConstraintLayout** | 2.2.0 | Primary layout for complex screens |
| **AndroidX AppCompat** | 1.7.0 | Backward-compatible Activity and Fragment support |
| **AndroidX Core KTX** | 1.15.0 | Kotlin extensions for Android framework APIs |
| **Coroutines (via lifecycleScope)** | — | Async splash delay |
| **Gradle Version Catalog** | — | Centralized dependency management via `libs.versions.toml` |

---

## 📁 Folder Structure

```
Enso/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/enso/
│           │   │
│           │   ├── 🔐 auth/
│           │   │   ├── SplashActivity.kt          # Animated entry + Firebase session check
│           │   │   ├── LoginActivity.kt            # Role-based login with Firebase Auth
│           │   │   └── SignupActivity.kt           # Registration with role selection
│           │   │
│           │   ├── 🧑‍💼 admin/
│           │   │   ├── activities/
│           │   │   │   ├── AdminMainActivity.kt   # Admin shell with BottomNav
│           │   │   │   └── AddSalonActivity.kt    # Manual salon registration form
│           │   │   ├── adapters/
│           │   │   │   └── SalonAdapter.kt        # RecyclerView adapter for salon list
│           │   │   ├── fragments/
│           │   │   │   ├── AdminDashboardFragment.kt  # Live stats grid
│           │   │   │   ├── AdminSalonsFragment.kt     # Salon management + filter chips
│           │   │   │   ├── AdminUsersFragment.kt      # User list with status summary
│           │   │   │   ├── AdminProfileFragment.kt    # Admin profile
│           │   │   │   └── UsersDashboardFragment.kt  # Users dashboard view
│           │   │   └── models/
│           │   │       ├── SalonModel.kt          # Salon data model
│           │   │       └── UserModel.kt           # User data model
│           │   │
│           │   ├── 💈 owner/
│           │   │   ├── activities/
│           │   │   │   ├── SalonOwnerMainActivity.kt      # Owner shell with BottomNav
│           │   │   │   ├── SalonOwnerDashboardActivity.kt # Dashboard activity entry
│           │   │   │   ├── AddNewServiceActivity.kt        # Step 1: Name + Category
│           │   │   │   ├── AddServicePricingActivity.kt    # Step 2: Price + Duration
│           │   │   │   ├── AddServiceReviewActivity.kt     # Step 3: Review before publish
│           │   │   │   └── ServicePublishedActivity.kt     # Publish success confirmation
│           │   │   ├── adapters/
│           │   │   │   ├── OwnerBookingsAdapter.kt         # Owner bookings RecyclerView
│           │   │   │   └── UpcomingAppointmentAdapter.kt   # Dashboard appointments list
│           │   │   ├── fragments/
│           │   │   │   ├── SalonOwnerDashboardFragment.kt  # Upcoming appointments
│           │   │   │   ├── SalonOwnerServicesFragment.kt   # Service list + toggle
│           │   │   │   ├── SalonOwnerBookingsFragment.kt   # All bookings view
│           │   │   │   └── SalonOwnerProfileFragment.kt    # Owner profile
│           │   │   └── models/
│           │   │       └── ServiceModel.kt                 # Service data model
│           │   │
│           │   ├── 👤 customer/
│           │   │   ├── activities/
│           │   │   │   ├── MainActivity.kt            # Customer shell with BottomNav
│           │   │   │   ├── SalonDetailsActivity.kt    # Salon detail view
│           │   │   │   ├── AllSalonActivity.kt        # Browse all salons
│           │   │   │   ├── SearchResultActivity.kt    # Keyword search results
│           │   │   │   ├── ChooseStylistActivity.kt   # Stylist selector
│           │   │   │   ├── DateTimeActivity.kt        # Date & time picker
│           │   │   │   ├── ConfirmBookingActivity.kt  # Booking summary + confirm
│           │   │   │   ├── ReceiptActivity.kt         # Digital receipt with QR code
│           │   │   │   └── MapViewActivity.kt         # Map view + filter bottom sheet
│           │   │   ├── adapters/
│           │   │   │   ├── CustomerBookingsAdapter.kt # Customer booking cards
│           │   │   │   ├── DateAdapter.kt             # Horizontal date carousel
│           │   │   │   ├── StylistAdapter.kt          # Stylist list cards
│           │   │   │   └── TimeSlotAdapter.kt         # Time slot grid
│           │   │   ├── fragments/
│           │   │   │   ├── HomeFragment.kt            # Home feed with salon list
│           │   │   │   ├── SearchFragment.kt          # Search input + recent history
│           │   │   │   ├── SalonDetailsFragment.kt    # Salon details in-app fragment
│           │   │   │   ├── BookingsFragment.kt        # Tabbed bookings view
│           │   │   │   ├── HeartFragment.kt           # Favourites list
│           │   │   │   ├── ProfileFragment.kt         # Customer profile + logout
│           │   │   │   ├── EditProfileFragment.kt     # Profile editing
│           │   │   │   ├── CalendarFragment.kt        # Calendar view
│           │   │   │   └── CalendarPopUpSheet.kt      # Calendar bottom sheet
│           │   │   ├── models/
│           │   │   │   └── BookingModel.kt            # Booking data model
│           │   │   └── BookingSessionManager.kt       # Singleton booking session state
│           │   │
│           │   └── EnsoApplication.kt                 # Application class — Firebase init
│           │
│           └── res/
│               ├── layout/        # All XML screen layouts
│               ├── drawable/      # Icons, backgrounds, vector assets
│               ├── anim/          # Slide and fade transition animations
│               ├── color/         # Selector-based color state lists
│               ├── menu/          # Bottom navigation menu XML files
│               ├── values/        # Colors, strings, themes, styles
│               └── xml/           # Backup and data extraction rules
│
├── gradle/
│   └── libs.versions.toml         # Centralized version catalog
├── app/build.gradle.kts           # App-level Gradle with dependencies
├── build.gradle.kts               # Project-level Gradle
├── settings.gradle.kts
├── FILE_STRUCTURE.md
└── PROJECT_STRUCTURE.md
```

---

## 🔥 Firebase Database Structure

Enso uses **Firebase Realtime Database** with the following node structure (inferred from source code):

```
📦 Firebase Realtime Database
│
├── 👥 Users
│   └── {uid}
│       ├── name: String
│       ├── email: String
│       ├── role: "customer" | "salon_owner" | "admin"
│       ├── status: "active" | "pending" | "banned"
│       ├── phone: String
│       ├── dob: String
│       ├── gender: String
│       ├── location: String
│       ├── salonId: String            ← linked salon (for owners)
│       └── createdAt: Long (timestamp)
│
├── 🏪 Salons
│   └── {salonId}
│       ├── salonName: String
│       ├── description: String
│       ├── category: String
│       ├── ownerId: String
│       ├── ownerName: String
│       ├── status: "active" | "pending" | "suspended"
│       ├── rating: String             ← e.g. "4.7 (312)"
│       ├── address
│       │   ├── street: String
│       │   ├── city: String
│       │   ├── state: String
│       │   ├── zip: String
│       │   └── country: String
│       └── imageUrl: String
│
├── 🛎️ Services
│   └── {serviceId}
│       ├── serviceId: String
│       ├── ownerId: String
│       ├── salonId: String
│       ├── serviceName: String
│       ├── category: String           ← Hair | Nails | Skin Care | Spa & Wellness | Makeup
│       ├── description: String
│       ├── price: String
│       ├── duration: String           ← in minutes
│       ├── status: "active" | "inactive"
│       └── createdAt: Long
│
└── 📅 Bookings
    └── {bookingId}
        ├── bookingId: String
        ├── customerId: String
        ├── customerName: String
        ├── salonId: String
        ├── ownerId: String
        ├── serviceId: String
        ├── serviceName: String
        ├── salonName: String
        ├── salonImageUrl: String
        ├── bookingDate: String
        ├── bookingTime: String
        ├── price: String
        ├── duration: String
        ├── status: "upcoming" | "completed" | "cancelled"
        └── createdAt: Long
```

> **Security Note:** Only salons with `status = "active"` are shown to customers. New salon owners are placed in `status = "pending"` until approved by an admin. This ensures marketplace integrity.

---

## 🚀 Installation Guide

### Prerequisites

- Android Studio **Hedgehog (2023.1.1)** or later
- JDK 11+
- An active [Firebase](https://console.firebase.google.com/) project
- Android device or emulator running **API 24+**

### Step-by-Step Setup

**1. Clone the Repository**
```bash
git clone https://github.com/19JayPatel/Enso.git
cd Enso
```

**2. Open in Android Studio**
```
File → Open → Select the cloned Enso/ folder
```

**3. Configure Firebase**

> The repository includes an existing `google-services.json`. To use your own Firebase project:

- Go to [Firebase Console](https://console.firebase.google.com/) and create a new project
- Add an Android app with package name: `com.example.enso`
- Download the `google-services.json` file
- Replace `app/google-services.json` with your downloaded file
- Enable **Authentication** → Email/Password sign-in method
- Enable **Realtime Database** and set rules to allow authenticated reads/writes:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

**4. Sync Gradle**
```
Click "Sync Now" when prompted in Android Studio
```
Or via terminal:
```bash
./gradlew build
```

**5. Seed an Admin Account**

Since admin accounts cannot be created via the app UI, manually add an admin user in Firebase Realtime Database:
```json
"Users": {
  "<your-firebase-uid>": {
    "name": "Admin",
    "email": "admin@enso.com",
    "role": "admin",
    "status": "active"
  }
}
```

**6. Run the App**
```
Run → Select Device → ▶ Run 'app'
```

---

## 📱 App Screens

> 📸 Screenshots coming soon — contribute by opening a PR with screen captures!

| Screen | Role | Description |
|--------|------|-------------|
| Splash Screen | All | Animated logo with fade + scale entry |
| Login / Signup | All | Role selector: Customer or Salon Owner |
| Home | Customer | Live salon feed from Firebase |
| Search | Customer | Keyword search with recent history |
| Salon Details | Customer | Services, stylists, location |
| Map View | Customer | Salon map with filter bottom sheet |
| Choose Stylist | Customer | Stylist selection cards |
| Date & Time | Customer | Date carousel + time slot grid |
| Confirm Booking | Customer | Summary with pricing breakdown |
| Receipt | Customer | Digital receipt with QR code |
| My Bookings | Customer | Tabbed: Upcoming / Completed / Cancelled |
| Favourites | Customer | Saved salons with remove dialog |
| Profile | Customer | User info + edit + logout |
| Owner Dashboard | Owner | Upcoming appointments list |
| Services | Owner | Service list with enable/disable toggle |
| Add Service | Owner | 3-step wizard: Details → Pricing → Review → Publish |
| Owner Bookings | Owner | All incoming bookings |
| Admin Dashboard | Admin | Live stats: Bookings, Salons, Users |
| Manage Salons | Admin | Filter + approve / suspend salons |
| Manage Users | Admin | User list with role and status |
| Add Salon | Admin | Manual salon registration form |

---

## 🗺️ Future Roadmap

| Feature | Priority | Description |
|---------|----------|-------------|
| 🔔 Push Notifications | High | Notify customers when bookings are confirmed/updated |
| 💳 Payment Integration | High | In-app payments via Razorpay / Stripe |
| ⭐ Reviews & Ratings | High | Post-booking customer reviews for salons and stylists |
| 🗓️ Real Availability Engine | High | Dynamic slot generation based on stylist schedules |
| 📍 Google Maps Integration | Medium | Interactive map with real salon markers |
| 🖼️ Firebase Storage | Medium | Upload real salon and stylist profile images |
| 📱 MVVM Refactor | Medium | Migrate to ViewModel + LiveData / StateFlow for cleaner architecture |
| 🔍 Advanced Search Filters | Medium | Filter by category, rating, distance, and price |
| 📊 Owner Analytics | Medium | Revenue charts and booking trends for salon owners |
| 🌙 Dark Mode | Low | Full dark theme across all role portals |
| 🌐 Multi-language Support | Low | Localization (i18n) for regional markets |
| 📥 Receipt Download | Low | Save and share digital receipts as PDF |
| 🤖 AI Stylist Recommendations | Future | ML-based service and stylist suggestions |

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/YourFeature`
3. Commit your changes: `git commit -m 'Add: YourFeature'`
4. Push to the branch: `git push origin feature/YourFeature`
5. Open a Pull Request

Please ensure your code follows the existing package and naming conventions.

---

## 👨‍💻 Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/19JayPatel">
        <img src="https://github.com/19JayPatel.png" width="80px;" alt="Jay Patel"/><br />
        <sub><b>Jay Patel</b></sub>
      </a><br/>
      <sub>Creator & Lead Developer</sub>
    </td>
  </tr>
</table>

---

## 📄 License

```
MIT License

Copyright (c) 2024 Jay Patel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 📬 Contact

**Jay Patel**

[![GitHub](https://img.shields.io/badge/GitHub-19JayPatel-181717?style=flat-square&logo=github)](https://github.com/19JayPatel)

> Have a feature request or found a bug? [Open an issue](https://github.com/19JayPatel/Enso/issues) on GitHub.

---

<div align="center">

**Built with ❤️ using Kotlin & Firebase**

*Enso — Where Style Meets Simplicity*

⭐ **Star this repo if you found it helpful!** ⭐

</div>
