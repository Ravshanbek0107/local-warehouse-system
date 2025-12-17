package uz.localwarehousesystem

import jakarta.servlet.http.HttpServletResponse
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import uz.localwarehousesystem.security.JwtService
import uz.localwarehousesystem.security.SecurityUtils
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Date
import java.util.UUID

interface EmployeeService{
    fun create(request: EmployeeCreateRequest): EmployeeResponse
    fun update(id:Long,request: EmployeeUpdateRequest): EmployeeResponse
    fun delete(id: Long)
    fun getAll(): List<EmployeeResponse>
    fun getOne(id: Long): EmployeeResponse
}


@Service
class EmployeeServiceImpl(
    val securityUtils: SecurityUtils,
    val warehouseRepository: WarehouseRepository,
    private val employeeRepository: EmployeeRepository
) : EmployeeService {
    // tizimdagi current employee
    val currentEmployee = securityUtils.getCurrentEmployee()

    @Transactional
    override fun create(request: EmployeeCreateRequest): EmployeeResponse {

        // bu yerda role yani kim yaratayotganiga qarab tekshirib yangi employee role ni belgilaydi yani agar admin create qilayotgan bolsa employee roleda yaratadi
        val role = when (currentEmployee.role) {
            EmployeeRole.MANAGER -> EmployeeRole.ADMIN
            EmployeeRole.ADMIN -> EmployeeRole.EMPLOYEE
            else -> throw EmployeeAccessDeniedException()
        }

        val warehouse = warehouseRepository.findByIdAndDeletedFalse(request.warehouseId) ?: throw WarehouseNotFoundException()

        val employee = Employee(
            name = request.name,
            surname = request.surname,
            phoneNumber = request.phoneNumber,
            password = securityUtils.passwordEncoder(request.password),
            warehouse = warehouse,
            role = role,
            status = Status.ACTIVE
        )

        val saved = employeeRepository.save(employee)

        return EmployeeResponse.toResponse(saved)

    }
    @Transactional
    override fun update(id: Long , request: EmployeeUpdateRequest): EmployeeResponse {

        val targetEmployee = employeeRepository.findByIdAndDeletedFalse(id) ?: throw EmployeeNotFoundException()

        // bu yerda ruxsat tekshiruvi yani current employee faqat ozi yaratgan employee larni update qiloladi yani manager faqat adminlarni qiladi
        val hasPermission = when (currentEmployee.role) {
            EmployeeRole.MANAGER ->
                targetEmployee.role == EmployeeRole.ADMIN

            EmployeeRole.ADMIN ->
                targetEmployee.role == EmployeeRole.EMPLOYEE

            else -> false
        }

        if (!hasPermission) throw EmployeeAccessDeniedException()


        request.name?.let { targetEmployee.name = it }
        request.surname?.let { targetEmployee.surname = it }
        request.phoneNumber?.let { targetEmployee.phoneNumber = it }

        request.password?.let {
            targetEmployee.password = securityUtils.passwordEncoder(it)
        }

        request.warehouseId?.let {
            val warehouse = warehouseRepository.findByIdAndDeletedFalse(it) ?: throw WarehouseNotFoundException()
            targetEmployee.warehouse = warehouse
        }
        request.status?.let {
            targetEmployee.status = it
        }

        val saved = employeeRepository.save(targetEmployee)

        return EmployeeResponse.toResponse(saved)
    }
    @Transactional
    override fun delete(id: Long) {
        val targetEmployee = employeeRepository.findByIdAndDeletedFalse(id) ?: throw EmployeeNotFoundException()

        // bu yerda ham update dagi tekshiruv
        val hasPermission = when (currentEmployee.role) {
            EmployeeRole.MANAGER -> targetEmployee.role == EmployeeRole.ADMIN
            EmployeeRole.ADMIN -> targetEmployee.role == EmployeeRole.EMPLOYEE
            else -> false
        }

        if (!hasPermission) {
            throw EmployeeAccessDeniedException()
        }

        employeeRepository.trash(targetEmployee.id!!)
    }

    override fun getAll(): List<EmployeeResponse> {
        //bu yerda ham role lar boyicha taqsimlangan manager hamma roledagini admin esa faqat employee
        val employees = when (currentEmployee.role) {
            EmployeeRole.MANAGER -> employeeRepository.findAllNotDeleted()
                .filter { it.role == EmployeeRole.ADMIN || it.role == EmployeeRole.EMPLOYEE }

            EmployeeRole.ADMIN -> employeeRepository.findAllNotDeleted()
                .filter { it.role == EmployeeRole.EMPLOYEE }

            else -> throw EmployeeAccessDeniedException()
        }

        return employees.map { EmployeeResponse.toResponse(it) }
    }

    override fun getOne(id: Long): EmployeeResponse {

        val targetEmployee = employeeRepository.findByIdAndDeletedFalse(id) ?: throw EmployeeNotFoundException()

        val employee = when (currentEmployee.role) {
            EmployeeRole.MANAGER -> employeeRepository.findByIdAndDeletedFalse(id)
                .let{ it?.role == EmployeeRole.ADMIN || it?.role == EmployeeRole.EMPLOYEE }

            EmployeeRole.ADMIN -> employeeRepository.findByIdAndDeletedFalse(id)
                .let{ it?.role == EmployeeRole.EMPLOYEE }

            else -> false
        }

        if (!employee) throw EmployeeAccessDeniedException()

        return EmployeeResponse.toResponse(targetEmployee)
    }

}

