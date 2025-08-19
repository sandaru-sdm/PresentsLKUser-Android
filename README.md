
<div align="center">
	<img src="app/src/main/res/drawable/logo.png" alt="PresentsLK Logo" width="120"/>
</div>

# 🎁 PresentsLK

PresentsLK is an Android application for shopping and user management, providing a comprehensive e-commerce experience for users.

---


## ✨ Features

- 🔐 **User Authentication**: Seamless login, registration, and password recovery
- 🛒 **Product Browsing**: Explore products with descriptions & images
- 🛍️ **Shopping Cart**: Add items and checkout easily
- 📦 **Order Management**: Track and view order history
- 👤 **User Profile**: Update personal details & profile picture
- 💖 **Wishlist**: Save favorite items for later
- 🔔 **Notifications**: Order status updates


## 🗂️ Project Structure

```text
PresentsLK/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sdm/presentslk/   # Main app logic & activities
│   │   │   ├── res/                       # UI resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml        # App configuration & permissions
│   │   ├── test/                          # Unit tests
│   │   └── androidTest/                   # Instrumentation tests
├── gradle/                                # Gradle wrapper files
├── build.gradle.kts                       # Project-level build config
├── settings.gradle.kts                    # Project settings
└── .gitignore                             # Git config
```

### 🏷️ Main Activities
- `MainActivity`: Entry point, login/register navigation
- `LoginActivity`: User login and password management
- `HomeViewActivity`: Dashboard, product browsing, notifications
- `CartActivity`, `WishlistActivity`, `OrdersActivity`, `UserProfileActivity`, etc.

### 🛡️ Permissions
- `INTERNET`: Required for network access
- `POST_NOTIFICATIONS`: Required for push notifications

### 📱 SDK Info
- **Min SDK**: 24
- **Target SDK**: 34


## 🚀 Installation & Usage

### Prerequisites
- 🧑‍💻 Android Studio
- 🐙 Git
- 📦 Android SDK

### Steps
1. Clone the repository:
	```bash
	git clone https://github.com/sandaru-sdm/PresentsLKUser-Android.git
	```
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or device

### 🛠️ Build & Test
- Build: Use Android Studio or run `./gradlew build`
- Unit tests: `app/src/test/java`
- Instrumentation tests: `app/src/androidTest/java`


## 📦 Dependencies

Key libraries:
- Google Play Services
- Android Jetpack Components
- Firebase (Authentication & Database)

#### Full Dependency List (from `app/build.gradle.kts`):
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.10.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`
- `androidx.swiperefreshlayout:swiperefreshlayout:1.1.0`
- `com.google.firebase:firebase-auth:22.3.0`
- `com.google.firebase:firebase-database:20.3.0`
- `com.google.firebase:firebase-firestore:24.9.1`
- `com.google.firebase:firebase-storage:20.3.0`
- `com.google.android.gms:play-services-maps:17.0.1`
- `com.google.android.gms:play-services-auth:20.7.0`
- `com.google.code.gson:gson:2.8.9`
- `com.squareup.picasso:picasso:2.8`
- `de.hdodenhof:circleimageview:3.1.0`
- `junit:junit:4.13.2` (unit tests)
- `androidx.test.ext:junit:1.1.5` (instrumentation tests)
- `androidx.test.espresso:espresso-core:3.5.1` (instrumentation tests)


## 🤝 Contributing

We welcome contributions! Please follow these steps:
1. 🍴 Fork the repository
2. 🌱 Create a new branch for your feature or bug fix
3. 💬 Commit your changes with clear messages
4. 🔀 Submit a pull request

### Guidelines
- 📚 Follow Android best practices
- 📝 Document your code
- 🧪 Write unit tests for new features
- 🎨 Maintain consistent code style
- 🌳 Use feature/bugfix branches (e.g., `feature/your-feature`)


## 📄 License

This project is licensed under the MIT License.


## 📬 Contact

For any queries or support, please open an issue in the GitHub repository or contact the maintainers.
