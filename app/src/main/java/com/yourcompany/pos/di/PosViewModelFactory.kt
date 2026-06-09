package com.yourcompany.pos.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourcompany.pos.data.repository.CartRepositoryImpl
import com.yourcompany.pos.data.repository.ProductRepositoryImpl
import com.yourcompany.pos.nfc.NfcManager
import com.yourcompany.pos.printer.PrinterManager
import com.yourcompany.pos.presentation.pos.PosViewModel
import com.yourcompany.pos.domain.repository.CartRepository
import com.yourcompany.pos.domain.repository.OrderRepository
import com.yourcompany.pos.domain.repository.ProductRepository
import com.yourcompany.pos.domain.repository.MemberRepository
import com.yourcompany.pos.domain.repository.SettingsRepository
import com.yourcompany.pos.domain.repository.EmployeeRepository
import com.yourcompany.pos.data.repository.HoldOrderRepository

class PosViewModelFactory(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository,
    private val settingsRepository: SettingsRepository,
    private val holdOrderRepository: HoldOrderRepository,
    private val employeeRepository: EmployeeRepository,
    private val nfcManager: NfcManager,
    private val printerManager: PrinterManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            return PosViewModel(
                productRepository = productRepository,
                cartRepository = cartRepository,
                orderRepository = orderRepository,
                memberRepository = memberRepository,
                settingsRepository = settingsRepository,
                holdOrderRepository = holdOrderRepository,
                employeeRepository = employeeRepository,
                nfcManager = nfcManager,
                printerManager = printerManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
