package beyondeyesight.repository

import beyondeyesight.config.uuidV7
import beyondeyesight.domain.model.gathering.Category
import beyondeyesight.domain.model.gathering.GatheringCursor
import beyondeyesight.domain.model.gathering.GatheringEntity
import beyondeyesight.domain.model.gathering.GatheringFilter
import beyondeyesight.domain.model.gathering.Status
import beyondeyesight.domain.repository.gathering.GatheringRepository
import beyondeyesight.infra.repository.gathering.GatheringRepositoryImpl
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream
import kotlin.test.Test

@RepositoryTest
@Import(GatheringRepositoryImpl::class)
class GatheringRepositoryTest {
    @Autowired
    lateinit var gatheringRepository: GatheringRepository

    @Autowired
    lateinit var entityManager: EntityManager

    private fun createGathering(
        category: Category = Category.PARTY,
        startDateTime: LocalDateTime = LocalDateTime.of(2025, 1, 6, 14, 0), // Monday
        place: String = "서울 강남구",
        genderRatioEnabled: Boolean = false,
        minCapacity: Int = 2,
        maxCapacity: Int = 10,
        fee: Int = 10000,
        score: Int = 0
    ): GatheringEntity {
        return gatheringRepository.save(
            GatheringEntity(
                uuid = uuidV7(),
                hostUuid = uuidV7(),
                minCapacity = minCapacity,
                maxCapacity = maxCapacity,
                genderRatioEnabled = genderRatioEnabled,
                maxMaleCount = null,
                maxFemaleCount = null,
                totalGuests = GatheringEntity.INITIAL_TOTAL_GUESTS,
                fee = fee,
                isSplit = false,
                place = place,
                category = category,
                status = Status.OPEN,
                imageUrl = "image",
                title = "title",
                introduction = "introduction",
                description = null,
                clickCount = GatheringEntity.INITIAL_CLICK_COUNT,
                startDateTime = startDateTime,
                duration = Duration.ofHours(2),
                dayOfWeek = startDateTime.dayOfWeek,
                score = score,
            )
        )
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scrollFilterCases")
    fun scrollWithFilter(testCase: ScrollFilterCase) {
        // given
        val gatherings = testCase.gatheringSetup(this)
        flushAndClear()

        // when
        val result = gatheringRepository.scroll(
            cursor = testCase.cursor,
            size = testCase.size,
            filter = testCase.filter
        )

        // then
        assertThat(result.items.map { it.uuid }).containsExactlyElementsOf(
            testCase.expectedUuids(gatherings)
        )
        assertThat(result.hasNext).isEqualTo(testCase.expectedHasNext)

        // verify ordering: score DESC, uuid ASC
        if (result.items.size > 1) {
            for (i in 0 until result.items.size - 1) {
                val current = result.items[i]
                val next = result.items[i + 1]
                val isValidOrder = current.score > next.score ||
                        (current.score == next.score && current.uuid < next.uuid)
                assertThat(isValidOrder)
                    .withFailMessage("Ordering violated: ${current.score}/${current.uuid} should come before ${next.score}/${next.uuid}")
                    .isTrue()
            }
        }
    }

    data class ScrollFilterCase(
        val name: String,
        val gatheringSetup: (GatheringRepositoryTest) -> List<GatheringEntity>,
        val filter: GatheringFilter,
        val cursor: GatheringCursor?,
        val size: Int,
        val expectedUuids: (List<GatheringEntity>) -> List<UUID>,
        val expectedHasNext: Boolean
    ) {
        override fun toString(): String = name
    }

    @Test
    fun scrollWithCursor() {
        // given
        val g1 = createGathering(score = 100)
        val g2 = createGathering(score = 80)
        val g3 = createGathering(score = 60)
        val g4 = createGathering(score = 40)
        flushAndClear()

        // when: first page
        val firstPage = gatheringRepository.scroll(
            cursor = null,
            size = 2,
            filter = emptyFilter()
        )

        // then
        assertThat(firstPage.items.map { it.uuid }).containsExactly(g1.uuid, g2.uuid)
        assertThat(firstPage.hasNext).isTrue()
        assertThat(firstPage.cursor).isNotNull

        // when: second page using cursor
        val secondPage = gatheringRepository.scroll(
            cursor = firstPage.cursor,
            size = 2,
            filter = emptyFilter()
        )

        // then
        assertThat(secondPage.items.map { it.uuid }).containsExactly(g3.uuid, g4.uuid)
        assertThat(secondPage.hasNext).isFalse()
    }

    @Test
    fun scrollWithSameScoreCursor() {
        // given: gatherings with same score (use unique high score to isolate from other tests)
        val uniqueScore = 9999
        val g1 = createGathering(score = uniqueScore)
        val g2 = createGathering(score = uniqueScore)
        val g3 = createGathering(score = uniqueScore)
        flushAndClear()

        // expected order: score DESC, uuid ASC
        val expectedOrder = listOf(g1, g2, g3).sortedBy { it.uuid }

        // when: first page
        val firstPage = gatheringRepository.scroll(
            cursor = null,
            size = 2,
            filter = emptyFilter()
        )

        // then: first page should have first 2 items in expected order
        val firstPageItems = firstPage.items.filter { it.score == uniqueScore }
        assertThat(firstPageItems).hasSize(2)
        assertThat(firstPageItems.map { it.uuid })
            .containsExactly(expectedOrder[0].uuid, expectedOrder[1].uuid)
        assertThat(firstPage.hasNext).isTrue()
        assertThat(firstPage.cursor).isNotNull

        // when: second page using cursor from first page
        val secondPage = gatheringRepository.scroll(
            cursor = firstPage.cursor,
            size = 2,
            filter = emptyFilter()
        )

        // then: second page should have the remaining item
        val secondPageItems = secondPage.items.filter { it.score == uniqueScore }
        assertThat(secondPageItems).hasSize(1)
        assertThat(secondPageItems.map { it.uuid })
            .containsExactly(expectedOrder[2].uuid)
        assertThat(secondPage.hasNext).isFalse()
    }

    companion object {
        private fun filter(
            categories: List<Category>? = null,
            guestCount: Int? = null,
            dayOfWeek: DayOfWeek? = null,
            startDate: LocalDate? = null,
            endDate: LocalDate? = null,
            location: String? = null,
            genderRatioEnabled: Boolean? = null,
            minCapacity: Int? = null,
            maxCapacity: Int? = null,
            minFee: Int? = null,
            maxFee: Int? = null
        ) = GatheringFilter(
            statuses = listOf(Status.OPEN, Status.IN_PROGRESS),
            categories = categories,
            guestCount = guestCount,
            dayOfWeek = dayOfWeek,
            startDate = startDate,
            endDate = endDate,
            location = location,
            genderRatioEnabled = genderRatioEnabled,
            minCapacity = minCapacity,
            maxCapacity = maxCapacity,
            minFee = minFee,
            maxFee = maxFee
        )

        private fun emptyFilter() = filter()

        @JvmStatic
        fun scrollFilterCases(): Stream<ScrollFilterCase> = Stream.of(
            // 필터 없음
            ScrollFilterCase(
                name = "No filter - returns all",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(score = 100),
                        test.createGathering(score = 50)
                    )
                },
                filter = emptyFilter(),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings -> gatherings.sortedByDescending { it.score }.map { it.uuid } },
                expectedHasNext = false
            ),

