package com.example.data

import kotlinx.coroutines.delay

enum class ImageResolution(val label: String, val dimensions: String) {
    SIZE_1K("1K", "1024 x 1024 px"),
    SIZE_2K("2K", "2048 x 2048 px"),
    SIZE_4K("4K", "4096 x 4096 px")
}

data class AiGeneratedJewellerySample(
    val prompt: String,
    val resolution: ImageResolution,
    val modelId: String,
    val previewDrawableKey: String,
    val metalGradeHint: String,
    val isPlaceholder: Boolean,
    val notice: String
)

interface AiImageRepository {
    val targetModelId: String
    suspend fun generateBespokeConcept(
        prompt: String,
        resolution: ImageResolution
    ): Result<AiGeneratedJewellerySample>
}

/**
 * Placeholder implementation used when Firebase AI Logic is not enabled.
 * Routes all bespoke image generation calls through a single repository boundary
 * so enabling Firebase AI Logic with gemini-3-pro-image-preview requires swapping only this implementation.
 */
class PlaceholderAiImageRepository : AiImageRepository {
    override val targetModelId: String = "gemini-3-pro-image-preview"

    override suspend fun generateBespokeConcept(
        prompt: String,
        resolution: ImageResolution
    ): Result<AiGeneratedJewellerySample> {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.length < 4) {
            return Result.failure(IllegalArgumentException("Enter a brief jewellery design description"))
        }
        delay(450)
        val lower = cleanPrompt.lowercase()
        val drawableKey = when {
            lower.contains("bridal") || lower.contains("polki") || lower.contains("set") -> "img_hero_bridal"
            lower.contains("jhumka") || lower.contains("earring") || lower.contains("chandbali") -> "img_earrings_jhumka"
            lower.contains("bangle") || lower.contains("kada") || lower.contains("bracelet") || lower.contains("cuff") -> "img_bangle_gold"
            lower.contains("necklace") || lower.contains("choker") || lower.contains("mangalsutra") || lower.contains("pendant") -> "img_necklace_gold"
            else -> "img_ring_gold"
        }
        val metalHint = when {
            lower.contains("silver") || lower.contains("platinum") -> "Silver"
            lower.contains("rose") -> "Rose Gold"
            lower.contains("copper") -> "Copper"
            lower.contains("brass") -> "Brass"
            else -> "Gold"
        }
        return Result.success(
            AiGeneratedJewellerySample(
                prompt = cleanPrompt,
                resolution = resolution,
                modelId = targetModelId,
                previewDrawableKey = drawableKey,
                metalGradeHint = metalHint,
                isPlaceholder = true,
                notice = "Sample studio preview shown (${resolution.label} / ${resolution.dimensions}). Live $targetModelId generation requires enabling Firebase AI Logic."
            )
        )
    }
}
