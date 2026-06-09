# Neon POS System

A modern, offline-ready Android Point of Sale (POS) system built with Jetpack Compose. It features a built-in Ktor web server that simultaneously serves two web applications: a restaurant administration dashboard and a customer-facing online ordering system.

## 🌟 Key Features

### Android POS App
- **Modern UI/UX**: Built entirely with Jetpack Compose featuring a sleek "Neon" dark theme.
- **Offline-Ready**: Uses Room Database for local storage, enabling the POS to function without an internet connection.
- **Cart & Checkout**: Advanced shopping cart with support for swipe-to-delete, multi-item checkout, and global discounts.
- **NFC Member Integration**: Native NFC scanning for quick member login and stored-value card payments.
- **Thermal Printer Support**: Seamless integration with ESC/POS network thermal printers for printing receipts and Z-reports.
- **Order Management**: Hold/retrieve orders, cancel orders with automatic member refunds, and generate daily Z-reports.

### Built-in Ktor Web Server
The Android app hosts a lightweight Ktor web server internally, exposing REST APIs to serve local web clients on the same network.

### Web Dashboards
- **Web Admin Dashboard (`/webapp`)**: A React/Vite-based application for restaurant managers to view live orders, manage inventory, and adjust store settings (like tax rates).
- **Customer Online Ordering (`/webapp-customer`)**: A React/Vite-based application allowing customers to place orders from their phones by connecting to the store's local Wi-Fi.

## 🛠 Tech Stack

- **Android App**: Kotlin, Jetpack Compose, Room (SQLite), Coroutines/Flow
- **Embedded Server**: Ktor Server, Kotlinx Serialization
- **Web Applications**: React, TypeScript, Vite, TailwindCSS

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 34
- Node.js & npm (if you plan to modify the web applications)

### Running the Android POS
1. Clone this repository.
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. Run the `app` module on a physical device or emulator.
   *Note: For NFC and Network Printer features, a physical Android device is highly recommended.*

### Running the Web Applications
The web applications are bundled inside the Android app's assets. However, for development:
1. Navigate to `webapp/` or `webapp-customer/`.
2. Run `npm install` to install dependencies.
3. Run `npm run dev` to start the local development server.

## 📁 Project Structure

- `app/src/main/java/.../pos/presentation/`: UI components built with Jetpack Compose.
- `app/src/main/java/.../pos/domain/`: Business logic, Models, and Repository interfaces.
- `app/src/main/java/.../pos/data/`: Data layer, including Room DAOs, Data Seeding, and the Ktor `PosWebServer`.
- `webapp/`: Source code for the manager admin dashboard.
- `webapp-customer/`: Source code for the customer ordering website.

## 📝 License
This project is for demonstration and private use.
