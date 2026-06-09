# Neon POS 系統

這是一款使用 Jetpack Compose 開發的現代化、支援離線操作的 Android 點餐（POS）系統。它內建了一個 Ktor 網頁伺服器，能夠同時提供兩個網頁應用程式：一個是供餐廳管理者使用的後台面板，另一個是供顧客點餐的線上點餐系統。

## 🌟 核心功能

### Android POS 應用程式
- **現代化 UI/UX**：完全使用 Jetpack Compose 打造，並採用極具科技感的「霓虹（Neon）」深色主題。
- **離線支援**：透過 Room Database 進行本地端資料儲存，即使沒有網路連線也能順暢運作。
- **購物車與結帳**：進階的購物車系統，支援滑動刪除商品、多種商品結帳，以及全單折扣功能。
- **NFC 會員整合**：內建 NFC 感應功能，支援快速會員登入及儲值卡扣款。
- **熱感應印表機支援**：無縫整合 ESC/POS 網路熱感應印表機，支援列印顧客收據與每日日結（Z-Report）報表。
- **訂單管理**：支援掛單/取單功能、取消訂單並自動退還會員餘額，以及生成每日營業報表。

### 內建 Ktor 網頁伺服器
Android 應用程式內部自帶一個輕量級的 Ktor 伺服器，對內部區網開放 REST API 來服務本地端的網頁系統。

### 網頁系統
- **管理者後台 (`/webapp`)**：使用 React/Vite 開發的網頁，餐廳經理可在此查看即時訂單、管理商品，以及調整商店設定（例如：修改稅率）。
- **顧客線上點餐 (`/webapp-customer`)**：同樣使用 React/Vite 開發的網頁，顧客只要連上店內的 Wi-Fi，就能透過手機掃描直接進行點餐。

## 🛠 技術架構

- **Android 應用程式**：Kotlin, Jetpack Compose, Room (SQLite), Coroutines/Flow
- **內建伺服器**：Ktor Server, Kotlinx Serialization
- **網頁應用程式**：React, TypeScript, Vite, TailwindCSS

## 🚀 快速開始

### 系統需求
- Android Studio Ladybug 或更新版本
- 最低 SDK 版本：API 26 (Android 8.0)
- 目標 SDK 版本：API 34
- Node.js & npm (若您需要修改網頁端程式碼)

### 執行 Android POS 系統
1. 下載或 Clone 此專案。
2. 使用 Android Studio 開啟專案。
3. 點擊 Sync 同步 Gradle 檔案。
4. 在實體手機或模擬器上執行 `app` 模組。
   *註：強烈建議使用實體的 Android 設備，以獲得完整的 NFC 與網路印表機測試體驗。*

### 執行網頁應用程式
網頁程式碼的編譯檔案已經打包在 Android App 的 assets 裡面。如果您需要進行網頁開發或修改：
1. 切換至 `webapp/` 或 `webapp-customer/` 資料夾。
2. 執行 `npm install` 安裝依賴套件。
3. 執行 `npm run dev` 啟動本地開發伺服器。

## 📁 專案結構說明

- `app/src/main/java/.../pos/presentation/`：使用 Jetpack Compose 建立的使用者介面 (UI) 元件。
- `app/src/main/java/.../pos/domain/`：商業邏輯、資料模型 (Models) 以及 Repository 介面。
- `app/src/main/java/.../pos/data/`：資料層，包含 Room DAOs、預設資料庫種子 (Seeding)，以及 Ktor `PosWebServer`。
- `webapp/`：管理者後台網站的原始碼。
- `webapp-customer/`：顧客線上點餐網站的原始碼。

## 📝 授權說明
本專案為展示及私人用途。
