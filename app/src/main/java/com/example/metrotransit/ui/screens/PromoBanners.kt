package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared height for every card in the "Featured Deals" rail. */
val FEATURED_CARD_HEIGHT = 164.dp

/** Card scheme mark rendered on a promo banner. */
enum class CardNetwork { VISA, MASTERCARD, AMEX, NONE }

/**
 * A designed (fully drawn, no network image) promotional banner for a bank /
 * card-scheme offer shown in the "Featured Deals" rail.
 */
data class PromoOffer(
    val brand: String,
    val headline: String,
    val subline: String,
    val badge: String,
    val footnote: String,
    val gradient: List<Color>,
    val accent: Color,
    val network: CardNetwork = CardNetwork.NONE
)

/** Bank & card-scheme offers featured on the home screen. */
val featuredPromos = listOf(
    PromoOffer(
        brand = "THE CITY BANK",
        headline = "20% CASHBACK",
        subline = "On metro tickets paid with City Bank Amex cards",
        badge = "UP TO ৳300",
        footnote = "Valid till 31 Dec 2026",
        gradient = listOf(Color(0xFF7A0C12), Color(0xFFC8102E), Color(0xFFED1C24)),
        accent = Color(0xFFFFD166),
        network = CardNetwork.AMEX
    ),
    PromoOffer(
        brand = "VISA CONTACTLESS",
        headline = "TAP & RIDE FREE",
        subline = "1 free ride every week on Visa tap-to-pay",
        badge = "NEW",
        footnote = "Min. 3 rides per week",
        gradient = listOf(Color(0xFF0B0F3B), Color(0xFF1A1F71), Color(0xFF2C36A8)),
        accent = Color(0xFFF7B600),
        network = CardNetwork.VISA
    ),
    PromoOffer(
        brand = "MASTERCARD × MRT PASS",
        headline = "৳100 BONUS",
        subline = "On every ৳500 MRT Pass recharge",
        badge = "BUY 1 GET 1",
        footnote = "First recharge each month",
        gradient = listOf(Color(0xFF12121C), Color(0xFF23233A), Color(0xFF31314F)),
        accent = Color(0xFFF79E1B),
        network = CardNetwork.MASTERCARD
    )
)

@Composable
fun PromoBannerCard(
    offer: PromoOffer,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(24.dp)
    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = offer.gradient,
                        start = Offset.Zero,
                        end = Offset(900f, 500f)
                    )
                )
        ) {
            // Decorative glow + card artwork
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.height * 0.75f,
                    center = Offset(size.width * 0.92f, size.height * 0.12f)
                )
                drawCircle(
                    color = offer.accent.copy(alpha = 0.12f),
                    radius = size.height * 0.45f,
                    center = Offset(size.width * 0.08f, size.height * 1.02f)
                )
            }

            MiniCreditCardArt(
                accent = offer.accent,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .rotate(-14f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        offer.brand,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = offer.accent.copy(alpha = 0.22f)
                    ) {
                        Text(
                            offer.badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = offer.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    offer.headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    offer.subline,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(0.72f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        offer.footnote,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CardNetworkMark(offer.network)
                }
            }
        }
    }

    if (onClick != null) {
        Surface(
            modifier = modifier
                .width(300.dp)
                .height(FEATURED_CARD_HEIGHT),
            shape = shape,
            color = Color.Transparent,
            onClick = onClick,
            content = { content() }
        )
    } else {
        Surface(
            modifier = modifier
                .width(300.dp)
                .height(FEATURED_CARD_HEIGHT),
            shape = shape,
            color = Color.Transparent,
            content = { content() }
        )
    }
}

/** Small decorative credit-card silhouette with chip & magnetic stripe. */
@Composable
private fun MiniCreditCardArt(accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 104.dp, height = 66.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.06f)
                    )
                )
            )
            .padding(9.dp)
    ) {
        // Chip
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 13.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accent.copy(alpha = 0.85f))
        )
        // Number placeholder dots
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(4) { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    repeat(if (group == 3) 4 else 3) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.55f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardNetworkMark(network: CardNetwork) {
    when (network) {
        CardNetwork.VISA -> Text(
            "VISA",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 1.5.sp,
            color = Color.White
        )

        CardNetwork.MASTERCARD -> Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(width = 34.dp, height = 22.dp)) {
                val r = size.height / 2f
                val left = Offset(r, r)
                val right = Offset(size.width - r, r)
                drawCircle(Color(0xFFEB001B), radius = r, center = left)
                drawCircle(Color(0xFFF79E1B), radius = r, center = right)
                // Interlock: paint the intersection in Mastercard's orange
                val leftCircle = Path().apply {
                    addOval(Rect(center = left, radius = r))
                }
                clipPath(leftCircle) {
                    drawCircle(Color(0xFFFF5F00), radius = r, center = right)
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "mastercard",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        CardNetwork.AMEX -> Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF016FD0)
        ) {
            Text(
                "AMEX",
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = Color.White
            )
        }

        CardNetwork.NONE -> Spacer(modifier = Modifier.size(0.dp))
    }
}
