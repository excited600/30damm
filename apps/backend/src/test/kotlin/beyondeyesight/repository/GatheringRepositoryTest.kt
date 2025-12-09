package beyondeyesight.repository

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.gathering.Category
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.GatheringFilter
import beyondeyesight.domain.model.gathering.SubCategory
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.infra.repository.gathering.GatheringRepositoryImpl
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.time.Duration
import java.time.LocalDateTime
import kotlin.test.Test

@RepositoryTest
@Import(GatheringRepositoryImpl::class)
class GatheringRepositoryTest(
) {
    @Autowired
    lateinit var gatheringRepository: GatheringRepository

    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    fun scrollGatherings() {

        val gathering1 = gatheringRepository.save(
            GatheringEntity.open(
                hostUuid = uuidV7(),
                approveType = GatheringEntity.ApproveType.APPROVAL,
                minCapacity = 1,
                maxCapacity = 10,
                genderRatioEnabled = false,
                minAge = 20,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 10_000,
                discountEnabled = false,
                offline = true,
                place = "place",
                category = Category.LANGUAGE,
                subCategory = SubCategory.HOME_PARTY,
                imageUrl = "image",
                title = "title",
                introduction = "introduction",
                startDateTime = LocalDateTime.now(),
                duration = Duration.ofHours(2),
            )
        )

        val gathering2 = gatheringRepository.save(
            GatheringEntity.open(
                hostUuid = uuidV7(),
                approveType = GatheringEntity.ApproveType.APPROVAL,
                minCapacity = 1,
                maxCapacity = 10,
                genderRatioEnabled = false,
                minAge = 20,
                maxAge = 40,
                maxMaleCount = null,
                maxFemaleCount = null,
                fee = 10_000,
                discountEnabled = false,
                offline = true,
                place = "place",
                category = Category.LANGUAGE,
                subCategory = SubCategory.HOME_PARTY,
                imageUrl = "image",
                title = "title",
                introduction = "introduction",
                startDateTime = LocalDateTime.now(),
                duration = Duration.ofHours(2),
            )
        )

        entityManager.flush()
        entityManager.clear()

        val result = gatheringRepository.scroll(
            cursor = null,
            size = 1,
            filter = GatheringFilter(
                categories = null,
                guestCount = null,
                dayOfWeek = null,
                startDate = null,
                endDate = null,
                location = null,
                startAge = null,
                endAge = null,
                genderRatioEnabled = null,
                minCapacity = null,
                maxCapacity = null,
                minFee = null,
                maxFee = null
            )
        )

        assertThat(result.items.size).isEqualTo(1)
    }

}