interface WarehouseService {
    fun create(request: WarehouseCreateRequest): WarehouseResponse
    fun update(id: Long, request: WarehouseUpdateRequest): WarehouseResponse
    fun delete(id: Long)
    fun getAll(): List<WarehouseResponse>
    fun getOne(id: Long): WarehouseResponse
}


@Service
class WarehouseServiceImpl(
    private val warehouseRepository: WarehouseRepository,
    private val securityUtils: SecurityUtils
) : WarehouseService {

    val currentEmployee = securityUtils.getCurrentEmployee()

    //faqat admin qiloladi shuni uchun hammasida tekshirmay bitta method yozib qoydim
    private fun checkAdminPermission() {
        if (currentEmployee.role != EmployeeRole.ADMIN) {
            throw EmployeeAccessDeniedException()
        }
    }
    @Transactional
    override fun create(request: WarehouseCreateRequest): WarehouseResponse {
        checkAdminPermission()

        val warehouse = Warehouse(
            name = request.name,
        )
        return WarehouseResponse.toResponse(warehouseRepository.save(warehouse))
    }
    @Transactional
    override fun update(id: Long, request: WarehouseUpdateRequest): WarehouseResponse {
        checkAdminPermission()
        val warehouse = warehouseRepository.findByIdAndDeletedFalse(id) ?: throw WarehouseNotFoundException()
        request.name?.let { warehouse.name = it }
        request.status?.let { warehouse.status = it }

        val saved = warehouseRepository.save(warehouse)
        return WarehouseResponse.toResponse(saved)
    }
    @Transactional
    override fun delete(id: Long) {
        checkAdminPermission()

        warehouseRepository.trash(id) ?: throw WarehouseNotFoundException()
    }

    override fun getAll(): List<WarehouseResponse> {
        checkAdminPermission()
        return warehouseRepository.findAllNotDeleted().map { WarehouseResponse.toResponse(it) }
    }

    override fun getOne(id: Long): WarehouseResponse {
        checkAdminPermission()
        val warehouse = warehouseRepository.findByIdAndDeletedFalse(id) ?: throw WarehouseNotFoundException()
        return WarehouseResponse.toResponse(warehouse)
    }

}


interface CategoryService {
    fun create(request: CategoryCreateRequest): CategoryResponse
    fun update(id: Long, request: CategoryUpdateRequest): CategoryResponse
    fun delete(id: Long)
    fun getAll(): List<CategoryResponse>
    fun getOne(id: Long): CategoryResponse
}


