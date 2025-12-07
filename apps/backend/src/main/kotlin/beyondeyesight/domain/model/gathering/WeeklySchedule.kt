package beyondeyesight.domain.model.gathering

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class WeeklySchedule(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val summaries: List<WeeklyScheduleSummary>
) {
    class WeeklyScheduleSummary(
        val dayOfWeek: DayOfWeek,
        val startTime: LocalTime,
        val duration: Duration?
    )
}