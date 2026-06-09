package com.yourcompany.pos.data.seed

import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.first

class SampleDataSeeder(
    private val productRepository: ProductRepository
) {
    suspend fun seedIfEmpty() {
        val existing = productRepository.observeProducts().first()
        if (existing.isNotEmpty()) return

        productRepository.upsertProducts(sampleProducts())
    }

    private fun sampleProducts(): List<Product> = listOf(
        Product(id = 0, sku = "DRK-001", name = "經典奶茶", price = 65.0, taxRate = 0.05, category = "飲品"),
        Product(id = 0, sku = "DRK-002", name = "美式咖啡", price = 55.0, taxRate = 0.05, category = "飲品"),
        Product(id = 0, sku = "DRK-003", name = "冷萃拿鐵", price = 85.0, taxRate = 0.05, category = "飲品"),
        Product(id = 0, sku = "DRK-004", name = "柚子冰茶", price = 70.0, taxRate = 0.05, category = "飲品"),
        Product(id = 0, sku = "DRK-005", name = "氣泡美式", price = 75.0, taxRate = 0.05, category = "飲品"),
        Product(id = 0, sku = "FOD-101", name = "雞肉三明治", price = 95.0, taxRate = 0.05, category = "輕食"),
        Product(id = 0, sku = "FOD-102", name = "可頌麵包", price = 45.0, taxRate = 0.05, category = "輕食"),
        Product(id = 0, sku = "FOD-103", name = "火腿起司捲", price = 72.0, taxRate = 0.05, category = "輕食"),
        Product(id = 0, sku = "FOD-104", name = "鮪魚潛艇堡", price = 118.0, taxRate = 0.05, category = "輕食"),
        Product(id = 0, sku = "FOD-105", name = "日式咖哩飯", price = 158.0, taxRate = 0.05, category = "輕食"),
        Product(id = 0, sku = "SNK-201", name = "海鹽洋芋片", price = 35.0, taxRate = 0.05, category = "零食"),
        Product(id = 0, sku = "SNK-202", name = "氣泡水", price = 30.0, taxRate = 0.05, category = "零食"),
        Product(id = 0, sku = "SNK-203", name = "能量飲料", price = 49.0, taxRate = 0.05, category = "零食"),
        Product(id = 0, sku = "SNK-204", name = "無糖優格", price = 42.0, taxRate = 0.05, category = "零食"),
        Product(id = 0, sku = "SNK-205", name = "焦糖爆米花", price = 39.0, taxRate = 0.05, category = "零食"),
        Product(id = 0, sku = "ADD-301", name = "加冰塊", price = 0.0, taxRate = 0.0, category = "加購"),
        Product(id = 0, sku = "ADD-302", name = "加鮮奶", price = 10.0, taxRate = 0.05, category = "加購"),
        Product(id = 0, sku = "ADD-303", name = "加起司", price = 12.0, taxRate = 0.05, category = "加購"),
        Product(id = 0, sku = "CTP-401", name = "會員卡補辦", price = 120.0, taxRate = 0.05, category = "其他"),
        Product(id = 0, sku = "CTP-402", name = "包裝盒", price = 15.0, taxRate = 0.05, category = "其他")
    )
}