@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val securityUtils: SecurityUtils
) : CategoryService {

    private val currentEmployee get() = securityUtils.getCurrentEmployee()

    private fun checkAdminPermission() {
        if (currentEmployee.role != EmployeeRole.ADMIN) {
            throw EmployeeAccessDeniedException()
        }
    }

    @Transactional
    override fun create(request: CategoryCreateRequest): CategoryResponse {

        checkAdminPermission()

        val parentCategory = request.parentCategoryId?.let {
            categoryRepository.findByIdAndDeletedFalse(it) ?: throw CategoryNotFoundException()
        }

        val category = Category(
            name = request.name,
            category = parentCategory
        )
        val saved = categoryRepository.save(category)
        return CategoryResponse.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: CategoryUpdateRequest): CategoryResponse {
        checkAdminPermission()

        val category = categoryRepository.findByIdAndDeletedFalse(id) ?: throw CategoryNotFoundException()
        category.let { if(it.status != Status.ACTIVE) throw CategoryNonActiveException() }

        request.name?.let { category.name = it }
        request.status?.let { category.status = it }
        request.parentCategoryId?.let {
            val parent = categoryRepository.findByIdAndDeletedFalse(it) ?: throw CategoryNotFoundException()
            category.category = parent
        }
        val saved = categoryRepository.save(category)
        return CategoryResponse.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long) {
        checkAdminPermission()

        val category = categoryRepository.findByIdAndDeletedFalse(id) ?: throw CategoryNotFoundException()

        //bu yerda polni tekshirilgan agar category ichida boglangan category yo product bolsa ochirilmaydi
        val hasChildCategories = categoryRepository.findAllNotDeleted()
            .any{ it.category?.id == category.id }

        if (hasChildCategories) throw CategoryHasChildException()

        val hasProducts = productRepository.findAllNotDeleted()
            .any { it.category?.id == category.id }

        if (hasProducts) throw CategoryHasProductsException()

        categoryRepository.trash(id)
    }

    override fun getAll(): List<CategoryResponse> {
        checkAdminPermission()
        return categoryRepository.findAllNotDeleted()
            .filter { it.status == Status.ACTIVE }
            .map { CategoryResponse.toResponse(it) }
    }

    override fun getOne(id: Long): CategoryResponse {
        checkAdminPermission()
        val category = categoryRepository.findByIdAndDeletedFalse(id) ?: throw CategoryNotFoundException()
        category.let { if (it.status != Status.ACTIVE) throw CategoryNonActiveException() }
        return CategoryResponse.toResponse(category)
    }
}


interface ProductService {
    fun create(request: ProductCreateRequest): ProductResponse
    fun update(id: Long, request: ProductUpdateRequest): ProductResponse
    fun delete(id: Long)
    fun getAll(): List<ProductResponse>
    fun getOne(id: Long): ProductResponse
}


@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val measureRepository: MeasureRepository,
    private val categoryRepository: CategoryRepository,
    private val securityUtils: SecurityUtils
) : ProductService {

    private val currentEmployee get() = securityUtils.getCurrentEmployee()

    private fun checkAdminPermission() {
        if (currentEmployee.role != EmployeeRole.ADMIN) {
            throw EmployeeAccessDeniedException()
        }
    }

    @Transactional
    override fun create(request: ProductCreateRequest): ProductResponse {
        checkAdminPermission()

        val measure = request.measureId?.let {
            measureRepository.findByIdAndDeletedFalse(it) ?: throw MeasureNotFoundException()
        }

        val category = request.categoryId?.let {
            categoryRepository.findByIdAndDeletedFalse(it) ?: throw CategoryNotFoundException()
        }

        val product = Product(
            name = request.name,
            measure = measure,
            category = category
        )
        val saved = productRepository.save(product)
        return ProductResponse.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: ProductUpdateRequest): ProductResponse {
        checkAdminPermission()

        val product = productRepository.findByIdAndDeletedFalse(id) ?: throw ProductNotFoundException()

        request.name?.let { product.name = it }
        request.measureId?.let {
            val measure = measureRepository.findByIdAndDeletedFalse(it) ?: throw MeasureNotFoundException()
            product.measure = measure
        }
        request.categoryId?.let {
            val category = categoryRepository.findByIdAndDeletedFalse(it) ?: throw CategoryNotFoundException()
            product.category = category
        }
        val saved = productRepository.save(product)
        return ProductResponse.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long) {
        checkAdminPermission()

        productRepository.trash(id) ?: throw ProductNotFoundException()
    }

    override fun getAll(): List<ProductResponse> {
        checkAdminPermission()
        return productRepository.findAllNotDeleted().map { ProductResponse.toResponse(it) }
    }

    override fun getOne(id: Long): ProductResponse {
        checkAdminPermission()
        val product = productRepository.findByIdAndDeletedFalse(id) ?: throw ProductNotFoundException()
        return ProductResponse.toResponse(product)
    }
}


interface MeasureService {
    fun create(request: MeasureCreateRequest): MeasureResponse
    fun update(id: Long, request: MeasureUpdateRequest): MeasureResponse
    fun delete(id: Long)
    fun getAll(): List<MeasureResponse>
    fun getOne(id: Long): MeasureResponse
}


