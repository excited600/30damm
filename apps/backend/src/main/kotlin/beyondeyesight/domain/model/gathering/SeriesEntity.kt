package beyondeyesight.domain.model.gathering

import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Duration
import java.util.UUID

@Entity
@Table(name = "series")
class SeriesEntity(
    override val uuid: UUID,
    @Column(nullable = false)
    val approveType: GatheringEntity.ApproveType,
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
    val fee: Int,
    @Column(nullable = false)
    val discountEnabled: Boolean,
    @Column(nullable = false)
    val offline: Boolean,
    @Column(nullable = false)
    val place: String,
    @Column(nullable = false)
    val category: GatheringEntity.Category,
    @Column(nullable = false)
    val subCategory: GatheringEntity.SubCategory,
    @Column(nullable = false)
    val imageUrl: String,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false)
    val introduction: String,

) : BaseEntity(uuid)