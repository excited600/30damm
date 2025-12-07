package beyondeyesight.config

import java.time.Duration
import java.time.LocalTime

private const val MILLIS_PER_HOUR = 3_600_000L

// TODO: TEST
fun Duration?.toHoursFloat(): Float? =
    this?.toMillis()?.div(MILLIS_PER_HOUR.toFloat())

fun Float?.toDurationHours(): Duration? =
    this?.let { Duration.ofMillis((it * MILLIS_PER_HOUR).toLong()) }

fun LocalTime.isThirtyMinuteInterval(): Boolean =
    minute % 30 == 0 && second == 0 && nano == 0