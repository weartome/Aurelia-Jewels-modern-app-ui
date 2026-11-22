package com.example

import com.example.data.SeedData
import com.example.data.parsedSizes
import com.example.data.parsedVariants
import com.example.data.requiresExplicitSize
import com.example.domain.CommerceRules
import com.example.domain.InrFormatter
import com.example.domain.OrderLineSummary
import com.example.ui.theme.isValidHexColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AureliaCommerceRulesTest {

    @Test
    fun inrFormattingUsesIndianNumberingSystem() {
        assertEquals("₹0", InrFormatter.format(0L))
        assertEquals("₹950", InrFormatter.format(950L))
        assertEquals("₹24,900", InrFormatter.format(24_900L))
        assertEquals("₹1,28,000", InrFormatter.format(1_28_000L))
        assertEquals("₹12,45,600", InrFormatter.format(12_45_600L))
        assertEquals("-₹5,000", InrFormatter.format(-5_000L))
    }

    @Test
    fun couponValidationEnforcesRealRules() {
        val validHigh = CommerceRules.validateCoupon("aurelia10", 38_500L)
        assertTrue(validHigh.isValid)
        assertEquals(10, validHigh.discountPercent)

        val belowMinimum = CommerceRules.validateCoupon("AURELIA10", 5_000L)
        assertFalse(belowMinimum.isValid)

        val heritage = CommerceRules.validateCoupon("HERITAGE5", 2_500L)
        assertTrue(heritage.isValid)
        assertEquals(5, heritage.discountPercent)

        val invalid = CommerceRules.validateCoupon("FAKE99", 50_000L)
        assertFalse(invalid.isValid)
    }

    @Test
    fun cartTotalsCalculateSubtotalDiscountShippingAndGrandTotal() {
        val breakdown = CommerceRules.calculateTotals(
            itemTotalsInr = listOf(38_500L, 1_28_000L),
            couponCode = "AURELIA10",
            shippingChargeInr = 149L
        )
        assertEquals(1_66_500L, breakdown.subtotalInr)
        assertEquals(16_650L, breakdown.discountInr)
        assertEquals(149L, breakdown.shippingInr)
        assertEquals(1_49_999L, breakdown.grandTotalInr)
        assertEquals("AURELIA10", breakdown.appliedCouponCode)

        val empty = CommerceRules.calculateTotals(emptyList(), "AURELIA10", 149L)
        assertEquals(0L, empty.grandTotalInr)
        assertNull(empty.appliedCouponCode)
    }

    @Test
    fun indianAddressValidationAcceptsValidAndRejectsInvalidInputs() {
        val valid = CommerceRules.validateIndianAddress(
            fullName = "Ananya Deshmukh",
            phone = "9876543210",
            line1 = "402 Gulmohar Residency",
            city = "Mumbai",
            state = "Maharashtra",
            pinCode = "400050"
        )
        assertTrue(valid.isValid)
        assertTrue(valid.errors.isEmpty())

        val invalid = CommerceRules.validateIndianAddress(
            fullName = "A",
            phone = "12345",
            line1 = "Rd",
            city = "",
            state = "",
            pinCode = "012345"
        )
        assertFalse(invalid.isValid)
        assertTrue(invalid.errors.containsKey("fullName"))
        assertTrue(invalid.errors.containsKey("phone"))
        assertTrue(invalid.errors.containsKey("line1"))
        assertTrue(invalid.errors.containsKey("city"))
        assertTrue(invalid.errors.containsKey("state"))
        assertTrue(invalid.errors.containsKey("pinCode"))
    }

    @Test
    fun whatsAppOrderMessageContainsKeyOrderDetailsAndDemoNotice() {
        val msg = CommerceRules.composeWhatsAppOrderMessage(
            brandName = "AURELIA JEWELS",
            orderId = "AJ123456",
            customerName = "Ananya Deshmukh",
            customerPhone = "9876543210",
            deliveryAddressSummary = "Bandra West, Mumbai - 400050",
            items = listOf(
                OrderLineSummary(
                    productId = "prod_ring_floral_gold",
                    productName = "Floral Gold Ring",
                    imageKey = "img_ring_gold",
                    selectedVariant = "22K Yellow Gold",
                    selectedSize = "12",
                    unitPriceInr = 38_500L,
                    quantity = 1
                )
            ),
            grandTotalInr = 38_500L,
            paymentMethod = "UPI (Demo)"
        )
        assertTrue(msg.contains("#AJ123456"))
        assertTrue(msg.contains("Floral Gold Ring"))
        assertTrue(msg.contains("Size 12"))
        assertTrue(msg.contains("₹38,500"))
        assertTrue(msg.contains("No online payment collected"))
        assertEquals("919820011223", CommerceRules.normalizeWhatsAppNumber("9820011223"))
    }

    @Test
    fun seededCatalogueIntegrityAndCategorySpecificSizing() {
        val products = SeedData.defaultProducts()
        assertTrue("Expected at least 24 demo products", products.size >= 24)
        assertEquals("Product IDs must be unique", products.size, products.map { it.id }.toSet().size)
        assertEquals("SKUs must be unique", products.size, products.map { it.sku }.toSet().size)

        val categoriesPresent = products.map { it.category }.toSet()
        SeedData.allCategories.forEach { cat ->
            assertTrue("Category $cat must have seeded items", categoriesPresent.contains(cat))
        }

        val metalsPresent = products.map { it.metal }.toSet()
        SeedData.allMetals.forEach { metal ->
            assertTrue("Metal $metal must have seeded items", metalsPresent.contains(metal))
        }

        val ring = products.first { it.category == "Rings" }
        assertTrue(ring.requiresExplicitSize())
        assertTrue(ring.parsedVariants().isNotEmpty())

        val necklace = products.first { it.category == "Necklaces" }
        assertFalse(necklace.requiresExplicitSize())
        assertEquals(listOf("One Size"), necklace.parsedSizes())
    }

    @Test
    fun studioHexColorValidationWorks() {
        assertTrue(isValidHexColor("#27351A"))
        assertTrue(isValidHexColor("AD853C"))
        assertFalse(isValidHexColor("#ZZZ"))
        assertFalse(isValidHexColor("12345"))
    }

    @Test
    fun gridFiltersByCategoryMaterialPriceRangeSavedAndSort() {
        val products = SeedData.defaultProducts()
        val saved = setOf("prod_ring_floral_gold", "prod_ear_heritage_jhumka")

        val goldRingsMidPrice = com.example.ui.screens.applyJewelleryGridFilters(
            products = products,
            wishlistIds = saved,
            criteria = com.example.ui.screens.GridFilterCriteria(
                category = "Rings",
                material = "Gold",
                priceRange = com.example.ui.screens.JewelleryPriceRange.RANGE_10K_50K,
                sortOrder = com.example.ui.screens.GridSortOrder.PRICE_ASC
            )
        )
        assertTrue(goldRingsMidPrice.isNotEmpty())
        assertTrue(goldRingsMidPrice.all { it.category == "Rings" && it.metal == "Gold" && it.priceInr in 10_000L..50_000L })
        assertTrue(goldRingsMidPrice.zipWithNext().all { (a, b) -> a.priceInr <= b.priceInr })

        val savedOnly = com.example.ui.screens.applyJewelleryGridFilters(
            products = products,
            wishlistIds = saved,
            criteria = com.example.ui.screens.GridFilterCriteria(savedOnly = true)
        )
        assertEquals(2, savedOnly.size)
        assertTrue(savedOnly.all { saved.contains(it.id) })
    }
}
