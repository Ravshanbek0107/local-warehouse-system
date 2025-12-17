package uz.localwarehousesystem.security


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uz.localwarehousesystem.Employee
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(employee: Employee): String =
        Jwts.builder()
            .setSubject(employee.employeeNumber!!.toString())
            .claim("role", employee.role!!.name)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    fun extractEmployeeNumber(token: String): Long =
        extractAllClaims(token).subject.toLong()

    fun extractRole(token: String): String =
        extractAllClaims(token)["role"].toString()

    fun isTokenValid(token: String): Boolean =
        extractAllClaims(token).expiration.after(Date())
}




