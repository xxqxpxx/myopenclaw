package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import com.myopenclaw.util.UrlLauncher
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import myopenclaw.composeapp.generated.resources.Res
 import myopenclaw.composeapp.generated.resources._72ba13596a954598f0c75a26014146d324940278

@Composable
fun OnboardingScreen(
    onNavigateToEmailAuth: () -> Unit,
    onNavigateToGoogleAuth: () -> Unit,
    onNavigateToAppleAuth: () -> Unit = {},
    urlLauncher: UrlLauncher = koinInject()
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1)
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        // Calculate responsive dimensions based on screen height
        val responsiveDimensions = calculateResponsiveDimensions(screenHeight)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .padding(top = responsiveDimensions.topPadding)
                    .size(responsiveDimensions.iconSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Grey10),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable._72ba13596a954598f0c75a26014146d324940278),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(responsiveDimensions.iconImageSize),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(responsiveDimensions.spacing1))

            // Main Title
            Text(
                text = "Signalwhisper:\nInstant and Smarter Trade Analysis with AI",
                style = TextStyle(
                    fontSize = responsiveDimensions.titleFontSize,
                    lineHeight = responsiveDimensions.titleLineHeight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(responsiveDimensions.spacing2))

            // Description
            Text(
                text = "Snap any trading chart and get expert market insights, daily buy/hold/sell signals, and AI-powered analysis to make confident trading decisions in seconds.",
                style = TextStyle(
                    fontSize = responsiveDimensions.descriptionFontSize,
                    lineHeight = responsiveDimensions.descriptionLineHeight,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.32).sp,
                    color = White.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(responsiveDimensions.spacing3))

            // Rating Card
            RatingCard(responsiveDimensions)

            Spacer(modifier = Modifier.height(responsiveDimensions.spacing4))

            // CTA Buttons
            AuthButtons(
                onEmailClick = onNavigateToEmailAuth,
                onGoogleClick = onNavigateToGoogleAuth,
                onAppleClick = onNavigateToAppleAuth,
                responsiveDimensions = responsiveDimensions
            )

            Spacer(modifier = Modifier.height(responsiveDimensions.spacing5))

            // Terms and Conditions
            TermsText(responsiveDimensions, urlLauncher)

            Spacer(modifier = Modifier.height(responsiveDimensions.bottomPadding))
        }
    }
}

/**
 * Data class to hold all responsive dimension values
 */
private data class ResponsiveDimensions(
    val topPadding: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp,
    val iconImageSize: androidx.compose.ui.unit.Dp,
    val spacing1: androidx.compose.ui.unit.Dp,
    val spacing2: androidx.compose.ui.unit.Dp,
    val spacing3: androidx.compose.ui.unit.Dp,
    val spacing4: androidx.compose.ui.unit.Dp,
    val spacing5: androidx.compose.ui.unit.Dp,
    val bottomPadding: androidx.compose.ui.unit.Dp,
    val titleFontSize: androidx.compose.ui.unit.TextUnit,
    val titleLineHeight: androidx.compose.ui.unit.TextUnit,
    val descriptionFontSize: androidx.compose.ui.unit.TextUnit,
    val descriptionLineHeight: androidx.compose.ui.unit.TextUnit,
    val ratingNumberSize: androidx.compose.ui.unit.TextUnit,
    val ratingLabelSize: androidx.compose.ui.unit.TextUnit,
    val downloadsSize: androidx.compose.ui.unit.TextUnit,
    val buttonHeight: androidx.compose.ui.unit.Dp,
    val termsTextSize: androidx.compose.ui.unit.TextUnit,
    val sizeCategory: ScreenSizeCategory
)

/**
 * Screen size categories for different device types
 */
private enum class ScreenSizeCategory {
    SMALL,    // < 667dp (iPhone SE, small phones)
    MEDIUM,   // 667-844dp (iPhone 13/14, standard phones)
    LARGE     // > 844dp (iPhone Pro Max, large phones, tablets)
}

/**
 * Calculate responsive dimensions based on screen height
 * Reference: iPhone 14/15 = 852dp height (standard target)
 */
