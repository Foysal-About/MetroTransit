package com.example.metrotransit.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.metrotransit.ui.theme.MetroTransitTheme

// ─── Model ────────────────────────────────────────────────────────────────────

/** Categories the partner directory can be filtered by. */
enum class PartnerCategory(val label: String) {
    FASHION("Fashion"),
    TRAVEL("Tour & Travels"),
    FOOD("Food"),
    ECOMMERCE("E-Commerce"),
    BEAUTY("Beauty"),
    ACCESSORY("Accessory")
}

/**
 * The drawn flourish that sits with a brand's wordmark, standing in for its real logo art
 * until an asset is supplied. Deliberately generic shapes — a burst, a leaf, a swoosh —
 * so a plate reads as a logo rather than as a line of text.
 */
enum class BrandMark {
    /** Wordmark only. */
    NONE,

    /** Radiating petals above the wordmark. */
    BURST,

    /** Small leaf tucked above the wordmark. */
    LEAF,

    /** Rising swoosh, for the travel brands. */
    SWOOSH,

    /** Long soaring line drawn across the plate. */
    CURVE,

    /** Wordmark framed in a brand-coloured box. */
    BOXED
}

/**
 * A discount partner shown in the directory.
 *
 * The plate falls back to a designed wordmark, so real art can be dropped in later through
 * [logoRes] (bundled drawable) or [logoUrl] (remote) without touching the UI.
 */
data class Partner(
    val name: String,
    val tagline: String,
    val category: PartnerCategory,
    val brandColor: Color,
    /** Reserved for the partner detail sheet; the grid shows the logo plate only. */
    val offer: String? = null,
    val wordmark: String = name,
    /** Overrides the plate's wordmark size — short marks (a Bengali lock-up) want to run big. */
    val wordmarkSize: TextUnit? = null,
    /** Second line inside the plate — a Bengali lock-up or a descriptor. */
    val subtext: String? = null,
    val subtextSize: TextUnit? = null,
    val mark: BrandMark = BrandMark.NONE,
    val markColor: Color? = null,
    /** Plate fill; brands with a coloured logo block override the default white. */
    val plateColor: Color = Color.White,
    val italic: Boolean = false,
    @DrawableRes val logoRes: Int? = null,
    val logoUrl: String? = null
)

