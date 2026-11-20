package com.mycompany.testflight.feature.applist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mycompany.testflight.designsystem.tokens.FriendlyDimens
import com.mycompany.testflight.designsystem.tokens.JakartaSans
import com.mycompany.testflight.designsystem.tokens.TestflightColors
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.feature.applist.R

@Composable
fun AppIcon(
    app: TestApp,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    cornerRadius: Dp = FriendlyDimens.TileCorner,
    letterSize: TextUnit = 17.sp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = app.name.take(1).uppercase(),
            style = TextStyle(
                fontFamily = JakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = letterSize,
            ),
            color = TestflightColors.Navy.copy(alpha = 0.82f),
        )
        val iconUrl = app.iconUrl
        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl,
                contentDescription = stringResource(
                    R.string.app_icon_content_description,
                    app.name,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
