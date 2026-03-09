package org.nikol.roasti.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Palette — sourced from Tailwind v4 oklch variables in theme.css
// Hex values are sRGB approximations of the oklch originals
// ---------------------------------------------------------------------------

// Orange (brand)
val Orange50  = Color(0xFFFFF7ED)  // oklch(98% 0.016 73.684)
val Orange100 = Color(0xFFFFEDD5)  // oklch(95.4% 0.038 75.164)
val Orange500 = Color(0xFFF97316)  // oklch(70.5% 0.213 47.604)
val Orange600 = Color(0xFFEA580C)  // oklch(64.6% 0.222 41.116) — primary brand
val Orange700 = Color(0xFFC2410C)  // oklch(55.3% 0.195 38.402)
val Orange800 = Color(0xFF9A3412)  // oklch(47% 0.157 37.304)
val Orange900 = Color(0xFF7C2D12)  // oklch(40.8% 0.123 38.172)

// Red
val Red50  = Color(0xFFFEF2F2)     // oklch(97.1% 0.013 17.38)
val Red500 = Color(0xFFEF4444)     // oklch(63.7% 0.237 25.331)
val Red600 = Color(0xFFDC2626)     // oklch(57.7% 0.245 27.325)
val Red700 = Color(0xFFB91C1C)     // oklch(50.5% 0.213 27.518)

// Emerald
val Emerald50  = Color(0xFFECFDF5) // oklch(97.9% 0.021 166.113)
val Emerald100 = Color(0xFFD1FAE5) // oklch(95% 0.052 163.051)
val Emerald200 = Color(0xFFA7F3D0) // oklch(90.5% 0.093 164.15)
val Emerald600 = Color(0xFF059669) // oklch(59.6% 0.145 163.225)
val Emerald800 = Color(0xFF065F46) // oklch(43.2% 0.095 166.913)

// Blue
val Blue100 = Color(0xFFDBEAFE)    // oklch(93.2% 0.032 255.585)
val Blue800 = Color(0xFF1E40AF)    // oklch(42.4% 0.199 265.638)

// Stone (warm neutral — coffee theme)
val Stone50  = Color(0xFFFAFAF9)   // oklch(98.5% 0.001 106.423)
val Stone100 = Color(0xFFF5F5F4)   // oklch(97% 0.001 106.424)
val Stone200 = Color(0xFFE7E5E4)   // oklch(92.3% 0.003 48.717)
val Stone300 = Color(0xFFD6D3D1)   // oklch(86.9% 0.005 56.366)
val Stone400 = Color(0xFFA8A29E)   // oklch(70.9% 0.01 56.259)
val Stone500 = Color(0xFF78716C)   // oklch(55.3% 0.013 58.071)
val Stone600 = Color(0xFF57534E)   // oklch(44.4% 0.011 73.639)
val Stone700 = Color(0xFF44403C)   // oklch(37.4% 0.01 67.558)
val Stone800 = Color(0xFF292524)   // oklch(26.8% 0.007 34.298)
val Stone900 = Color(0xFF1C1917)   // oklch(21.6% 0.006 56.043)

// Gray
val Gray100 = Color(0xFFF3F4F6)    // oklch(96.7% 0.003 264.542)
val Gray500 = Color(0xFF6B7280)    // oklch(55.1% 0.027 264.364)

// ---------------------------------------------------------------------------
// Semantic — light scheme (from theme.css :root)
// ---------------------------------------------------------------------------
val LightBackground      = Color(0xFFFFFFFF)
val LightForeground      = Color(0xFF030213)
val LightCard            = Color(0xFFFFFFFF)
val LightPrimaryFg       = Color(0xFFFFFFFF)
val LightSecondary       = Color(0xFFEEF0F8)  // oklch(0.95 0.0058 264.53)
val LightMuted           = Color(0xFFECECF0)
val LightMutedFg         = Color(0xFF717182)
val LightAccent          = Color(0xFFE9EBEF)
val LightBorder          = Color(0x1A000000)  // rgba(0,0,0,0.1)
val LightInputBg         = Color(0xFFF3F3F5)

// ---------------------------------------------------------------------------
// Semantic — dark scheme (from theme.css .dark)
// ---------------------------------------------------------------------------
val DarkBackground       = Color(0xFF141414)  // oklch(0.145 0 0)
val DarkForeground       = Color(0xFFF9F9F9)  // oklch(0.985 0 0)
val DarkCard             = Color(0xFF141414)
val DarkPrimaryFg        = Color(0xFF242424)  // oklch(0.205 0 0)
val DarkSurface          = Color(0xFF2E2E2E)  // oklch(0.269 0 0)
val DarkMutedFg          = Color(0xFF8D8D8D)  // oklch(0.708 0 0)
val DarkDestructive      = Color(0xFF7A2020)  // oklch(0.396 0.141 25.723)
val DarkBorder           = Color(0xFF2E2E2E)
