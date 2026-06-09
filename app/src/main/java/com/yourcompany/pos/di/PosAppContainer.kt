package com.yourcompany.pos.di

import android.content.Context
import androidx.room.Room
import com.yourcompany.pos.data.local.db.PosDatabase
import com.yourcompany.pos.data.repository.CartRepositoryImpl
import com.yourcompany.pos.data.repository.HoldOrderRepository
import com.yourcompany.pos.data.repository.OrderRepositoryImpl
import com.yourcompany.pos.data.repository.ProductRepositoryImpl
import com.yourcompany.pos.data.repository.SettingsRepositoryImpl
import com.yourcompany.pos.data.seed.SampleDataSeeder
import com.yourcompany.pos.printer.PrinterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.yourcompany.pos.data.remote.PosWebServer

class PosAppContainer(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: PosDatabase = Room.databaseBuilder(
        context,
        PosDatabase::class.java,
        "pos.db"
    ).addMigrations(PosDatabase.MIGRATION_2_3, PosDatabase.MIGRATION_3_4, PosDatabase.MIGRATION_4_5, PosDatabase.MIGRATION_5_6, PosDatabase.MIGRATION_6_7, PosDatabase.MIGRATION_7_8, PosDatabase.MIGRATION_8_9)
        .fallbackToDestructiveMigration()
        .build()

    val printerManager = PrinterManager(context)

    val productRepository = ProductRepositoryImpl(database.productDao())
    val cartRepository = CartRepositoryImpl(database.cartDao())
    val orderRepository = OrderRepositoryImpl(database.orderDao())
    val memberRepository = com.yourcompany.pos.data.repository.MemberRepositoryImpl(database.memberDao())
    val settingsRepository = SettingsRepositoryImpl(database.settingsDao())
    val holdOrderRepository = HoldOrderRepository(database.holdOrderDao())
    private val sampleDataSeeder = SampleDataSeeder(productRepository)

    val webServer = PosWebServer(
        androidContext = context,
        orderRepository = orderRepository,
        productRepository = productRepository,
        memberRepository = memberRepository,
        settingsRepository = settingsRepository
    )

    fun getViewModelFactory(nfcManager: com.yourcompany.pos.nfc.NfcManager): PosViewModelFactory {
        return PosViewModelFactory(
            productRepository = productRepository,
            cartRepository = cartRepository,
            orderRepository = orderRepository,
            memberRepository = memberRepository,
            settingsRepository = settingsRepository,
            holdOrderRepository = holdOrderRepository,
            nfcManager = nfcManager,
            printerManager = printerManager
        )
    }

    init {
        applicationScope.launch {
            sampleDataSeeder.seedIfEmpty()
        }
    }
}
