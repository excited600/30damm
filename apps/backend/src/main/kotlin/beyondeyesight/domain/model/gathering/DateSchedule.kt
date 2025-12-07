package beyondeyesight.domain.model.gathering

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class DateSchedule(
    val summaries: List<DateScheduleSummary>
) {
    class DateScheduleSummary(
        val date: LocalDate,
        val startTime: LocalTime,
        val duration: Duration?
    )
}