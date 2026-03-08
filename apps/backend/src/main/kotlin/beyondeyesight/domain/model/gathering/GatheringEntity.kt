package beyondeyesight.domain.model.gathering

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "gatherings")
class GatheringEntity(
    uuid: UUID,
    @Column(nullable = false)
    val hostUuid: UUID,
    @Column(nullable = false)
    val minCapacity: Int,
    @Column(nullable = false)
    val maxCapacity: Int,
    @Column(nullable = false)
    val genderRatioEnabled: Boolean,
    @Column(nullable = true)
    val maxMaleCount: Int?,
    @Column(nullable = true)
    val maxFemaleCount: Int?,
    @Column(nullable = false)
    val totalGuests: Int,
    @Column(nullable = false)
    val fee: Int,
    @Column(name = "is_split", nullable = false)
    val isSplit: Boolean,
    @Column(nullable = true)
    val place: String?,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status,
    @Column(name = "image_url", nullable = true)
    val imageUrl: String?,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = true)
    val description: String?,
    @Column(nullable = false)
    var clickCount: Int,
    @Column(name = "start_date_time", nullable = true)
    val startDateTime: LocalDateTime?,
    @Column(columnDefinition = "interval", nullable = true)
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    val duration: Duration?,
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    val dayOfWeek: DayOfWeek?,
    @Column(nullable = false)
    val score: Int,
): BaseEntity(uuid = uuid) {

    fun close() {
        this.status = Status.CLOSED
    }

    fun isFree(): Boolean {
        return fee == 0
    }

    companion object {
        fun open(
            hostUuid: UUID,
            title: String,
            description: String?,
            category: Category,
            location: String?,
            startDateTime: LocalDateTime?,
            duration: Duration?,
            minCapacity: Int,
            maxCapacity: Int,
            genderRatioEnabled: Boolean,
            maxMaleCount: Int?,
            maxFemaleCount: Int?,
            isFree: Boolean,
            price: Int?,
            isSplit: Boolean,
            imageUrl: String?,
        ): GatheringEntity {
            val fee = if (isFree) 0 else (price ?: 0)
            return GatheringEntity(
                uuid = uuidV7(),
                hostUuid = hostUuid,
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                genderRatioEnabled = genderRatioEnabled,
                maxMaleCount = maxMaleCount,
                maxFemaleCount = maxFemaleCount,
                totalGuests = INITIAL_TOTAL_GUESTS,
                fee = fee,
                isSplit = isSplit,
                place = location,
                category = category,
                status = Status.OPEN,
                imageUrl = imageUrl,
                title = title,
                description = description,
                clickCount = INITIAL_CLICK_COUNT,
                startDateTime = startDateTime,
                duration = duration,
                dayOfWeek = startDateTime?.dayOfWeek,
                score = 0,
            )
        }

        const val INITIAL_TOTAL_GUESTS = 1
        const val INITIAL_CLICK_COUNT = 0
        const val RESOURCE_NAME = "gatherings"
    }

}