@Service
class MeasureServiceImpl(
    private val measureRepository: MeasureRepository,
    private val securityUtils: SecurityUtils
) : MeasureService {

    private val currentEmployee get() = securityUtils.getCurrentEmployee()

    private fun checkAdminPermission() {
        if (currentEmployee.role != EmployeeRole.ADMIN) {
            throw EmployeeAccessDeniedException()
        }
    }

    @Transactional
    override fun create(request: MeasureCreateRequest): MeasureResponse {
        checkAdminPermission()
        val measure = Measure(
            name = request.name,
            status = Status.ACTIVE
        )
        val saved = measureRepository.save(measure)
        return MeasureResponse.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: MeasureUpdateRequest): MeasureResponse {
        checkAdminPermission()
        val measure = measureRepository.findByIdAndDeletedFalse(id) ?: throw MeasureNotFoundException()

        request.name?.let { measure.name = it }
        request.status?.let { measure.status = it }

        val saved = measureRepository.save(measure)
        return MeasureResponse.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long) {
        checkAdminPermission()

        measureRepository.trash(id)?: throw MeasureNotFoundException()
    }

    override fun getAll(): List<MeasureResponse> {
        //filterda status active boganlarini oladi faqat
        checkAdminPermission()
        return measureRepository.findAllNotDeleted().filter { it.status == Status.ACTIVE }.map { MeasureResponse.toResponse(it) }
    }

    override fun getOne(id: Long): MeasureResponse {
        checkAdminPermission()
        val measure = measureRepository.findByIdAndDeletedFalse(id) ?: throw MeasureNotFoundException()
        measure.let {if (it.status != Status.ACTIVE) throw MeasureNonActiveException() }
        return MeasureResponse.toResponse(measure)
    }
}



interface SupplierService {
    fun create(request: SupplierCreateRequest): SupplierResponse
    fun update(id: Long, request: SupplierUpdateRequest): SupplierResponse
    fun delete(id: Long)
    fun getAll(): List<SupplierResponse>
    fun getOne(id: Long): SupplierResponse
}


@Service
class SupplierServiceImpl(
    private val supplierRepository: SupplierRepository,
    private val securityUtils: SecurityUtils
) : SupplierService {

    private val currentEmployee get() = securityUtils.getCurrentEmployee()

    private fun checkAdminPermission() {
        if (currentEmployee.role != EmployeeRole.ADMIN) {
            throw EmployeeAccessDeniedException()
        }
    }

    @Transactional
    override fun create(request: SupplierCreateRequest): SupplierResponse {
        checkAdminPermission()
        val supplier = Supplier(
            name = request.name,
            phoneNumber = request.phoneNumber
        )
        return SupplierResponse.toResponse(supplierRepository.save(supplier))
    }

    @Transactional
    override fun update(id: Long, request: SupplierUpdateRequest): SupplierResponse {
        checkAdminPermission()
        val supplier = supplierRepository.findByIdAndDeletedFalse(id) ?: throw SupplierNotFoundException()

        request.name?.let { supplier.name = it }
        request.phoneNumber?.let { supplier.phoneNumber = it }

        return SupplierResponse.toResponse(supplierRepository.save(supplier))
    }

    @Transactional
    override fun delete(id: Long) {
        checkAdminPermission()

        supplierRepository.trash(id) ?: throw SupplierNotFoundException()
    }

    override fun getAll(): List<SupplierResponse> {
        checkAdminPermission()
        return supplierRepository.findAllNotDeleted().map { SupplierResponse.toResponse(it) }
    }

    override fun getOne(id: Long): SupplierResponse {
        checkAdminPermission()
        val supplier = supplierRepository.findByIdAndDeletedFalse(id) ?: throw SupplierNotFoundException()
        return SupplierResponse.toResponse(supplier)
    }
}



interface TransactionService {
    fun create(request: TransactionCreateRequest): TransactionResponse
    fun getOne(id: Long): TransactionResponse
    fun getAll(): List<TransactionResponse>
}