private fun calculateResponsiveDimensions(screenHeight: androidx.compose.ui.unit.Dp): ResponsiveDimensions {
    // Determine screen size category
    val category = when {
        screenHeight < 667.dp -> ScreenSizeCategory.SMALL
        screenHeight <= 844.dp -> ScreenSizeCategory.MEDIUM
        else -> ScreenSizeCategory.LARGE
    }

    // Calculate scale factor based on standard iPhone 14 height (852dp)
    val standardHeight = 852f
    val scaleFactor = (screenHeight.value / standardHeight).coerceIn(0.75f, 1.3f)

    return when (category) {
        ScreenSizeCategory.SMALL -> ResponsiveDimensions(
            topPadding = (60 * scaleFactor).dp.coerceAtLeast(50.dp),
            iconSize = 64.dp,
            iconImageSize = 50.dp,
            spacing1 = (20 * scaleFactor).dp.coerceAtLeast(16.dp),
            spacing2 = (12 * scaleFactor).dp.coerceAtLeast(10.dp),
            spacing3 = (18 * scaleFactor).dp.coerceAtLeast(16.dp),
            spacing4 = (24 * scaleFactor).dp.coerceAtLeast(20.dp),
            spacing5 = (18 * scaleFactor).dp.coerceAtLeast(16.dp),
            bottomPadding = (20 * scaleFactor).dp.coerceAtLeast(16.dp),
            titleFontSize = 18.sp,
            titleLineHeight = 26.sp,
            descriptionFontSize = 14.sp,
            descriptionLineHeight = 20.sp,
            ratingNumberSize = 28.sp,
            ratingLabelSize = 11.sp,
            downloadsSize = 14.sp,
            buttonHeight = 52.dp,
            termsTextSize = 12.sp,
            sizeCategory = category
        )
        ScreenSizeCategory.MEDIUM -> ResponsiveDimensions(
            topPadding = (85 * scaleFactor).dp,
            iconSize = 72.dp,
            iconImageSize = 56.dp,
            spacing1 = (30 * scaleFactor).dp,
            spacing2 = (16 * scaleFactor).dp,
            spacing3 = (24 * scaleFactor).dp,
            spacing4 = (32 * scaleFactor).dp,
            spacing5 = (24 * scaleFactor).dp,
            bottomPadding = (32 * scaleFactor).dp,
            titleFontSize = 20.sp,
            titleLineHeight = 28.sp,
            descriptionFontSize = 16.sp,
            descriptionLineHeight = 24.sp,
            ratingNumberSize = 32.sp,
            ratingLabelSize = 12.sp,
            downloadsSize = 16.sp,
            buttonHeight = 56.dp,
            termsTextSize = 14.sp,
            sizeCategory = category
        )
        ScreenSizeCategory.LARGE -> ResponsiveDimensions(
            topPadding = (100 * scaleFactor).dp.coerceAtMost(120.dp),
            iconSize = 80.dp,
            iconImageSize = 64.dp,
            spacing1 = (36 * scaleFactor).dp.coerceAtMost(44.dp),
            spacing2 = (20 * scaleFactor).dp.coerceAtMost(24.dp),
            spacing3 = (28 * scaleFactor).dp.coerceAtMost(32.dp),
            spacing4 = (40 * scaleFactor).dp.coerceAtMost(48.dp),
            spacing5 = (28 * scaleFactor).dp.coerceAtMost(32.dp),
            bottomPadding = (40 * scaleFactor).dp.coerceAtMost(48.dp),
            titleFontSize = 22.sp,
            titleLineHeight = 30.sp,
            descriptionFontSize = 17.sp,
            descriptionLineHeight = 26.sp,
            ratingNumberSize = 36.sp,
            ratingLabelSize = 13.sp,
            downloadsSize = 17.sp,
            buttonHeight = 60.dp,
            termsTextSize = 15.sp,
            sizeCategory = category
        )
    }
}

