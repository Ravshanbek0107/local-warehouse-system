package uz.localwarehousesystem.security


import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uz.localwarehousesystem.CurrentUserNotFoundException
import uz.localwarehousesystem.Employee
@Component
class SecurityUtils {

    fun passwordEncoder(password: String): String {
        val encoder = BCryptPasswordEncoder()
        return encoder.encode(password)
    }

    fun getCurrentEmployee(): Employee {
        val authentication = SecurityContextHolder.getContext().authentication

        if (
            authentication != null &&
            authentication.isAuthenticated &&
            authentication.principal is Employee
        ) {
            return authentication.principal as Employee
        }

        throw CurrentUserNotFoundException()
    }
}
