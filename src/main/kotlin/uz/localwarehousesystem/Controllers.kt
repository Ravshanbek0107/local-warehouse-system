package uz.localwarehousesystem

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Date

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse =
        authService.login(request)
}

@RestController
@RequestMapping("api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    fun create(@RequestBody request: EmployeeCreateRequest): EmployeeResponse =
        employeeService.create(request)

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: EmployeeUpdateRequest
    ): EmployeeResponse =
        employeeService.update(id, request)

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseMessage {
        employeeService.delete(id)
        return BaseMessage.OK
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    fun getAll(): List<EmployeeResponse> =
        employeeService.getAll()

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): EmployeeResponse =
        employeeService.getOne(id)
}

@RestController
@RequestMapping("api/warehouses")
class WarehouseController(
    private val warehouseService: WarehouseService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@RequestBody request: WarehouseCreateRequest) =
        warehouseService.create(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: WarehouseUpdateRequest
    ) =
        warehouseService.update(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseMessage {
        warehouseService.delete(id)
        return BaseMessage.OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAll() = warehouseService.getAll()

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) =
        warehouseService.getOne(id)
}

@RestController
@RequestMapping("api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@RequestBody request: CategoryCreateRequest) =
        categoryService.create(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: CategoryUpdateRequest) = categoryService.update(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseMessage {
        categoryService.delete(id)
        return BaseMessage.OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAll() = categoryService.getAll()

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) =
        categoryService.getOne(id)
}


@RestController
@RequestMapping("api/products")
class ProductController(
    private val productService: ProductService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun create(@RequestBody request: ProductCreateRequest) =
        productService.create(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: ProductUpdateRequest
    ) = productService.update(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseMessage {
        productService.delete(id)
        return BaseMessage.OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAll() = productService.getAll()

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = productService.getOne(id)
}


@RestController
@RequestMapping("api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @PostMapping
    fun create(@RequestBody request: TransactionCreateRequest) =
        transactionService.create(request)

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) =
        transactionService.getOne(id)

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    @GetMapping
    fun getAll() = transactionService.getAll()
}

@RestController
@RequestMapping("api/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/daily-in")
    fun dailyIn(@RequestParam date: Date) =
        statisticsService.getDailyStockIn(date)

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/daily-out")
    fun dailyOut(@RequestParam date: Date) =
        statisticsService.getDailyTopStockOut(date)

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expired")
    fun expired(@RequestParam date: Date) =
        statisticsService.getExpiredProducts(date)
}

@RestController
@RequestMapping("/api/measures")
class MeasureController(private val measureService: MeasureService) {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAllMeasures(): List<MeasureResponse> {
        return measureService.getAll()
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getMeasureById(@PathVariable id: Long): MeasureResponse {
        return measureService.getOne(id)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createMeasure(@RequestBody request: MeasureCreateRequest): MeasureResponse {
        return measureService.create(request)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateMeasure(@PathVariable id: Long, @RequestBody request: MeasureUpdateRequest): MeasureResponse {
        return measureService.update(id, request)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteMeasure(@PathVariable id: Long): BaseMessage {
        measureService.delete(id)
        return BaseMessage.OK
    }
}

@RestController
@RequestMapping("/api/suppliers")
class SupplierController(private val supplierService: SupplierService) {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAllSuppliers(): List<SupplierResponse> {
        return supplierService.getAll()
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getSupplierById(@PathVariable id: Long): SupplierResponse {
        return supplierService.getOne(id)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createSupplier(@RequestBody request: SupplierCreateRequest): SupplierResponse {
        return supplierService.create(request)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateSupplier(@PathVariable id: Long, @RequestBody request: SupplierUpdateRequest): SupplierResponse {
        return supplierService.update(id, request)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteSupplier(@PathVariable id: Long): BaseMessage {
        supplierService.delete(id)
        return BaseMessage.OK
    }
}







