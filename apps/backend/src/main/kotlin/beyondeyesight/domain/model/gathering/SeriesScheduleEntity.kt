package beyondeyesight.domain.model.gathering

import beyondeyesight.domain.exception.DataIntegrityException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.PostLoad
import jakarta.persistence.Table
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.time.Duration

@Entity
@Table(name = "series_schedules")
class SeriesScheduleEntity(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val scheduleType: ScheduleType,
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    val dayOfWeek: DayOfWeek?,
    @Column(nullable = true)
    val scheduleStartDate: LocalDate?,
    @Column(nullable = true)
    val scheduleEndDate: LocalDate?,
    @Column(nullable = true)
    val date: LocalDate?,
    @Column(nullable = false)
    val time: LocalTime,
    @Column(nullable = true)
    val duration: Duration?,
    @Column(nullable = false)
    val scheduleUuid: UUID?
) {
    init {
        validate()
    }

    // TODO: 테스트
    @PostLoad
    fun validate() {
        when (scheduleType) {
            ScheduleType.WEEKLY -> {
                if (dayOfWeek == null) {
                    throw DataIntegrityException("series_schedules", "dayOfWeek must not be null when series is scheduled weekly")
                }
                if (scheduleStartDate == null) {
                    throw DataIntegrityException("series_schedules", "scheduleStartDate must not be null when series is scheduled weekly")
                }
                if (scheduleEndDate == null) {
                    throw DataIntegrityException("series_schedules", "scheduleEndDate must not be null when series is scheduled weekly")
                }

                if (scheduleStartDate!! > scheduleEndDate) {
                    throw DataIntegrityException("series_schedules", "scheduleStartDate must be before or equal to scheduleEndDate when series is scheduled weekly")
                }

                if (date == null) {
                    throw DataIntegrityException("series_schedules", "date must be null when series is scheduled weekly")
                }
            }
            ScheduleType.DATE -> {
                if (dayOfWeek != null) {
                    throw DataIntegrityException("series_schedules", "dayOfWeek must be null when series is scheduled date")
                }
                if (scheduleStartDate != null) {
                    throw DataIntegrityException("series_schedules", "scheduleStartDate must be null when series is scheduled date")
                }
                if (scheduleEndDate != null) {
                    throw DataIntegrityException("series_schedules", "scheduleEndDate must be null when series is scheduled date")
                }
                if (date == null) {
                    throw DataIntegrityException("series_schedules", "date must not be null when series is scheduled date")
                }
            }
        }
    }
}