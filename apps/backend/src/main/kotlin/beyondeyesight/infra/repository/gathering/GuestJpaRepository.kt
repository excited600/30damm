package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.GuestEntity
import beyondeyesight.domain.model.GuestId
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GuestJpaRepository : JpaRepository<GuestEntity, GuestId>, KotlinJdslJpqlExecutor {
    fun findAllByGatheringUuid(gatheringUuid: UUID): List<GuestEntity>
}