@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val transactionItemRepository: TransactionItemRepository,
    private val warehouseRepository: WarehouseRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val securityUtils: SecurityUtils
) : TransactionService {

    @Transactional
    override fun create(request: TransactionCreateRequest): TransactionResponse {

        val currentEmployee = securityUtils.getCurrentEmployee()

        when (request.transactionType) {
            TransactionType.STOCK_IN -> if (currentEmployee.role != EmployeeRole.ADMIN) throw TransactionAccessDeniedException()
            TransactionType.STOCK_OUT -> if (currentEmployee.role != EmployeeRole.EMPLOYEE) throw TransactionAccessDeniedException()
            else -> {}
        }

        val warehouse = warehouseRepository.findByIdAndDeletedFalse(request.warehouseId) ?: throw WarehouseNotFoundException()

        val supplier = request.supplierId?.let {
            supplierRepository.findByIdAndDeletedFalse(it) ?: throw SupplierNotFoundException()
        }

        val transaction = Transaction(
            warehouse = warehouse,
            supplier = supplier,
            transactionType = request.transactionType,
            employee = currentEmployee
        )

        val savedTransaction = transactionRepository.save(transaction)

        var totalAmount = BigDecimal.ZERO

        val transactionItems = request.items.map { itemReq ->
            val product = productRepository.findByIdAndDeletedFalse(itemReq.productId) ?: throw ProductNotFoundException()
            //BU YERDA AGAR KIRIM BOLSA REQUESTDAGI PRICELAR OLINADI AGAR CHIQIM BOLSA BAZILARI OLINMAYDI yani null
            val priceIn = if (request.transactionType == TransactionType.STOCK_IN) itemReq.priceIn else null
            val priceOut = if (request.transactionType == TransactionType.STOCK_OUT) itemReq.priceOut else null

            val transactionItem = TransactionItem(
                transaction = savedTransaction,
                product = product,
                quantity = itemReq.quantity,
                priceIn = priceIn,
                priceOut = priceOut
            )

            if (priceIn != null) totalAmount += priceIn.multiply(itemReq.quantity)
            if (priceOut != null) totalAmount += priceOut.multiply(itemReq.quantity)

            transactionItemRepository.save(transactionItem)
        }

        savedTransaction.totalAmount = totalAmount
        transactionRepository.save(savedTransaction)

        return TransactionResponse.toResponse(savedTransaction, transactionItems)
    }

    override fun getOne(id: Long): TransactionResponse {
        val transaction = transactionRepository.findByIdAndDeletedFalse(id) ?: throw TransactionNotFoundException()

        val items = transactionItemRepository.findAllByTransaction(transaction)

        val currentEmployee = securityUtils.getCurrentEmployee()

        if ((transaction.transactionType == TransactionType.STOCK_IN && currentEmployee.role != EmployeeRole.ADMIN) ||
            (transaction.transactionType == TransactionType.STOCK_OUT && currentEmployee.role != EmployeeRole.EMPLOYEE)
        ) throw TransactionAccessDeniedException()

        return TransactionResponse.toResponse(transaction, items)
    }

    override fun getAll(): List<TransactionResponse> {
        val currentEmployee = securityUtils.getCurrentEmployee()

        val transactions = when (currentEmployee.role) {
            EmployeeRole.ADMIN -> transactionRepository.findAllNotDeleted()
                .filter { it.transactionType == TransactionType.STOCK_IN }
            EmployeeRole.EMPLOYEE -> transactionRepository.findAllNotDeleted()
                .filter { it.transactionType == TransactionType.STOCK_OUT }
            else -> throw TransactionAccessDeniedException()
        }

        return transactions.map { transaction ->
            val items = transactionItemRepository.findAllByTransaction(transaction)
            TransactionResponse.toResponse(transaction, items)
        }
    }
}


interface ProductImageService {
    fun upload(productId: Long, file: MultipartFile, isPrimary: Boolean = false): ProductImageResponse
    fun download(hashId: Long, response: HttpServletResponse)
    fun getAllByProduct(productId: Long): List<ProductImageResponse>
}


