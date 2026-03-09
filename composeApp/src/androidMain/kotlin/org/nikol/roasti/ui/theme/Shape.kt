package org.nikol.roasti.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Mapped from theme.css radius tokens (1rem = 16dp base)
// --radius-xs: 0.125rem = 2dp
// --radius-sm: calc(0.625rem - 4px) = 6dp
// --radius-md: calc(0.625rem - 2px) = 8dp
// --radius-lg: 0.625rem             = 10dp  (base --radius)
// --radius-xl: calc(0.625rem + 4px) = 14dp
// --radius-3xl: 1.5rem              = 24dp

val RoastiShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // radius-xs
    small      = RoundedCornerShape(6.dp),   // radius-sm
    medium     = RoundedCornerShape(8.dp),   // radius-md
    large      = RoundedCornerShape(10.dp),  // radius-lg (base)
    extraLarge = RoundedCornerShape(14.dp),  // radius-xl
)

// Use directly where needed (e.g. bottom sheet, modal)
val ShapeXxl = RoundedCornerShape(24.dp)     // radius-3xl
