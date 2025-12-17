package uz.localwarehousesystem


import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean

import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Component

import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.support.RequestContextUtils
import uz.localwarehousesystem.security.SecurityConfig
import uz.localwarehousesystem.security.SecurityUtils
import java.util.Locale

@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @Bean
    fun errorMessageSource() = ResourceBundleMessageSource().apply {
        setDefaultEncoding(Charsets.UTF_8.name())
        setBasename("error")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : AsyncHandlerInterceptor {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

                request.getHeader("hl")?.let {
                    RequestContextUtils.getLocaleResolver(request)
                        ?.setLocale(request, response, Locale(it))
                }
                return true
            }
        })
    }
}

@Component
class DataLoader(
    private val employeeRepository: EmployeeRepository,
    private val securityUtils: SecurityUtils
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val managerExists = employeeRepository.findAll().any { it.role == EmployeeRole.MANAGER }
        if (!managerExists) {
            val manager = Employee(
                name = "Ravi",
                surname = "Qahhorov",
                phoneNumber = "998991112233",
                password = securityUtils.passwordEncoder("123"),
                role = EmployeeRole.MANAGER,
                status = Status.ACTIVE
            )
            employeeRepository.save(manager)
            println("Manager created: ${manager.name}")
        }
    }
}