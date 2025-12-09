package beyondeyesight.infra.repository.gathering

import beyondeyesight.domain.model.gathering.GatheringEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GatheringJpaRepository: JpaRepository<GatheringEntity, UUID>, KotlinJdslJpqlExecutor {
}