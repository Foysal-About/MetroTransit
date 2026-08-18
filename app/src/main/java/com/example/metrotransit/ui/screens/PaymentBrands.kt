package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.metrotransit.R

/**
 * A way to pay, and everything needed to draw it.
 *
 * The logos ship at wildly different aspect ratios and with different amounts of built-in
 * padding, which is why the artwork is described here rather than at each call site: the
 * tile decides how a mark is placed, so every row in every list gets the same optical size.
 */
data class PaymentBrand(
    val id: String,
    val name: String,
    val color: Color,
    /** A logo asset, drawn inside a white tile so brand colours keep their contrast. */
    val logoRes: Int? = null,
    /** Set when the asset carries its own background and should fill the tile edge to edge. */
    val logoFillsTile: Boolean = false,
    /** Used when there is no usable logo asset; drawn in [color] on a tint of it. */
    val icon: ImageVector? = null,
    /**
     * Draws [icon] white on a solid [color] tile instead. For a real brand standing in for
     * its logo — a pale tint next to four full-strength marks reads as a disabled row.
     */
    val iconOnBrandColor: Boolean = false
)

/**
 * Every method offered for a ticket purchase, in the order they are shown.
 *
 * Rocket is drawn from its icon rather than its asset: the supplied logo is a wide lock-up
 * with a wordmark, which shrinks to an unreadable strip in a square tile.
 */
val mobileWalletBrands = listOf(
    PaymentBrand(
        id = "bKash",
        name = "bKash",
        color = Color(0xFFE2136E),
        logoRes = R.drawable.bkash_logo
    ),
    PaymentBrand(
        id = "Nagad",
        name = "Nagad",
        color = Color(0xFFED1C24),
        logoRes = R.drawable.nagad_logo
    ),
    PaymentBrand(
        id = "Rocket",
        name = "Rocket",
        color = Color(0xFF8C3494),
        icon = Icons.AutoMirrored.Filled.Send,
        iconOnBrandColor = true
    ),
    PaymentBrand(
        id = "Upay",
        name = "Upay",
        color = Color(0xFF00ADEF),
        logoRes = R.drawable.upay_logo,
        logoFillsTile = true
    )
)

/** Cards and bank rails, kept apart from the wallets so a long list stays scannable. */
val cardAndBankBrands = listOf(
    PaymentBrand(
        id = "Card",
        name = "Debit/Credit Card",
        color = Color(0xFF0061C1),
        icon = Icons.Default.CreditCard
    ),
    PaymentBrand(
        id = "NetBanking",
        name = "Internet Banking",
        color = Color(0xFF0F766E),
        icon = Icons.Default.AccountBalance
    )
)

/**
 * The brand behind a free-text method name (the MRT Pass portal stores methods as strings),
 * or null when nothing matches and the caller should fall back to an initial.
 */
fun paymentBrandFor(name: String): PaymentBrand? {
    val needle = name.lowercase().trim()
    if (needle.isEmpty()) return null

    return (mobileWalletBrands + cardAndBankBrands).firstOrNull {
        needle.contains(it.id.lowercase()) ||
            needle.contains(it.name.lowercase()) ||
            (it.id == "Card" && (needle.contains("card") || needle.contains("visa") || needle.contains("master"))) ||
            (it.id == "NetBanking" && (needle.contains("bank") && !needle.contains("mobile")))
    }
}

/** One size for every payment mark in the app. */
val PaymentTileSize = 44.dp

/**
 * A payment mark in its tile. Whatever the artwork — a tight logo, a logo that brings its
 * own background, or a plain icon — it lands in the same square, so a list of methods reads
 * as one column instead of a row of mismatched stickers.
 */
@Composable
fun PaymentBrandTile(
    brand: PaymentBrand?,
    fallbackName: String = "",
    modifier: Modifier = Modifier,
    size: Dp = PaymentTileSize
) {
    val shape = RoundedCornerShape(size * 0.28f)
    val tile = modifier.size(size)

    when {
        brand?.logoRes != null && brand.logoFillsTile -> {
            Image(
                painter = painterResource(id = brand.logoRes),
                contentDescription = brand.name,
                contentScale = ContentScale.Crop,
                modifier = tile.clip(shape)
            )
        }

        brand?.logoRes != null -> {
            // White, not the brand tint: these marks are printed for light backgrounds and
            // lose their edges on anything else.
            Surface(modifier = tile, shape = shape, color = Color.White) {
                Image(
                    painter = painterResource(id = brand.logoRes),
                    contentDescription = brand.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(size * 0.16f)
                )
            }
        }

        brand?.icon != null -> {
            val onBrand = brand.iconOnBrandColor
            Surface(
                modifier = tile,
                shape = shape,
                color = if (onBrand) brand.color else brand.color.copy(alpha = 0.14f)
            ) {
                Icon(
                    brand.icon,
                    contentDescription = brand.name,
                    tint = if (onBrand) Color.White else brand.color,
                    modifier = Modifier.padding(size * 0.25f)
                )
            }
        }

        else -> {
            Surface(modifier = tile, shape = shape, color = Color(0xFF3269B5).copy(alpha = 0.14f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        fallbackName.take(1).uppercase(),
                        color = Color(0xFF3269B5),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
