package uz.localwarehousesystem

import java.math.BigDecimal
import java.util.Date

data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        var OK = BaseMessage(0, "OK")
    }
}

data class EmployeeCreateRequest(
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val password: String,
    val warehouseId: Long,
)

data class EmployeeUpdateRequest(
    val name: String?,
    val surname: String?,
    val phoneNumber: String?,
    val password: String?,
    val warehouseId: Long?,
    val status: Status?
)

data class EmployeeResponse(
    val id: Long,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val warehouse: Warehouse,
    val role: EmployeeRole,
    val employeeNumber: Long,
    val status: Status
){
    companion object{
        fun toResponse(employee: Employee) = EmployeeResponse(
           id = employee.id!!,
           name = employee.name,
           surname = employee.surname,
           phoneNumber = employee.phoneNumber,
           warehouse = employee.warehouse,
           role = employee.role,
           employeeNumber = employee.employeeNumber,
           status = employee.status
        )
    }
}


data class WarehouseCreateRequest(
    val name: String,
)

data class WarehouseUpdateRequest(
    val name: String?,
    val status: Status?
)

data class WarehouseResponse(
    val id: Long,
    val name: String,
    val status: Status
) {
    companion object {
        fun toResponse(entity: Warehouse) = WarehouseResponse(
            id = entity.id!!,
            name = entity.name,
            status = entity.status
        )
    }
}

data class CategoryCreateRequest(
    val name: String,
    val parentCategoryId: Long?,
)

data class CategoryUpdateRequest(
    val name: String?,
    val parentCategoryId: Long?,
    val status: Status?
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val status: Status,
    val parentCategoryId: Long?
) {
    companion object {
        fun toResponse(entity: Category) = CategoryResponse(
            id = entity.id!!,
            name = entity.name,
            status = entity.status,
            parentCategoryId = entity.category?.id
        )
    }
}


data class ProductCreateRequest(
    val name: String,
    val measureId: Long?,
    val categoryId: Long?
)

data class ProductUpdateRequest(
    val name: String?,
    val measureId: Long?,
    val categoryId: Long?
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val productNumber: Long?,
    val measureId: Long?,
    val categoryId: Long?
) {
    companion object {
        fun toResponse(entity: Product) = ProductResponse(
            id = entity.id!!,
            name = entity.name,
            productNumber = entity.productNumber,
            measureId = entity.measure?.id,
            categoryId = entity.category?.id
        )
    }
}

data class MeasureCreateRequest(
    val name: String
)

data class MeasureUpdateRequest(
    val name: String?,
    val status: Status?
)

data class MeasureResponse(
    val id: Long,
    val name: String,
    val status: Status
) {
    companion object {
        fun toResponse(entity: Measure) = MeasureResponse(
            id = entity.id!!,
            name = entity.name,
            status = entity.status
        )
    }
}



data class SupplierCreateRequest(
    val name: String,
    val phoneNumber: String
)

data class SupplierUpdateRequest(
    val name: String?,
    val phoneNumber: String?
)

data class SupplierResponse(
    val id: Long,
    val name: String,
    val phoneNumber: String?
) {
    companion object {
        fun toResponse(entity: Supplier) = SupplierResponse(
            id = entity.id!!,
            name = entity.name,
            phoneNumber = entity.phoneNumber
        )
    }
}


data class TransactionItemRequest(
    val productId: Long,
    val quantity: BigDecimal,
    val priceIn: BigDecimal?,
    val priceOut: BigDecimal?,
    val experiDate: Date?
)


data class TransactionItemResponse(
    val id: Long,
    val productId: Long,
    val quantity: BigDecimal,
    val priceIn: BigDecimal?,
    val priceOut: BigDecimal?,
    val experiDate: Date?
) {
    companion object {
        fun toResponse(item: TransactionItem) = TransactionItemResponse(
            id = item.id!!,
            productId = item.product.id!!,
            quantity = item.quantity!!,
            priceIn = item.priceIn,
            priceOut = item.priceOut,
            experiDate = item.experiDate
        )
    }
}


data class TransactionCreateRequest(
    val date:Date,
    val warehouseId: Long,
    val supplierId: Long?,
    val transactionType: TransactionType,
    val items: List<TransactionItemRequest>
)

data class TransactionResponse(
    val id: Long,
    val date: Date,
    val warehouseId: Long,
    val supplierId: Long?,
    val transactionNumber: Long,
    val transactionType: TransactionType,
    val totalAmount: BigDecimal,
    val employeeId: Long,
    val items: List<TransactionItemResponse>
) {
    companion object {
        fun toResponse(transaction: Transaction, items: List<TransactionItem>) = TransactionResponse(
            id = transaction.id!!,
            date = transaction.date,
            warehouseId = transaction.warehouse.id!!,
            supplierId = transaction.supplier?.id,
            transactionNumber = transaction.transactionNumber,
            transactionType = transaction.transactionType,
            totalAmount = transaction.totalAmount,
            employeeId = transaction.employee.id!!,
            items = items.map { TransactionItemResponse.toResponse(it) }
        )
    }
}

data class ProductImageResponse(
    val id: Long,
    val productId: Long,
    val fileAssetId: Long,
    val fileName: String?,
    val contentType: String?,
    val size: Long,
    val isPrimary: Boolean
) {
    companion object {
        fun toResponse(image: ProductImage) = ProductImageResponse(
            id = image.id!!,
            productId = image.product.id!!,
            fileAssetId = image.fileAsset.hashId,
            fileName = image.fileAsset.fileName,
            contentType = image.fileAsset.contentType,
            size = image.fileAsset.size,
            isPrimary = image.isPrimary
        )
    }
}


data class DailyInStat(
    val productId: Long,
    val productName: String,
    val totalQuantity: BigDecimal,
    val totalAmount: BigDecimal
)

data class DailyOutStat(
    val productId: Long,
    val productName: String,
    val totalQuantity: BigDecimal
)

data class ExpiredProductStat(
    val productId: Long,
    val productName: String,
    val quantity: BigDecimal
)

data class DailyStatisticsResponse(
    val dailyInProducts: List<DailyInStat>,
    val dailyTopOutProducts: List<DailyOutStat>,
    val expiredProducts: List<ExpiredProductStat>
)


data class NotificationSettingRequest(
    val beforeDay: Long,
//    val chatId: String
)

data class LoginRequest(
    val employeeNumber: Long,
    val password: String
)

data class LoginResponse(
    val token: String
)







