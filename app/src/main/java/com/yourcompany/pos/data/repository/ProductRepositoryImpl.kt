package com.yourcompany.pos.data.repository

import com.yourcompany.pos.data.local.dao.ProductDao
import com.yourcompany.pos.data.local.entity.ProductEntity
import com.yourcompany.pos.domain.model.Product
import com.yourcompany.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {
    override fun observeProducts(): Flow<List<Product>> =
        productDao.observeActiveProducts().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchProducts(keyword: String): Flow<List<Product>> =
        productDao.searchProducts(keyword).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getProductById(productId: Long): Product? =
        productDao.getProductById(productId)?.toDomain()

    override suspend fun upsertProduct(product: Product) {
        productDao.upsert(ProductEntity.fromDomain(product))
    }

    override suspend fun upsertProducts(products: List<Product>) {
        productDao.upsertAll(products.map(ProductEntity::fromDomain))
    }

    override suspend fun deleteProductById(productId: Long) {
        productDao.deleteProductById(productId)
    }

    override suspend fun clearAll() {
        productDao.clearAll()
    }
}
