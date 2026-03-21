package beyondeyesight.domain.service.block

import beyondeyesight.domain.exception.ResourceNotFoundException
import beyondeyesight.domain.exception.block.DuplicateBlockException
import beyondeyesight.domain.model.block.UserBlockedUserEntity
import beyondeyesight.domain.repository.block.UserBlockedUserRepository
import beyondeyesight.domain.repository.user.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BlockService(
    private val userBlockedUserRepository: UserBlockedUserRepository,
    private val userRepository: UserRepository,
) {
    fun blockUser(blockerUuid: UUID, blockedUuid: UUID): UserBlockedUserEntity {
        userRepository.findByUuid(blockedUuid)
            ?: throw ResourceNotFoundException.byUuid(
                resourceName = "User",
                resourceUuid = blockedUuid,
            )

        if (userBlockedUserRepository.existsByBlockerUuidAndBlockedUuid(blockerUuid, blockedUuid)) {
            throw DuplicateBlockException(blockedUuid.toString())
        }

        return userBlockedUserRepository.save(
            UserBlockedUserEntity.create(
                blockerUuid = blockerUuid,
                blockedUuid = blockedUuid,
            )
        )
    }
}