/** Sample partner directory — swap for API data when the merchant service is live. */
val partnerDirectory = listOf(
    Partner(
        name = "Aarong",
        tagline = "Fashion & Lifestyle",
        category = PartnerCategory.FASHION,
        brandColor = Color(0xFFE23A2E),
        offer = "10% OFF",
        subtext = "আড়ং",
        mark = BrandMark.BURST,
        markColor = Color(0xFFF26A21)
    ),
    Partner(
        name = "AMYBD",
        tagline = "Travel in Style",
        category = PartnerCategory.TRAVEL,
        brandColor = Color(0xFF1F4CA8),
        offer = "12% OFF",
        subtext = "ভ্রমি",
        subtextSize = 15.sp
    ),
    Partner(
        name = "Apex",
        tagline = "Fashion That Fits, Trends That Last",
        category = PartnerCategory.FASHION,
        brandColor = Color(0xFFE01B24),
        offer = "15% OFF",
        mark = BrandMark.LEAF,
        markColor = Color(0xFF35A853),
        italic = true
    ),
    Partner(
        name = "Bata",
        tagline = "Make Your Way",
        category = PartnerCategory.FASHION,
        brandColor = Color(0xFFD81E27),
        offer = "10% OFF",
        italic = true
    ),
    Partner(
        name = "Gozayaan",
        tagline = "Flight Booking",
        category = PartnerCategory.TRAVEL,
        brandColor = Color(0xFF1B54B3),
        offer = "৳500 OFF",
        wordmark = "gozayaan",
        mark = BrandMark.SWOOSH
    ),
    Partner(
        name = "KFC",
        tagline = "Order online for Delivery",
        category = PartnerCategory.FOOD,
        brandColor = Color(0xFFE4002B),
        offer = "Buy 1 Get 1",
        mark = BrandMark.BOXED
    ),
    Partner(
        name = "Le Reve",
        tagline = "Fashion and Lifestyle",
        category = PartnerCategory.FASHION,
        brandColor = Color(0xFF1B1B1B),
        offer = "20% OFF",
        wordmark = "le reve",
        mark = BrandMark.CURVE,
        markColor = Color(0xFF1B1B1B),
        plateColor = Color(0xFFFFC300)
    ),
    Partner(
        name = "ShareTrip",
        tagline = "Experience Better",
        category = PartnerCategory.TRAVEL,
        brandColor = Color(0xFF00A0DF),
        offer = "৳750 OFF",
        mark = BrandMark.SWOOSH
    ),
    Partner(
        name = "Sultan's Dine",
        tagline = "Kacchi & more",
        category = PartnerCategory.FOOD,
        brandColor = Color(0xFF8B1E3F),
        offer = "12% OFF",
        subtext = "সুলতান'স ডাইন"
    ),
    Partner(
        name = "Chillox",
        tagline = "Burgers & Grill",
        category = PartnerCategory.FOOD,
        brandColor = Color.White,
        offer = "15% OFF",
        plateColor = Color(0xFF212121)
    ),
    Partner(
        name = "Daraz",
        tagline = "Online Shopping",
        category = PartnerCategory.ECOMMERCE,
        brandColor = Color(0xFFF85606),
        offer = "৳300 Voucher",
        mark = BrandMark.SWOOSH
    ),
    Partner(
        name = "Chaldal",
        tagline = "Grocery Delivery",
        category = PartnerCategory.ECOMMERCE,
        brandColor = Color(0xFF2E9E4F),
        offer = "10% OFF",
        subtext = "চালডাল",
        mark = BrandMark.LEAF,
        markColor = Color(0xFF2E9E4F)
    ),
    Partner(
        name = "Pickaboo",
        tagline = "Gadgets & Electronics",
        category = PartnerCategory.ECOMMERCE,
        brandColor = Color(0xFF00A99D),
        offer = "৳500 OFF"
    ),
    Partner(
        name = "Persona",
        tagline = "Beauty & Grooming",
        category = PartnerCategory.BEAUTY,
        brandColor = Color(0xFFB4126B),
        offer = "20% OFF",
        mark = BrandMark.BURST,
        markColor = Color(0xFFB4126B)
    ),
    Partner(
        name = "Herlan",
        tagline = "Skincare & Wellness",
        category = PartnerCategory.BEAUTY,
        brandColor = Color(0xFF6D3BAF),
        offer = "15% OFF",
        mark = BrandMark.LEAF,
        markColor = Color(0xFF6D3BAF)
    ),
    Partner(
        name = "Ecstasy",
        tagline = "Bags & Accessories",
        category = PartnerCategory.ACCESSORY,
        brandColor = Color(0xFF1D3557),
        offer = "10% OFF"
    ),
    Partner(
        name = "Artisan",
        tagline = "Watches & Eyewear",
        category = PartnerCategory.ACCESSORY,
        brandColor = Color(0xFF00695C),
        offer = "12% OFF",
        mark = BrandMark.CURVE,
        markColor = Color(0xFF00695C)
    )
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerOffersScreen(
    onBack: () -> Unit,
    partners: List<Partner> = partnerDirectory,
    onPartnerClick: (Partner) -> Unit = {}
) {
    val extendedColors = MetroTransitTheme.extendedColors
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PartnerCategory?>(null) }

    val results = remember(query, selectedCategory, partners) {
        val trimmed = query.trim()
        partners.filter { partner ->
            val matchesCategory = selectedCategory == null || partner.category == selectedCategory
            val matchesQuery = trimmed.isEmpty() ||
                partner.name.contains(trimmed, ignoreCase = true) ||
                partner.tagline.contains(trimmed, ignoreCase = true) ||
                partner.category.label.contains(trimmed, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Partner Offers",
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                        Text(
                            "Discounts for metro riders",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extendedColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extendedColors.backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                PartnerSearchField(query = query, onQueryChange = { query = it })

                CategoryFilterRow(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )

                if (results.isEmpty()) {
                    NoPartnersFound()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 24.dp + LocalJourneyBarInset.current
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(results, key = { _, partner -> partner.name }) { index, partner ->
                            PartnerCard(
                                partner = partner,
                                lightIndex = index,
                                onClick = { onPartnerClick(partner) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerSearchField(query: String, onQueryChange: (String) -> Unit) {
    val extendedColors = MetroTransitTheme.extendedColors
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 26.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search", color = extendedColors.textSecondary) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = extendedColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = extendedColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = extendedColors.textPrimary,
                unfocusedTextColor = extendedColors.textPrimary
            ),
            singleLine = true
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selected: PartnerCategory?,
    onSelect: (PartnerCategory?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CategoryChip("All", selected == null) { onSelect(null) }
        }
        items(PartnerCategory.entries.toList()) { category ->
            CategoryChip(category.label, selected == category) {
                onSelect(if (selected == category) null else category)
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val extendedColors = MetroTransitTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    LiquidGlassSurface(
        cornerRadius = 20.dp,
        light = LiquidGlassLight.Inset,
        tint = if (isSelected) accent.copy(alpha = 0.10f) else Color.Unspecified,
        border = BorderStroke(1.dp, if (isSelected) accent else extendedColors.glassBorder),
        onClick = onClick
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accent else extendedColors.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun PartnerCard(partner: Partner, lightIndex: Int, onClick: () -> Unit) {
    val extendedColors = MetroTransitTheme.extendedColors
    LiquidGlassSurface(
        cornerRadius = 18.dp,
        light = liquidGlassLightAt(lightIndex),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PartnerLogoPlate(partner)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                partner.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                partner.tagline,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                color = extendedColors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** The logo tile: real asset when supplied, otherwise a designed brand lock-up. */
@Composable
private fun PartnerLogoPlate(partner: Partner) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.74f)
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(partner.plateColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            partner.logoRes != null -> androidx.compose.foundation.Image(
                painter = painterResource(partner.logoRes),
                contentDescription = partner.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(12.dp)
            )

            partner.logoUrl != null -> AsyncImage(
                model = partner.logoUrl,
                contentDescription = partner.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(12.dp)
            )

            else -> DesignedBrandLockup(partner)
        }
    }
}

@Composable
private fun DesignedBrandLockup(partner: Partner) {
    val markColor = partner.markColor ?: partner.brandColor
    Column(
        modifier = Modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (partner.mark) {
            BrandMark.BURST -> {
                BurstMark(markColor)
                Spacer(modifier = Modifier.height(5.dp))
            }

            BrandMark.LEAF -> {
                LeafMark(markColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            BrandMark.SWOOSH -> {
                SwooshMark(markColor)
                Spacer(modifier = Modifier.height(3.dp))
            }

            BrandMark.CURVE -> {
                CurveMark(markColor)
                Spacer(modifier = Modifier.height(2.dp))
            }

            BrandMark.BOXED, BrandMark.NONE -> Unit
        }

        val wordmark: @Composable () -> Unit = {
            Text(
                partner.wordmark,
                fontSize = partner.wordmarkSize
                    ?: if (partner.mark == BrandMark.NONE) 17.sp else 15.sp,
                lineHeight = (partner.wordmarkSize?.value?.plus(4f) ?: 19f).sp,
                fontWeight = FontWeight.Black,
                fontStyle = if (partner.italic) FontStyle.Italic else FontStyle.Normal,
                letterSpacing = (-0.4).sp,
                color = partner.brandColor,
                textAlign = TextAlign.Center,
                // A one-word mark must never break mid-word; only a multi-word
                // lock-up is allowed to run onto a second line.
                softWrap = partner.wordmark.contains(' '),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (partner.mark == BrandMark.BOXED) {
            Box(
                modifier = Modifier
                    .border(2.dp, partner.brandColor, RoundedCornerShape(5.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                wordmark()
            }
        } else {
            wordmark()
        }

        partner.subtext?.let { subtext ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subtext,
                fontSize = partner.subtextSize ?: 9.sp,
                lineHeight = ((partner.subtextSize?.value ?: 9f) + 3f).sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                color = partner.brandColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Radiating petals — the floral burst above a wordmark. */
@Composable
private fun BurstMark(color: Color) {
    Canvas(modifier = Modifier.size(26.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val petal = size.minDimension * 0.13f
        val ring = size.minDimension * 0.33f
        repeat(8) { i ->
            val angle = (i * 45f) * (Math.PI / 180f).toFloat()
            drawCircle(
                color = color,
                radius = petal,
                center = Offset(
                    center.x + ring * kotlin.math.cos(angle),
                    center.y + ring * kotlin.math.sin(angle)
                )
            )
        }
        repeat(8) { i ->
            val angle = (i * 45f + 22.5f) * (Math.PI / 180f).toFloat()
            drawCircle(
                color = color.copy(alpha = 0.75f),
                radius = petal * 0.55f,
                center = Offset(
                    center.x + size.minDimension * 0.46f * kotlin.math.cos(angle),
                    center.y + size.minDimension * 0.46f * kotlin.math.sin(angle)
                )
            )
        }
        drawCircle(color = color, radius = petal * 0.9f, center = center)
    }
}

/** A small leaf, tucked above the wordmark. */
@Composable
private fun LeafMark(color: Color) {
    Canvas(modifier = Modifier.size(width = 18.dp, height = 13.dp)) {
        val leaf = Path().apply {
            moveTo(0f, size.height)
            quadraticTo(0f, 0f, size.width, size.height * 0.1f)
            quadraticTo(size.width * 0.35f, size.height * 0.75f, 0f, size.height)
            close()
        }
        drawPath(leaf, color)
    }
}

/** A rising swoosh with a leading tip, for the travel brands. */
@Composable
private fun SwooshMark(color: Color) {
    Canvas(modifier = Modifier.size(width = 34.dp, height = 15.dp)) {
        val stroke = size.height * 0.16f
        val swoosh = Path().apply {
            moveTo(0f, size.height * 0.9f)
            cubicTo(
                size.width * 0.35f, size.height * 0.85f,
                size.width * 0.55f, size.height * 0.45f,
                size.width * 0.92f, size.height * 0.08f
            )
        }
        drawPath(swoosh, color, style = Stroke(width = stroke))

        val tip = Path().apply {
            moveTo(size.width, 0f)
            lineTo(size.width * 0.62f, size.height * 0.22f)
            lineTo(size.width * 0.88f, size.height * 0.5f)
            close()
        }
        drawPath(tip, color)
    }
}

/** A long soaring line drawn across the plate. */
@Composable
private fun CurveMark(color: Color) {
    Canvas(modifier = Modifier.size(width = 46.dp, height = 16.dp)) {
        val line = Path().apply {
            moveTo(0f, size.height * 0.85f)
            cubicTo(
                size.width * 0.3f, size.height * 1.05f,
                size.width * 0.62f, size.height * 0.2f,
                size.width, 0f
            )
        }
        drawPath(line, color, style = Stroke(width = size.height * 0.13f))
    }
}

@Composable
private fun NoPartnersFound() {
    val extendedColors = MetroTransitTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = extendedColors.glass,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = extendedColors.textSecondary,
                modifier = Modifier.padding(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No partners found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Try another keyword or category",
            style = MaterialTheme.typography.bodySmall,
            color = extendedColors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(LocalJourneyBarInset.current))
    }
}
