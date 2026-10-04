# VssMart (VMart) - Campus Marketplace Android App

[![Download APK](https://img.shields.io/badge/Download-APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/Ayush-Shukla-1234/VssMart-Android/releases/latest/download/VssMart.apk)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![APK Size](https://img.shields.io/badge/APK%20Size-4.08%20MB-blue?style=for-the-badge)](https://github.com/Ayush-Shukla-1234/VssMart-Android/releases/latest)

Production-grade native Android marketplace application built in **Java (JDK 17+)** designed for college campus students to buy and sell pre-owned hostel essentials (cycles, coolers, study tables, books, mattresses, kettles, electronics).

---

## 📱 Application Preview

| Feed Screen | Product Details | Add Listing | Profile Screen |
| :---: | :---: | :---: | :---: |
| ![Feed](screenshots/feed.jpeg) | ![Details](screenshots/product_detail.png) | ![Add Listing](screenshots/add_listing.jpeg) | ![Profile](screenshots/profile.jpeg) |

---

## 📥 Direct Download

Get the production APK directly on your Android device:

- **Latest Release:** [Download VssMart.apk](https://github.com/Ayush-Shukla-1234/VssMart-Android/releases/latest/download/VssMart.apk)
- **All Releases & Release Notes:** [GitHub Releases Page](https://github.com/Ayush-Shukla-1234/VssMart-Android/releases)

---

## 🏛️ Architecture & Tech Stack

- **Language**: Native Java (JDK 17+)
- **Architecture**: Strict MVVM (Model-View-ViewModel) + Repository Pattern
- **UI Toolkit**: Material Design 3 (MD3), ConstraintLayout, CardView, BottomNavigationView, ViewBinding
- **State Management**: LiveData & MediatorLiveData observables with `Resource<T>` wrapper
- **Authentication**: Firebase Authentication (Google Sign-In & Firebase Phone OTP with `PhoneAuthProvider`)
- **Database**: Cloud Firestore (`users`, `listings`, `feedback`)
- **Storage & Caching**: Firebase Storage with Bumptech Glide 4.16 image caching and fallback handling
- **Direct Buyer-Seller Chat**: Native WhatsApp Intent API with Indian phone number sanitization (+91 normalization) and pre-filled product inquiries
- **Binary Optimization**: ProGuard/R8 dead-code and resource stripping (~4.08 MB distribution size)

---

## 📂 Project Structure

```text
io.mastercoding.vssmart/
├── data/
│   ├── model/
│   │   ├── UserModel.java              // User profile schema (UID, name, email, phone, hostel, room)
│   │   ├── ListingModel.java           // Product listing schema (ID, title, price, category, hostel, WhatsApp phone, isSold, timestamp)
│   │   └── FeedbackModel.java          // Feedback & query model
│   └── repository/
│       ├── AuthRepository.java         // Google Auth, Phone OTP, and session verification
│       ├── ListingRepository.java      // Firestore listing CRUD, real-time snapshot queries, filtering
│       └── UserRepository.java         // Firestore profile fetch, update, and local cache
├── viewmodel/
│   ├── AuthViewModel.java              // Handles login states, OTP trigger/verification, session state
│   ├── ListingViewModel.java           // Exposes LiveData for active listings, category chips, search queries, upload states
│   └── UserViewModel.java              // Exposes LiveData for user profile sync, my listings, updates
├── view/
│   ├── splash/
│   │   └── SplashActivity.java         // "VMart" -> "VssMart" logo expansion animation & session auto-route
│   ├── auth/
│   │   ├── AuthActivity.java           // Google Sign-In & Phone OTP Material bottom sheets/screens
│   │   └── OtpVerificationDialog.java  // 6-digit OTP entry dialog
│   ├── main/
│   │   ├── MainActivity.java           // Hosts BottomNavigationView & Fragment transitions
│   │   ├── MarketFragment.java         // Tab 1: Search bar, category filter chips, 2-column grid feed
│   │   ├── AddListingFragment.java     // Tab 2: Image picker, mandatory WhatsApp input, upload form
│   │   └── YouFragment.java            // Tab 3: Editable campus profile (hostel, room), My Listings, Settings
│   ├── detail/
│   │   └── ListingDetailActivity.java  // Full item specs & WhatsApp intent redirect with pre-filled text
│   ├── support/
│   │   ├── HelpFeedbackActivity.java   // FAQs & Firestore feedback submission
│   │   └── AboutActivity.java          // Developer profile, college info, GitHub links
│   └── adapter/
│       ├── ListingAdapter.java         // Clean ViewHolder with Glide binding & click listeners
│       ├── MyListingsAdapter.java      // User's own items with "Mark as Sold" / "Delete" actions
│       └── CategoryChipAdapter.java    // Horizontal category filter chips
└── utils/
    ├── Constants.java                  // Firestore collection names, category list, Intent extras
    ├── WhatsAppHelper.java             // Sanitizes Indian phone numbers (+91) & builds URI intents
    ├── Resource.java                   // Generic UI state wrapper (SUCCESS, ERROR, LOADING, CODE_SENT)
    └── SharedPrefManager.java          // Offline session & campus profile caching