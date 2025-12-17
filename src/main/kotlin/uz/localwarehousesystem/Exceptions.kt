package uz.localwarehousesystem


import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.Locale

@ControllerAdvice
class ExceptionHandler(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(Throwable::class)
    fun handleOtherExceptions(exception: Throwable): ResponseEntity<Any> {
        when (exception) {
            is ShopAppException-> {

                return ResponseEntity
                    .badRequest()
                    .body(exception.getErrorMessage(errorMessageSource))
            }

            else -> {
                exception.printStackTrace()
                return ResponseEntity
                    .badRequest().body(
                        BaseMessage(100,
                            "Iltimos support bilan bog'laning")
                    )
            }
        }
    }

}



sealed class ShopAppException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null
    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments(),
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}


class CurrentUserNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.CURRENT_USER_NOT_FOUND
}

class EmployeeAccessDeniedException() : ShopAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_ACCSESS_DENIED
}

class WarehouseNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.WAREHOUSE_NOT_FOUND
}

class EmployeeNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_NOT_FOUND
}

class CategoryNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_NOT_FOUND
}

class CategoryHasChildException() : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_HAS_SUBCATEGORIES
}

class CategoryHasProductsException() : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_HAS_PRODUCTS
}

class CategoryNonActiveException() : ShopAppException() {
    override fun errorType() = ErrorCode.CATEGORY_NON_ACTIVE
}

class MeasureNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.MEASURE_NOT_FOUND
}

class ProductNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.PRODUCT_NOT_FOUND
}

class MeasureNonActiveException() : ShopAppException() {
    override fun errorType() = ErrorCode.MEASURE_NON_ACTIVE
}

class SupplierNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.SUPPLIER_NOT_FOUND
}

class TransactionAccessDeniedException() : ShopAppException() {
    override fun errorType() = ErrorCode.TRANSACTION_ACCSESS_DENIED
}

class TransactionNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.TRANSACTION_NOT_FOUND
}

class FileNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.FILE_NOT_FOUND
}

class ProductImageNotFoundException() : ShopAppException() {
    override fun errorType() = ErrorCode.IMAGE_NOT_FOUND
}

class WrongPasswordException() : ShopAppException() {
    override fun errorType() = ErrorCode.WRONG_PASSWORD
}
class EmployeeNonActiveException() : ShopAppException() {
    override fun errorType() = ErrorCode.EMPLOYEE_NON_ACTIVE
}

