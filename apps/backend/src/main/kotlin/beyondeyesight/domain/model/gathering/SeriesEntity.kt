package beyondeyesight.domain.model.gathering

import beyondeyesight.domain.model.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "series")
class SeriesEntity(
    override val uuid: UUID,
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
    val fee: Int,
    @Column(name = "is_split", nullable = false)
    val isSplit: Boolean,
    @Column(nullable = true)
    val place: String?,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,
    @Column(name = "image_url", nullable = true)
    val imageUrl: String?,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = true)
    val description: String?,

) : BaseEntity(uuid = uuid) {

    companion object {
        const val RESOURCE_NAME = "series"
    }
}
