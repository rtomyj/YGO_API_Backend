package com.rtomyj.skc.browse.product.dao

import com.rtomyj.skc.browse.product.model.Product
import com.rtomyj.skc.browse.product.model.ProductContent
import com.rtomyj.skc.browse.product.model.Products
import com.rtomyj.skc.util.enumeration.ProductType

interface ProductDao {
	fun getProductInfo(productId: String, locale: String): Product
	fun getProductsByLocale(locale: String): List<Product>
	fun getProductDetailsForCard(cardId: String): MutableList<Product>
	fun getProductContents(productId: String, locale: String): Set<ProductContent>
	fun getAllProductsByType(productType: ProductType, locale: String): Products
	fun getProductRarityCount(productId: String): Map<String, Int>
}