@Service
class ProductImageServiceImpl(
    private val productRepository: ProductRepository,
    private val fileAssetRepository: FileAssetRepository,
    private val productImageRepository: ProductImageRepository,
    private val securityUtils: SecurityUtils // current employee va role tekshirish uchun
) : ProductImageService {

    @Transactional
    override fun upload(productId: Long, file: MultipartFile, isPrimary: Boolean): ProductImageResponse {
        val currentEmployee = securityUtils.getCurrentEmployee()
        if (currentEmployee.role != EmployeeRole.ADMIN) throw EmployeeAccessDeniedException()

        val product = productRepository.findByIdAndDeletedFalse(productId) ?: throw ProductNotFoundException()

        val folder = Paths.get("uploads/products")
        if (!Files.exists(folder)) Files.createDirectories(folder)
        val filename = UUID.randomUUID().toString() + "_" + file.originalFilename
        val path = folder.resolve(filename)
        file.inputStream.use { Files.copy(it, path) }

        val fileAsset = FileAsset(
            fileName = file.originalFilename,
            contentType = file.contentType,
            size = file.size,
            path = path.toString()
        )
        val savedFileAsset = fileAssetRepository.save(fileAsset)

        val productImage = ProductImage(
            product = product,
            fileAsset = savedFileAsset,
            isPrimary = isPrimary
        )
        val savedImage = productImageRepository.save(productImage)

        return ProductImageResponse.toResponse(savedImage)
    }

    override fun download(hashId: Long, response: HttpServletResponse) {
        val fileAsset = fileAssetRepository.findByIdAndDeletedFalse(hashId) ?: throw FileNotFoundException()

        val file = Paths.get(fileAsset.path)
        if (!Files.exists(file)) throw FileNotFoundException()

        response.contentType = fileAsset.contentType
        response.setHeader("Content-Disposition", "inline; filename=\"${fileAsset.fileName}\"")
        Files.copy(file, response.outputStream)
        response.flushBuffer()
    }



    override fun getAllByProduct(productId: Long): List<ProductImageResponse> {
        val product = productRepository.findByIdAndDeletedFalse(productId) ?: throw ProductNotFoundException()

        return productImageRepository.findAllNotDeleted()
            .filter { it.product?.id == product.id }
            .map { ProductImageResponse.toResponse(it) }
    }
}

interface StatisticsService {
    fun getDailyStockIn(date: Date): List<DailyInStat>
    fun getDailyTopStockOut(date: Date): List<DailyOutStat>
    fun getExpiredProducts(date: Date): List<ExpiredProductStat>
}


@Service
class StatisticsServiceImpl(
    private val transactionItemRepository: TransactionItemRepository,
    private val securityUtils: SecurityUtils
) : StatisticsService {
    private val currentEmployee get() = securityUtils.getCurrentEmployee()

    private fun checkAdminPermission() {
        if (currentEmployee.role != EmployeeRole.ADMIN) {
            throw EmployeeAccessDeniedException()
        }
    }
    @Transactional(readOnly = true)
    override fun getDailyStockIn(date: Date): List<DailyInStat> {
        checkAdminPermission()
        return transactionItemRepository.findDailyInProducts(date)
    }

    @Transactional(readOnly = true)
    override fun getDailyTopStockOut(date: Date): List<DailyOutStat> {
        checkAdminPermission()
        return transactionItemRepository.findDailyTopOutProducts(date)
    }

    @Transactional(readOnly = true)
    override fun getExpiredProducts(date: Date): List<ExpiredProductStat> {
        checkAdminPermission()
        return transactionItemRepository.findExpiredProducts(date)
    }
}



interface NotificationSettingService {
    fun setNotification(settingRequest: NotificationSettingRequest)
}


@Service
class NotificationSettingServiceImpl(
    private val notificationSettingRepository: NotificationSettingRepository,
    private val securityUtils: SecurityUtils,
) : NotificationSettingService {


    @Transactional
    override fun setNotification(settingRequest: NotificationSettingRequest) {
        val currentEmployee = securityUtils.getCurrentEmployee()
        if (currentEmployee.role != EmployeeRole.ADMIN) throw EmployeeAccessDeniedException()

        val setting = notificationSettingRepository.findAllNotDeleted().firstOrNull() ?: NotificationSetting()

        notificationSettingRepository.save(setting)
    }

}

interface AuthService {
    fun login(request: LoginRequest): LoginResponse
}

@Service
class AuthServiceImpl(
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) : AuthService {

    override fun login(request: LoginRequest): LoginResponse {

        val employee = employeeRepository
            .findByEmployeeNumberAndDeletedFalse(request.employeeNumber) ?: throw EmployeeNotFoundException()

        if (!passwordEncoder.matches(request.password, employee.password)) {
            throw WrongPasswordException()
        }

        if (employee.status != Status.ACTIVE) {
            throw EmployeeAccessDeniedException()
        }

        val token = jwtService.generateToken(employee)

        return LoginResponse(token)
    }
}

