            // categories 필터
            ScrollFilterCase(
                name = "Filter by single category",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(category = Category.PARTY, score = 100),
                        test.createGathering(category = Category.FOOD_DRINK, score = 90),
                        test.createGathering(category = Category.PARTY, score = 80)
                    )
                },
                filter = filter(categories = listOf(Category.PARTY)),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.category == Category.PARTY }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            ScrollFilterCase(
                name = "Filter by multiple categories",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(category = Category.PARTY, score = 100),
                        test.createGathering(category = Category.FOOD_DRINK, score = 90),
                        test.createGathering(category = Category.ACTIVITY, score = 80),
                        test.createGathering(category = Category.NONE, score = 70)
                    )
                },
                filter = filter(categories = listOf(Category.PARTY, Category.FOOD_DRINK)),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.category in listOf(Category.PARTY, Category.FOOD_DRINK) }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            // dayOfWeek 필터
            ScrollFilterCase(
                name = "Filter by dayOfWeek",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 6, 14, 0), // Monday
                            score = 100
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 7, 14, 0), // Tuesday
                            score = 90
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 13, 14, 0), // Monday
                            score = 80
                        )
                    )
                },
                filter = filter(dayOfWeek = DayOfWeek.MONDAY),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.dayOfWeek == DayOfWeek.MONDAY }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            // startDate, endDate 필터
            ScrollFilterCase(
                name = "Filter by date range",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 5, 14, 0),
                            score = 100
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 10, 14, 0),
                            score = 90
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 15, 14, 0),
                            score = 80
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 20, 14, 0),
                            score = 70
                        )
                    )
                },
                filter = filter(
                    startDate = LocalDate.of(2025, 1, 8),
                    endDate = LocalDate.of(2025, 1, 16)
                ),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter {
                        val date = it.startDateTime!!.toLocalDate()
                        date >= LocalDate.of(2025, 1, 8) && date <= LocalDate.of(2025, 1, 16)
                    }.sortedByDescending { it.score }.map { it.uuid }
                },
                expectedHasNext = false
            ),

            // location 필터
            ScrollFilterCase(
                name = "Filter by location (LIKE)",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(place = "서울 강남구", score = 100),
                        test.createGathering(place = "서울 서초구", score = 90),
                        test.createGathering(place = "부산 해운대구", score = 80)
                    )
                },
                filter = filter(location = "서울"),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.place?.contains("서울") == true }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            // genderRatioEnabled 필터
            ScrollFilterCase(
                name = "Filter by genderRatioEnabled = true",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(genderRatioEnabled = true, score = 100),
                        test.createGathering(genderRatioEnabled = false, score = 90),
                        test.createGathering(genderRatioEnabled = true, score = 80)
                    )
                },
                filter = filter(genderRatioEnabled = true),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.genderRatioEnabled }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            ScrollFilterCase(
                name = "Filter by genderRatioEnabled = false",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(genderRatioEnabled = true, score = 100),
                        test.createGathering(genderRatioEnabled = false, score = 90),
                        test.createGathering(genderRatioEnabled = false, score = 80)
                    )
                },
                filter = filter(genderRatioEnabled = false),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { !it.genderRatioEnabled }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            // capacity 필터
            ScrollFilterCase(
                name = "Filter by minCapacity",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(minCapacity = 2, score = 100),
                        test.createGathering(minCapacity = 5, score = 90),
                        test.createGathering(minCapacity = 3, score = 80)
                    )
                },
                filter = filter(minCapacity = 3),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.minCapacity >= 3 }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            ScrollFilterCase(
                name = "Filter by maxCapacity",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(maxCapacity = 10, score = 100),
                        test.createGathering(maxCapacity = 20, score = 90),
                        test.createGathering(maxCapacity = 15, score = 80)
                    )
                },
                filter = filter(maxCapacity = 15),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.maxCapacity <= 15 }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            // fee 필터
            ScrollFilterCase(
                name = "Filter by minFee",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(fee = 5000, score = 100),
                        test.createGathering(fee = 10000, score = 90),
                        test.createGathering(fee = 15000, score = 80)
                    )
                },
                filter = filter(minFee = 10000),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.fee >= 10000 }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            ScrollFilterCase(
                name = "Filter by maxFee",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(fee = 5000, score = 100),
                        test.createGathering(fee = 10000, score = 90),
                        test.createGathering(fee = 15000, score = 80)
                    )
                },
                filter = filter(maxFee = 10000),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.fee <= 10000 }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            ScrollFilterCase(
                name = "Filter by fee range (minFee and maxFee)",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(fee = 5000, score = 100),
                        test.createGathering(fee = 10000, score = 90),
                        test.createGathering(fee = 15000, score = 80),
                        test.createGathering(fee = 20000, score = 70)
                    )
                },
                filter = filter(minFee = 8000, maxFee = 16000),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.fee in 8000..16000 }
                        .sortedByDescending { it.score }
                        .map { it.uuid }
                },
                expectedHasNext = false
            ),

            // 복합 필터: category + dayOfWeek
            ScrollFilterCase(
                name = "Filter by category and dayOfWeek",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 6, 14, 0), // Monday
                            score = 100
                        ),
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 7, 14, 0), // Tuesday
                            score = 90
                        ),
                        test.createGathering(
                            category = Category.FOOD_DRINK,
                            startDateTime = LocalDateTime.of(2025, 1, 6, 14, 0), // Monday
                            score = 80
                        ),
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 13, 14, 0), // Monday
                            score = 70
                        )
                    )
                },
                filter = filter(
                    categories = listOf(Category.PARTY),
                    dayOfWeek = DayOfWeek.MONDAY
                ),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter {
                        it.category == Category.PARTY && it.dayOfWeek == DayOfWeek.MONDAY
                    }.sortedByDescending { it.score }.map { it.uuid }
                },
                expectedHasNext = false
            ),

            // 복합 필터: category + location + fee
            ScrollFilterCase(
                name = "Filter by category, location and fee range",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(
                            category = Category.PARTY,
                            place = "서울 강남구",
                            fee = 10000,
                            score = 100
                        ),
                        test.createGathering(
                            category = Category.PARTY,
                            place = "서울 서초구",
                            fee = 20000,
                            score = 90
                        ),
                        test.createGathering(
                            category = Category.FOOD_DRINK,
                            place = "서울 강남구",
                            fee = 10000,
                            score = 80
                        ),
                        test.createGathering(
                            category = Category.PARTY,
                            place = "부산 해운대구",
                            fee = 10000,
                            score = 70
                        ),
                        test.createGathering(
                            category = Category.PARTY,
                            place = "서울 마포구",
                            fee = 15000,
                            score = 60
                        )
                    )
                },
                filter = filter(
                    categories = listOf(Category.PARTY),
                    location = "서울",
                    minFee = 10000,
                    maxFee = 15000
                ),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter {
                        it.category == Category.PARTY &&
                                it.place?.contains("서울") == true &&
                                it.fee in 10000..15000
                    }.sortedByDescending { it.score }.map { it.uuid }
                },
                expectedHasNext = false
            ),

            // 복합 필터: date range + genderRatioEnabled
            ScrollFilterCase(
                name = "Filter by date range and genderRatioEnabled",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 10, 14, 0),
                            genderRatioEnabled = true,
                            score = 100
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 15, 14, 0),
                            genderRatioEnabled = true,
                            score = 90
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 10, 14, 0),
                            genderRatioEnabled = false,
                            score = 80
                        ),
                        test.createGathering(
                            startDateTime = LocalDateTime.of(2025, 1, 5, 14, 0),
                            genderRatioEnabled = true,
                            score = 70
                        )
                    )
                },
                filter = filter(
                    startDate = LocalDate.of(2025, 1, 8),
                    endDate = LocalDate.of(2025, 1, 20),
                    genderRatioEnabled = true
                ),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter {
                        val date = it.startDateTime!!.toLocalDate()
                        date >= LocalDate.of(2025, 1, 8) &&
                                date <= LocalDate.of(2025, 1, 20) &&
                                it.genderRatioEnabled
                    }.sortedByDescending { it.score }.map { it.uuid }
                },
                expectedHasNext = false
            ),

            // 페이지네이션 + 필터
            ScrollFilterCase(
                name = "Pagination with filter - hasNext true",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(category = Category.PARTY, score = 100),
                        test.createGathering(category = Category.PARTY, score = 90),
                        test.createGathering(category = Category.FOOD_DRINK, score = 85),
                        test.createGathering(category = Category.PARTY, score = 80),
                        test.createGathering(category = Category.PARTY, score = 70)
                    )
                },
                filter = filter(categories = listOf(Category.PARTY)),
                cursor = null,
                size = 2,
                expectedUuids = { gatherings ->
                    gatherings.filter { it.category == Category.PARTY }
                        .sortedByDescending { it.score }
                        .take(2)
                        .map { it.uuid }
                },
                expectedHasNext = true
            ),

            // 결과 없음
            ScrollFilterCase(
                name = "No matching results",
                gatheringSetup = { test ->
                    listOf(
                        test.createGathering(category = Category.PARTY, score = 100),
                        test.createGathering(category = Category.FOOD_DRINK, score = 90)
                    )
                },
                filter = filter(categories = listOf(Category.ACTIVITY)),
                cursor = null,
                size = 10,
                expectedUuids = { emptyList() },
                expectedHasNext = false
            ),

            // 모든 필터 조합
            ScrollFilterCase(
                name = "All filters combined",
                gatheringSetup = { test ->
                    listOf(
                        // 모든 조건 충족
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 6, 14, 0), // Monday
                            place = "서울 강남구",
                            genderRatioEnabled = true,
                            minCapacity = 5,
                            maxCapacity = 15,
                            fee = 15000,
                            score = 100
                        ),
                        // category 불일치
                        test.createGathering(
                            category = Category.FOOD_DRINK,
                            startDateTime = LocalDateTime.of(2025, 1, 6, 14, 0),
                            place = "서울 강남구",
                            genderRatioEnabled = true,
                            minCapacity = 5,
                            maxCapacity = 15,
                            fee = 15000,
                            score = 95
                        ),
                        // dayOfWeek 불일치
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 7, 14, 0), // Tuesday
                            place = "서울 강남구",
                            genderRatioEnabled = true,
                            minCapacity = 5,
                            maxCapacity = 15,
                            fee = 15000,
                            score = 90
                        ),
                        // location 불일치
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 6, 14, 0),
                            place = "부산 해운대구",
                            genderRatioEnabled = true,
                            minCapacity = 5,
                            maxCapacity = 15,
                            fee = 15000,
                            score = 85
                        ),
                        // 또 다른 모든 조건 충족
                        test.createGathering(
                            category = Category.PARTY,
                            startDateTime = LocalDateTime.of(2025, 1, 13, 14, 0), // Monday
                            place = "서울 서초구",
                            genderRatioEnabled = true,
                            minCapacity = 6,
                            maxCapacity = 12,
                            fee = 12000,
                            score = 80
                        )
                    )
                },
                filter = GatheringFilter(
                    statuses = listOf(Status.OPEN, Status.IN_PROGRESS),
                    categories = listOf(Category.PARTY),
                    guestCount = null,
                    dayOfWeek = DayOfWeek.MONDAY,
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 1, 31),
                    location = "서울",
                    genderRatioEnabled = true,
                    minCapacity = 5,
                    maxCapacity = 15,
                    minFee = 10000,
                    maxFee = 20000
                ),
                cursor = null,
                size = 10,
                expectedUuids = { gatherings ->
                    gatherings.filter {
                        val date = it.startDateTime!!.toLocalDate()
                        it.category == Category.PARTY &&
                                it.dayOfWeek == DayOfWeek.MONDAY &&
                                date >= LocalDate.of(2025, 1, 1) &&
                                date <= LocalDate.of(2025, 1, 31) &&
                                it.place?.contains("서울") == true &&
                                it.genderRatioEnabled &&
                                it.minCapacity >= 5 &&
                                it.maxCapacity <= 15 &&
                                it.fee in 10000..20000
                    }.sortedByDescending { it.score }.map { it.uuid }
                },
                expectedHasNext = false
            )
        )
    }
}
