package beyondeyesight.domain.service.auth

import beyondeyesight.config.JwtProperties
import beyondeyesight.config.JwtTokenProvider
import beyondeyesight.domain.exception.ClientException
import beyondeyesight.domain.model.auth.RefreshTokenEntity
import beyondeyesight.domain.repository.auth.RefreshTokenRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
) {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
    )

    fun issueTokens(userUuid: UUID): TokenPair {
        val accessToken = jwtTokenProvider.generateAccessToken(userUuid)
        val refreshToken = jwtTokenProvider.generateRefreshToken(userUuid)

        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshTokenExpiry / 1000)

        refreshTokenRepository.save(
            RefreshTokenEntity.create(
                userUuid = userUuid,
                token = refreshToken,
                expiresAt = expiresAt,
            )
        )

        return TokenPair(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun refresh(refreshToken: String): TokenPair {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw InvalidRefreshTokenException()
        }

        val entity = refreshTokenRepository.findByToken(refreshToken)
            ?: throw InvalidRefreshTokenException()

        if (entity.isExpired()) {
            refreshTokenRepository.deleteByToken(refreshToken)
            throw InvalidRefreshTokenException()
        }

        refreshTokenRepository.deleteByToken(refreshToken)

        return issueTokens(entity.userUuid)
    }

    fun revokeAllTokens(userUuid: UUID) {
        refreshTokenRepository.deleteAllByUserUuid(userUuid)
    }
}

class InvalidRefreshTokenException : ClientException(
    statusCode = 401,
    message = "유효하지 않거나 만료된 리프레시 토큰입니다."
)
