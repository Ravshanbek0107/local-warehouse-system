package uz.localwarehousesystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
@EnableJpaAuditing
@EnableScheduling
class LocalWarehouseSystemApplication

fun main(args: Array<String>) {
    runApplication<LocalWarehouseSystemApplication>(*args)
}
