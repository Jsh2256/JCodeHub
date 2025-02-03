package org.jbnu.jdevops.jcodeportallogin.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expire}") private val expireTime: Long
) {

    fun createToken(email: String, url: String, role: String): String {
        val claims = Jwts.claims().setSubject(email)
        claims["url"] = url
        claims["role"] = role

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expireTime))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getEmailFromToken(token: String): String? {
        return try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
        } catch (e: Exception) {
            null
        }
    }

    fun getRoleFromToken(token: String): String? {
        return try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body["role"] as String?
        } catch (e: Exception) {
            null
        }
    }
}
