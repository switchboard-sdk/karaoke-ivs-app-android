package com.synervoz.switchboardsampleapp.karaokewithivs.ui

import kotlin.math.roundToInt

/** Formats a duration in seconds as e.g. "2m 19s". */
fun formatTime(seconds: Double): String {
    val safe = if (seconds.isFinite() && seconds > 0) seconds else 0.0
    return "${(safe / 60).toInt()}m ${(safe % 60).roundToInt()}s"
}
