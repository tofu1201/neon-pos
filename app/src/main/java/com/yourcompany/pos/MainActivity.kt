package com.yourcompany.pos

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourcompany.pos.di.PosAppContainer
import com.yourcompany.pos.presentation.pos.PosEvent
import com.yourcompany.pos.presentation.pos.PosScreen
import com.yourcompany.pos.presentation.theme.PosTheme
import com.yourcompany.pos.presentation.pos.PosViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: PosAppContainer
    private lateinit var nfcManager: com.yourcompany.pos.nfc.NfcManager

    private var barcodeBuffer = StringBuilder()
    private var lastBarcodeTime: Long = 0
    private var posViewModelInstance: PosViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screen sleep
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        appContainer = (application as PosApplication).container
        nfcManager = com.yourcompany.pos.nfc.NfcManager(this)

        val serviceIntent = Intent(this, com.yourcompany.pos.data.remote.PosWebServerService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            PosTheme {
                val posViewModel = viewModel<PosViewModel>(factory = appContainer.getViewModelFactory(nfcManager))
                posViewModelInstance = posViewModel

                val state by posViewModel.uiState.collectAsStateWithLifecycle()

                com.yourcompany.pos.presentation.navigation.PosAppNavGraph(
                    posViewModel = posViewModel
                )
            }
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val pressedKey = event.unicodeChar.toChar()
            val time = System.currentTimeMillis()
            
            // Standard barcode scanners act like fast keyboards
            if (time - lastBarcodeTime > 200) {
                barcodeBuffer.clear()
            }
            lastBarcodeTime = time

            if (event.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                if (barcodeBuffer.isNotEmpty()) {
                    val barcode = barcodeBuffer.toString()
                    barcodeBuffer.clear()
                    
                    if (barcode.startsWith("WEB-")) {
                        posViewModelInstance?.onEvent(PosEvent.ScanOnlineOrder(barcode))
                        return true
                    }
                }
            } else if (pressedKey.isDefined() && !pressedKey.isISOControl()) {
                barcodeBuffer.append(pressedKey)
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        nfcManager.startReaderMode()
    }

    override fun onPause() {
        nfcManager.stopReaderMode()
        super.onPause()
    }

    override fun onDestroy() {
        appContainer.printerManager.disconnect()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcManager.onNewIntent(intent)
    }
}
