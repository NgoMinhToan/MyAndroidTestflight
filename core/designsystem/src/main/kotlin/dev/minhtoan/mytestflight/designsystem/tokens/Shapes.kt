package dev.minhtoan.mytestflight.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val FriendlyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(19.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val PillShape = RoundedCornerShape(50)

object FriendlyDimens {
    val SheetCorner = 28.dp
    val CardCorner = 19.dp
    val TileCorner = 16.dp
    val DetailTileCorner = 24.dp
    val InputCorner = 14.dp
    val HeaderBleed = 28.dp
    val ContentPadding = 18.dp
    val TrackHeight = 2.dp
    val Hairline = 1.dp
    val MinTouch = 48.dp
}
