package beyondeyesight.infra.repository.payment

import beyondeyesight.domain.model.payment.PaymentEntity
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PaymentJpaRepository: JpaRepository<PaymentEntity, UUID>, KotlinJdslJpqlExecutor