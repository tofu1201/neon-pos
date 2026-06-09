# 手持 POS App 超詳細打包與安裝教學

這份教學的目標是讓你把目前這個 Android POS 專案，從開發機打包成 release APK / AAB，再安裝到 Android 手持終端機。

## 先決條件

在開始之前，請確認你已經準備好：

1. 一台 macOS / Windows / Linux 開發電腦。
2. Android Studio 最新穩定版。
3. JDK 17。
4. Android SDK Platform 35。
5. 一台可連 adb 的 Android 手持終端機。
6. 如果要正式上線，請先確認印表機服務已安裝在終端機上，服務包名是 `com.incar.printerservice`。

## 目前專案結構重點

這個專案已經具備：

1. Compose UI。
2. Room Database。
3. NFC ReaderMode 與 NDEF 解析。
4. PrinterClient AIDL 整合。
5. release signingConfig 與 `keystore.properties` 範本。
6. Debug / Release 分流設定。

---

## 1. 安裝開發環境

### 1.1 安裝 Android Studio

1. 到 Android Studio 官網下載最新版本。
2. 安裝完成後開啟 Android Studio。
3. 第一次啟動時選擇標準設定即可。

### 1.2 安裝 JDK 17

如果你的電腦還沒有 JDK 17，需要先安裝。

macOS 可用以下方式：

```bash
brew install --cask temurin17
```

安裝後確認：

```bash
java -version
```

你應該會看到 17.x 的版本資訊。

### 1.3 安裝 Android SDK

在 Android Studio 中：

1. 打開 `Settings` 或 `Preferences`。
2. 進入 `Android SDK`。
3. 確認已安裝：
   - Android SDK Platform 35
   - Build-Tools 35.x
   - Platform-Tools
   - Command-line Tools

---

## 2. 準備 release 簽章

### 2.1 建立 keystore

如果你還沒有 release keystore，可以用 keytool 建立：

