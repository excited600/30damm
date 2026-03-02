package beyondeyesight.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.secret))
    }

    fun generateAccessToken(userUuid: UUID): String {
        return generateToken(userUuid, jwtProperties.accessTokenExpiry)
    }

    fun generateRefreshToken(userUuid: UUID): String {
        return generateToken(userUuid, jwtProperties.refreshTokenExpiry)
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserUuid(token: String): UUID {
        val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return UUID.fromString(claims.subject)
    }

    private fun generateToken(userUuid: UUID, expiryMs: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(userUuid.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expiryMs))
            .signWith(key)
            .compact()
    }
}
