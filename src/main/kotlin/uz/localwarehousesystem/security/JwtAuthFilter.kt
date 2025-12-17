package uz.localwarehousesystem.security


import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import uz.localwarehousesystem.EmployeeNonActiveException
import uz.localwarehousesystem.EmployeeNotFoundException
import uz.localwarehousesystem.EmployeeRepository
import uz.localwarehousesystem.Status


@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val employeeRepository: EmployeeRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response)
            return
        }

        if (SecurityContextHolder.getContext().authentication == null) {

            val employeeNumber = jwtService.extractEmployeeNumber(token)

            val employee = employeeRepository
                .findByEmployeeNumberAndDeletedFalse(employeeNumber)
                ?: throw EmployeeNotFoundException()

            if (employee.status != Status.ACTIVE) throw EmployeeNonActiveException()


            val authority = SimpleGrantedAuthority("ROLE_${employee.role}")

            val authentication = UsernamePasswordAuthenticationToken(
                employee,
                null,
                listOf(authority)
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}