```bash
keytool -genkeypair -v \
  -keystore release-keystore.jks \
  -alias pos-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

系統會要求你輸入密碼與基本資訊。

### 2.2 建立 keystore.properties

專案根目錄已提供範本：

- [keystore.properties.example](../keystore.properties.example)

請複製一份成 `keystore.properties`，內容像這樣：

```properties
storeFile=/absolute/path/to/release-keystore.jks
storePassword=your-store-password
keyAlias=pos-release
keyPassword=your-key-password
```

注意事項：

1. `storeFile` 必須是絕對路徑。
2. `keystore.properties` 不要提交到 Git。
3. `.jks` 請備份至少兩份。

---

## 3. release 打包流程

### 3.1 用 Android Studio 打包

1. 打開專案。
2. 等 Gradle sync 完成。
3. 確認選到 `release` 變體。
4. 執行：
   - `Build > Generate Signed Bundle / APK`
5. 選擇：
   - `APK`：如果要直接裝到手持終端機。
   - `Android App Bundle`：如果要交給 Play Console 或企業分發平台。
6. 選擇你的 keystore 與 alias。
7. 完成後產出 signed APK / AAB。

### 3.2 用命令列打包

如果你要用命令列，先進專案根目錄：

```bash
cd /Users/tofu/Documents/pos
```

打 release APK：

```bash
./gradlew clean assembleRelease
```

打 release AAB：

```bash
./gradlew clean bundleRelease
```

產物位置：

- APK: `app/build/outputs/apk/release/`
- AAB: `app/build/outputs/bundle/release/`

### 3.3 檢查是否簽章成功

你可以用 Android Studio 的 APK Analyzer，或用 `apksigner`：

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

如果有看到證書資訊，代表簽章正常。

---

## 4. 安裝到 Android 手持終端機

### 4.1 開啟開發者模式

在手持終端機上：

1. 進入 `設定`。
2. 找到 `關於裝置`。
3. 連續點擊 `版本號` 或 `Build number` 直到開啟開發者模式。
4. 回到設定，打開：
   - `USB debugging`
   - 如果需要，開啟 `Install via USB` 或 `Allow unknown apps`

### 4.2 用 adb 安裝

先確認電腦可看到設備：

```bash
adb devices
```

如果有出現設備序號，代表連線成功。

安裝 release APK：

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

升級時同樣用：

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

### 4.3 手動安裝

如果現場不能用 adb：

1. 把 release APK 複製到終端機。
2. 用檔案管理器點開 APK。
3. 允許安裝未知來源。
4. 完成安裝。

### 4.4 用 MDM / 企業平台推送

如果你們有 MDM 或企業 app store：

1. 上傳 signed APK 或 AAB。
2. 指派到指定設備群組。
3. 推送更新。
4. 在設備端確認版本號已更新。

---

## 5. 上線前測試清單

正式交機前，請至少測這些：

1. App 是否能正常啟動。
2. 商品是否有自動 seed 出來。
3. NFC 會員卡是否能讀到會員資訊。
4. 商品 NFC 標籤是否能解析 SKU / 名稱。
5. 印表機是否能連線。
6. 收據是否能列印：
   - 店名
   - 日期時間
   - 會員資訊
   - 折扣
   - 收款金額
   - 找零
   - QR Code
7. 缺紙時是否有友善提示。
8. 印表機斷線後是否會自動重連。
9. 離線情況下是否仍能建立本地訂單。

---

## 6. 正式發版建議

### 6.1 每次版本都更新 versionCode

在 [app/build.gradle.kts](../app/build.gradle.kts) 的 `defaultConfig` 裡，每次正式發版都把：

- `versionCode` 往上加
- `versionName` 改成對應版本

例如：

```kotlin
versionCode = 2
versionName = "1.1.0"
```

### 6.2 不要用 debug key 上線

release 必須用正式 keystore 簽章。

### 6.3 保留 keystore 備份

keystore 一旦遺失，以後就無法用同一簽章更新現有安裝包，這對企業部署很重要。

---

## 7. 常見問題

### 7.1 `java runtime not found`

代表你的電腦沒有安裝 JDK，先安裝 JDK 17。

### 7.2 `Missing keystore.properties`

代表你還沒建立正式簽章設定檔。請複製 `keystore.properties.example`。

### 7.3 `INSTALL_FAILED_VERSION_DOWNGRADE`

代表你要安裝的版本號比設備上的版本還小。把 `versionCode` 調高。

### 7.4 `INSTALL_PARSE_FAILED_NO_CERTIFICATES`

代表 APK 沒有正確簽章。重新用 signed release 打包。

### 7.5 印表機連不上

請確認：

1. 終端機上真的有安裝 `com.incar.printerservice`。
2. `AndroidManifest.xml` 有加 `<queries>`。
3. 設備系統版本與 vendor service 相容。

---

## 8. 建議的實戰流程

如果你現在要實際做一次完整流程，建議照這樣跑：

1. 安裝 JDK 17。
2. 建立 `release-keystore.jks`。
3. 建立 `keystore.properties`。
4. 執行 `./gradlew clean assembleRelease`。
5. 取得 `app-release.apk`。
6. 用 `adb install -r` 裝到手持終端機。
7. 打開 App，測 NFC 與印表機。
8. 若一切正常，再做 `bundleRelease` 給企業部署平台。

---

## 9. 如果你要量產部署

建議再做這些：

1. 把 `keystore.properties` 放到安全管理。
2. 用 CI/CD 產出 release APK/AAB。
3. 每次 build 自動更新版本號。
4. 用 MDM 管理手持終端機更新。
5. 建立測試 SOP，避免現場更新失敗。

---

## 10. 最短可執行命令摘要

```bash
cd /Users/tofu/Documents/pos
./gradlew clean assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

如果你是第一次做 release，先確認：

1. JDK 17 已安裝。
2. `keystore.properties` 已建立。
3. Android Studio 已 sync 成功。
