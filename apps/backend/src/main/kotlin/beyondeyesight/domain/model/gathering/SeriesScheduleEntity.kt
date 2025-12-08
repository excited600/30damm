package beyondeyesight.domain.model.gathering

import beyondeyesight.domain.exception.DataIntegrityException
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "series_schedules")
class SeriesScheduleEntity(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val scheduleType: ScheduleType,
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    val openDayOfWeek: DayOfWeek?,
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    val startDayOfWeek: DayOfWeek?,
    @Column(nullable = true)
    val scheduleStartDate: LocalDate?,
    @Column(nullable = true)
    val scheduleEndDate: LocalDate?,
    @Column(nullable = false)
    val openDate: LocalDate?,
    @Column(nullable = true)
    val startDate: LocalDate?,
    @Column(nullable = false)
    val startTime: LocalTime,
    @Column(nullable = true)
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    val duration: Duration?,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_uuid", nullable = false)
    val seriesEntity: SeriesEntity,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val seq: Long? = null

    // TODO: 테스트
    @PostLoad
    fun validate() {
        when (scheduleType) {
            ScheduleType.WEEKLY -> {
                if (startDayOfWeek == null) {
                    throw DataIntegrityException("series_schedules", "startDayOfWeek must not be null when series is scheduled weekly")
                }
                if (openDayOfWeek == null) {
                    throw DataIntegrityException("series_schedules", "openDayOfWeek must not be null when series is scheduled weekly")
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
                if (startDate != null) {
                    throw DataIntegrityException("series_schedules", "startDate must be null when series is scheduled weekly")
                }
                if (openDate != null) {
                    throw DataIntegrityException("series_schedules", "openDate must be null when series is scheduled weekly")
                }
            }
            ScheduleType.DATE -> {
                if (openDayOfWeek != null) {
                    throw DataIntegrityException("series_schedules", "openDayOfWeek must be null when series is scheduled date")
                }
                if (startDayOfWeek != null) {
                    throw DataIntegrityException("series_schedules", "startDayOfWeek must be null when series is scheduled date")
                }
                if (scheduleStartDate != null) {
                    throw DataIntegrityException("series_schedules", "scheduleStartDate must be null when series is scheduled date")
                }
                if (scheduleEndDate != null) {
                    throw DataIntegrityException("series_schedules", "scheduleEndDate must be null when series is scheduled date")
                }
                if (startDate == null) {
                    throw DataIntegrityException("series_schedules", "startDate must not be null when series is scheduled date")
                }
                if (openDate == null) {
                    throw DataIntegrityException("series_schedules", "openDate must not be null when series is scheduled date")
                }
            }
        }
    }
}