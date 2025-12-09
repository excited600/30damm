package beyondeyesight.domain.model.gathering

import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import beyondeyesight.config.uuidV7
import java.util.UUID

@Entity
@Table(name = "gatherings")
class GatheringEntity(
    uuid: UUID,
    @Column(nullable = false)
    val hostUuid: UUID,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val approveType: ApproveType,
    @Column(nullable = false)
    val minCapacity: Int,
    @Column(nullable = false)
    val maxCapacity: Int,
    @Column(nullable = false)
    val genderRatioEnabled: Boolean,
    @Column(nullable = false)
    val minAge: Int,
    @Column(nullable = false)
    val maxAge: Int,
    @Column(nullable = true)
    val maxMaleCount: Int?,
    @Column(nullable = true)
    val maxFemaleCount: Int?,
    @Column(nullable = false)
    val totalGuests: Int,
    @Column(nullable = false)
    val fee: Int,
    @Column(nullable = false)
    val discountEnabled: Boolean,
    @Column(nullable = false)
    val offline: Boolean,
    @Column(nullable = false)
    val place: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val subCategory: SubCategory,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status,
    @Column(nullable = false)
    val imageUrl: String,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false)
    val introduction: String,
    @Column(nullable = false)
    var clickCount: Int,
    @Column(nullable = false)
    val startDateTime: LocalDateTime,
    @Column(columnDefinition = "interval", nullable = true)
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    val duration: Duration?,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val dayOfWeek: DayOfWeek,
): BaseEntity(uuid = uuid) {

    fun close() {
        this.status = Status.CLOSED
    }

    companion object {
        fun open(
            hostUuid: UUID,
            approveType: ApproveType,
            minCapacity: Int,
            maxCapacity: Int,
            genderRatioEnabled: Boolean,
            minAge: Int,
            maxAge: Int,
            maxMaleCount: Int?,
            maxFemaleCount: Int?,
            fee: Int,
            discountEnabled: Boolean,
            offline: Boolean,
            place: String,
            category: Category,
            subCategory: SubCategory,
            imageUrl: String,
            title: String,
            introduction: String,
            startDateTime: LocalDateTime,
            duration: Duration?,
        ): GatheringEntity {
            return GatheringEntity(
                uuid = uuidV7(),
                hostUuid = hostUuid,
                approveType = approveType,
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                genderRatioEnabled = genderRatioEnabled,
                minAge = minAge,
                maxAge = maxAge,
                maxMaleCount = maxMaleCount,
                maxFemaleCount = maxFemaleCount,
                totalGuests = INITIAL_TOTAL_GUESTS,
                fee = fee,
                discountEnabled = discountEnabled,
                offline = offline,
                place = place,
                category = category,
                subCategory = subCategory,
                status = Status.OPEN,
                imageUrl = imageUrl,
                title = title,
                introduction = introduction,
                clickCount = INITIAL_CLICK_COUNT,
                startDateTime = startDateTime,
                duration = duration,
                dayOfWeek = startDateTime.dayOfWeek
            )
        }

        const val INITIAL_TOTAL_GUESTS = 1
        const val INITIAL_CLICK_COUNT = 0
    }

    enum class ApproveType {
        FIRST_IN,
        APPROVAL;

    }

    enum class SubCategory {
        HOME_PARTY,
    }

    enum class Status {
        OPEN, CLOSED, IN_PROGRESS, COMPLETED
    }
}