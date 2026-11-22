package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.StoreConfigEntity
import com.example.ui.components.AureliaMonogramEmblem
import com.example.ui.components.AureliaWordmark
import com.example.ui.components.JewelleryPhoto
import com.example.ui.components.PrimaryOliveButton
import com.example.ui.theme.DeepOliveDark
import com.example.ui.theme.PrimaryDeepOlive
import com.example.ui.theme.WarmGoldHighlight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    config: StoreConfigEntity,
    isSeeded: Boolean,
    onContinue: () -> Unit
) {
    LaunchedEffect(isSeeded) {
        if (isSeeded) {
            delay(1100L)
            onContinue()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepOliveDark, PrimaryDeepOlive, Color(0xFF1F2A14))
                )
            )
            .testTag("splash_screen")
    ) {
        // Editorial bridal heritage jewellery photograph anchored softly at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(340.dp)
        ) {
            JewelleryPhoto(
                imageKey = "img_hero_bridal",
                contentDescription = "Indian bridal heritage jewellery showcase",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PrimaryDeepOlive,
                                PrimaryDeepOlive.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            AureliaMonogramEmblem(
                modifier = Modifier.size(62.dp),
                color = WarmGoldHighlight
            )
            Spacer(modifier = Modifier.height(18.dp))
            AureliaWordmark(
                brandName = config.brandName,
                brandSubName = config.brandSubName,
                titleColor = Color(0xFFF6EFE4),
                subColor = WarmGoldHighlight,
                large = true
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = config.tagline,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFF5EFE6).copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class OnboardingSlide(
    val title: String,
    val description: String,
    val imageKey: String
)

@Composable
fun OnboardingScreen(
    config: StoreConfigEntity,
    onFinish: () -> Unit
) {
    val slides = listOf(
        OnboardingSlide(
            title = "Heritage in Every Piece",
            description = "Gold, silver, brass, and copper silhouettes inspired by timeless Indian craftsmanship.",
            imageKey = "img_hero_bridal"
        ),
        OnboardingSlide(
            title = "Jewellery for Every Occasion",
            description = "From intimate engagements and bridal celebrations to refined everyday accents.",
            imageKey = "img_ring_gold"
        ),
        OnboardingSlide(
            title = "Crafted to Be Cherished",
            description = "Explore curated collections, select variants with ease, and share inquiries directly on WhatsApp.",
            imageKey = "img_earrings_jhumka"
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val bgColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val slide = slides[page]
                Box(modifier = Modifier.fillMaxSize()) {
                    JewelleryPhoto(
                        imageKey = slide.imageKey,
                        contentDescription = slide.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(460.dp),
                        contentScale = ContentScale.Crop
                    )
                    // Soft ivory dissolve at bottom of image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(460.dp)
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.60f to Color.Transparent,
                                    0.86f to bgColor.copy(alpha = 0.85f),
                                    1.0f to bgColor
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Skip action at top-right
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor.copy(alpha = 0.82f))
                    .clickable(onClick = onFinish)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("onboarding_skip")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(if (active) 22.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            val isLast = pagerState.currentPage == slides.lastIndex
            PrimaryOliveButton(
                text = if (isLast) "Explore Collection" else "Next",
                onClick = {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                leadingIcon = if (isLast) Icons.AutoMirrored.Outlined.ArrowForward else null,
                modifier = Modifier.fillMaxWidth(),
                testTag = "onboarding_primary_cta"
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${config.brandName} ${config.brandSubName} — Demo Luxury Storefront",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
