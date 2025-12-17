package uz.localwarehousesystem

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)


@Entity
@Table(name = "warehouses")
class Warehouse(
    @Column(unique = true, nullable = false) var name: String,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
):BaseEntity()


@Entity
@Table(name = "employees")
class Employee(
    var name: String,
    var surname: String,
    var phoneNumber: String,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
    @Column(unique = true) var employeeNumber: Long,
    var password: String,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id") var warehouse: Warehouse,
    @Enumerated(EnumType.STRING) var role: EmployeeRole
):BaseEntity()


@Entity
@Table(name = "categories")
class Category(
    @Column(nullable = false, unique = true) var name: String,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false) var category: Category
):BaseEntity()

@Entity
@Table(name = "measures")
class Measure(
    @Column(nullable = false, unique = true) var name: String,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
):BaseEntity()


@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false) var name : String,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false) var category: Category,
    @Column(unique = true, nullable = false) var productNumber: Long,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "measure_id") var measure: Measure,
):BaseEntity()


@Entity
@Table(name = "suppliers")
class Supplier(
    @Column(nullable = false)var name: String,
    @Column(nullable = false)var phoneNumber: String,
):BaseEntity()


@Entity
@Table(name = "transactions")
class Transaction(
    @Temporal(TemporalType.DATE) var date: Date,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id", nullable = false) var warehouse: Warehouse,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "supplier_id") var supplier: Supplier? = null,

    @Column(columnDefinition = "NUMERIC(10,2)") var totalAmount: BigDecimal = BigDecimal.ZERO,
    @Column(unique = true, nullable = false) var transactionNumber: Long,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var transactionType: TransactionType,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_transaction_id") var parentTransaction: Transaction? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id") var employee: Employee,
):BaseEntity()


@Entity
@Table(name = "transaction_items")
class TransactionItem(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "transaction_id") var transaction: Transaction,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") var product: Product,
    @Column(columnDefinition = "NUMERIC(10,1)") var quantity: BigDecimal? = BigDecimal.ZERO,
    @Column(columnDefinition = "NUMERIC(10,2)") var priceIn: BigDecimal? = null,
    @Column(columnDefinition = "NUMERIC(10,2)") var priceOut: BigDecimal? = null,
    @Temporal(TemporalType.DATE) var experiDate: Date? = null,
):BaseEntity()

@Entity
@Table(name = "notification_setting")
class NotificationSetting(
    @ColumnDefault("3") var beforeDay: LocalDate,
//    var chatId: Long? = null,
):BaseEntity()


@Entity
@Table(name = "file_assets")
class FileAsset(
    @Column(unique = true) var hashId: Long,
    var fileName: String,
    var contentType: String,
    var size: Long,
    var path: String
): BaseEntity()

@Entity
@Table(name = "product_images")
class ProductImage(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") var product: Product,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "file_asset_id") var fileAsset: FileAsset,

    @Column(nullable = false) var isPrimary: Boolean = false

): BaseEntity()






























