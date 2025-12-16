package beyondeyesight.domain.model.gathering

import java.time.DayOfWeek
import java.time.LocalDate

class GatheringFilter(
    val statuses: List<Status>?,
    val categories: List<Category>?,
    val guestCount: Int?,
    val dayOfWeek: DayOfWeek?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val location: String?,
    val startAge: Int?,
    val endAge: Int?,
    val genderRatioEnabled: Boolean?,
    val minCapacity: Int?,
    val maxCapacity: Int?,
    val minFee: Int?,
    val maxFee: Int?
)