package com.example.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats integer INR amounts into Indian numbering format (e.g. 128000 -> "₹1,28,000").
 */
object InrFormatter {
    fun format(amountInRupees: Long): String {
        val isNegative = amountInRupees < 0
        val digits = kotlin.math.abs(amountInRupees).toString()
        val formatted = if (digits.length <= 3) {
            digits
        } else {
            val lastThree = digits.takeLast(3)
            val rest = digits.dropLast(3)
            val groupedRest = rest.reversed().chunked(2).joinToString(",").reversed()
            "$groupedRest,$lastThree"
        }
        return if (isNegative) "-₹$formatted" else "₹$formatted"
    }

    fun formatDateTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        return sdf.format(Date(timestampMs))
    }

    fun formatDateShort(timestampMs: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date(timestampMs))
    }
}

/**
 * Pure domain calculation and validation rules for cart pricing, coupons, addresses, and WhatsApp order messages.
 */
object CommerceRules {

    data class PriceBreakdown(
        val subtotalInr: Long,
        val discountInr: Long,
        val shippingInr: Long,
        val grandTotalInr: Long,
        val appliedCouponCode: String?
    )

    data class CouponValidation(
        val isValid: Boolean,
        val discountPercent: Int = 0,
        val message: String
    )

    fun validateCoupon(codeRaw: String, subtotalInr: Long): CouponValidation {
        val code = codeRaw.trim().uppercase(Locale.ENGLISH)
        if (code.isBlank()) {
            return CouponValidation(false, 0, "Enter a demo coupon code")
        }
        return when (code) {
            "AURELIA10" -> {
                if (subtotalInr >= 10_000L) {
                    CouponValidation(true, 10, "10% demo privilege applied")
                } else {
                    CouponValidation(false, 0, "Minimum bag value ₹10,000 required for AURELIA10")
                }
            }
            "HERITAGE5" -> CouponValidation(true, 5, "5% demo heritage savings applied")
            else -> CouponValidation(false, 0, "Invalid coupon code. Try AURELIA10 or HERITAGE5")
        }
    }

    fun calculateTotals(
        itemTotalsInr: List<Long>,
        couponCode: String?,
        shippingChargeInr: Long
    ): PriceBreakdown {
        val subtotal = itemTotalsInr.sumOf { it.coerceAtLeast(0L) }
        if (subtotal == 0L) {
            return PriceBreakdown(0L, 0L, 0L, 0L, null)
        }
        val couponCheck = if (!couponCode.isNullOrBlank()) {
            validateCoupon(couponCode, subtotal)
        } else null
        val discount = if (couponCheck?.isValid == true) {
            (subtotal * couponCheck.discountPercent) / 100L
        } else 0L
        val shipping = shippingChargeInr.coerceAtLeast(0L)
        val total = (subtotal - discount + shipping).coerceAtLeast(0L)
        return PriceBreakdown(
            subtotalInr = subtotal,
            discountInr = discount,
            shippingInr = shipping,
            grandTotalInr = total,
            appliedCouponCode = if (couponCheck?.isValid == true) couponCode?.trim()?.uppercase(Locale.ENGLISH) else null
        )
    }

    data class AddressValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String>
    )

    fun validateIndianAddress(
        fullName: String,
        phone: String,
        line1: String,
        city: String,
        state: String,
        pinCode: String
    ): AddressValidationResult {
        val errors = mutableMapOf<String, String>()
        if (fullName.trim().length < 2) {
            errors["fullName"] = "Enter customer full name"
        }
        val digitsOnlyPhone = phone.filter { it.isDigit() }.let {
            if (it.length == 12 && it.startsWith("91")) it.drop(2) else it
        }
        if (!digitsOnlyPhone.matches(Regex("^[6-9][0-9]{9}$"))) {
            errors["phone"] = "Enter a valid 10-digit Indian mobile number"
        }
        if (line1.trim().length < 5) {
            errors["line1"] = "Enter house number, street, or building"
        }
        if (city.trim().length < 2) {
            errors["city"] = "Enter city name"
        }
        if (state.trim().length < 2) {
            errors["state"] = "Enter state name"
        }
        if (!pinCode.trim().matches(Regex("^[1-9][0-9]{5}$"))) {
            errors["pinCode"] = "Enter a valid 6-digit Indian PIN code"
        }
        return AddressValidationResult(errors.isEmpty(), errors)
    }

    fun normalizeWhatsAppNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 -> "91$digits"
            digits.length in 11..15 -> digits
            else -> ""
        }
    }

    fun composeWhatsAppOrderMessage(
        brandName: String,
        orderId: String,
        customerName: String,
        customerPhone: String,
        deliveryAddressSummary: String,
        items: List<OrderLineSummary>,
        grandTotalInr: Long,
        paymentMethod: String
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Hello $brandName,")
        sb.appendLine("Demo Order Inquiry - #$orderId")
        sb.appendLine()
        sb.appendLine("Customer: $customerName")
        sb.appendLine("Phone: $customerPhone")
        sb.appendLine("Delivery: $deliveryAddressSummary")
        sb.appendLine("Selected Payment Mode (Demo): $paymentMethod")
        sb.appendLine()
        sb.appendLine("Items:")
        items.forEachIndexed { idx, item ->
            val sizePart = if (item.selectedSize.isNotBlank() && item.selectedSize != "One Size") {
                " | Size ${item.selectedSize}"
            } else ""
            sb.appendLine(
                "${idx + 1}. ${item.productName} (${item.selectedVariant}$sizePart) x ${item.quantity} - ${InrFormatter.format(item.lineTotalInr)}"
            )
        }
        sb.appendLine()
        sb.appendLine("Estimated Demo Total: ${InrFormatter.format(grandTotalInr)}")
        sb.appendLine("Note: Shared from Aurelia Jewels demo mobile prototype. No online payment collected.")
        return sb.toString().trim()
    }
}

data class OrderLineSummary(
    val productId: String,
    val productName: String,
    val imageKey: String,
    val selectedVariant: String,
    val selectedSize: String,
    val unitPriceInr: Long,
    val quantity: Int
) {
    val lineTotalInr: Long get() = unitPriceInr * quantity
}