@Composable
private fun RatingCard(responsiveDimensions: ResponsiveDimensions) {
    // Determine layout based on screen size
    val useCompactLayout = responsiveDimensions.sizeCategory == ScreenSizeCategory.SMALL

    if (useCompactLayout) {
        // Compact layout for small screens - stack vertically if needed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Dark6)
                .border(
                    width = 1.dp,
                    color = Dark2,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star Icon
            Text(
                text = "⭐",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Rating Section
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "4.9",
                    style = TextStyle(
                        fontSize = responsiveDimensions.ratingNumberSize,
                        fontWeight = FontWeight.Bold,
                        color = Green2,
                        lineHeight = (responsiveDimensions.ratingNumberSize.value + 4).sp
                    )
                )
                Text(
                    text = "Average rating",
                    style = TextStyle(
                        fontSize = responsiveDimensions.ratingLabelSize,
                        fontWeight = FontWeight.Medium,
                        color = Green2.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Dark2)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Downloads Text
            Text(
                text = "Over 100k+\nDownloads",
                style = TextStyle(
                    fontSize = responsiveDimensions.downloadsSize,
                    fontWeight = FontWeight.Medium,
                    color = White,
                    lineHeight = (responsiveDimensions.downloadsSize.value + 4).sp
                ),
                textAlign = TextAlign.Start
            )
        }
    } else {
        // Standard layout for medium and large screens
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Dark6)
                .border(
                    width = 1.dp,
                    color = Dark2,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star Icon
            Text(
                text = "⭐",
                fontSize = if (responsiveDimensions.sizeCategory == ScreenSizeCategory.LARGE) 28.sp else 24.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Rating Section
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "4.9",
                    style = TextStyle(
                        fontSize = responsiveDimensions.ratingNumberSize,
                        fontWeight = FontWeight.Bold,
                        color = Green2,
                        lineHeight = (responsiveDimensions.ratingNumberSize.value + 4).sp
                    )
                )
                Text(
                    text = "Average rating",
                    style = TextStyle(
                        fontSize = responsiveDimensions.ratingLabelSize,
                        fontWeight = FontWeight.Medium,
                        color = Green2.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(Dark2)
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Downloads Text
            Text(
                text = "Over 100k+\nDownloads",
                style = TextStyle(
                    fontSize = responsiveDimensions.downloadsSize,
                    fontWeight = FontWeight.Medium,
                    color = White,
                    lineHeight = (responsiveDimensions.downloadsSize.value + 6).sp
                ),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun AuthButtons(
    onEmailClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    responsiveDimensions: ResponsiveDimensions
) {
    val buttonFontSize = when (responsiveDimensions.sizeCategory) {
        ScreenSizeCategory.SMALL -> 15.sp
        ScreenSizeCategory.MEDIUM -> 16.sp
        ScreenSizeCategory.LARGE -> 17.sp
    }

    val iconSize = when (responsiveDimensions.sizeCategory) {
        ScreenSizeCategory.SMALL -> 18.dp
        ScreenSizeCategory.MEDIUM -> 20.dp
        ScreenSizeCategory.LARGE -> 22.dp
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email Button
        Button(
            onClick = onEmailClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(responsiveDimensions.buttonHeight)
                .testTag("onboarding_email_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green2,
                contentColor = White
            ),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✉",
                    fontSize = iconSize.value.sp,
                    color = White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign up with Email",
                    style = TextStyle(
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Google Button
        Button(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(responsiveDimensions.buttonHeight)
                .border(
                    width = 1.dp,
                    color = Grey96,
                    shape = CircleShape
                )
                .testTag("onboarding_google_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = White,
                contentColor = Grey900
            ),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable._72ba13596a954598f0c75a26014146d324940278),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign up with Google",
                    style = TextStyle(
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = Grey900
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apple Button
        Button(
            onClick = onAppleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(responsiveDimensions.buttonHeight)
                .border(
                    width = 1.dp,
                    color = Grey96,
                    shape = CircleShape
                )
                .testTag("onboarding_apple_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = White
            ),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\uF8FF",
                    fontSize = iconSize.value.sp,
                    color = White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign up with Apple",
                    style = TextStyle(
                        fontSize = buttonFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                )
            }
        }
    }
}

@Composable
private fun TermsText(responsiveDimensions: ResponsiveDimensions, urlLauncher: UrlLauncher) {
    val annotatedString = buildAnnotatedString {
        append("By continuing, you agree to our ")

        pushStringAnnotation(tag = "terms", annotation = "https://myopenclaw.com/terms")
        withStyle(
            style = SpanStyle(
                color = White,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Terms and conditions")
        }
        pop()

        append(" and ")

        pushStringAnnotation(tag = "privacy", annotation = "https://myopenclaw.com/privacy")
        withStyle(
            style = SpanStyle(
                color = White,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("privacy policy")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = TextStyle(
            fontSize = responsiveDimensions.termsTextSize,
            lineHeight = (responsiveDimensions.termsTextSize.value + 6).sp,
            fontWeight = FontWeight.Normal,
            color = White.copy(alpha = 0.7f)
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "terms",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                urlLauncher.openUrl(it.item)
                return@ClickableText
            }
            annotatedString.getStringAnnotations(
                tag = "privacy",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                urlLauncher.openUrl(it.item)
            }
        },
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}
