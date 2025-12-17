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
    @Column(unique = true) var name: String? = null,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
):BaseEntity()


@Entity
@Table(name = "employees")
class Employee(
    var name: String? =null,
    var surname: String? =null,
    var phoneNumber: String? =null,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
    @Id
    @SequenceGenerator(name = "employee_seq", sequenceName = "employee_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
    @Column(unique = true) var employeeNumber: Long? = null,
    var password: String? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id") var warehouse: Warehouse? = null,
    @Enumerated(EnumType.STRING) var role: EmployeeRole? = null
):BaseEntity()


@Entity
@Table(name = "categories")
class Category(
    var name: String? = null,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") var category: Category? = null,
):BaseEntity()

@Entity
@Table(name = "measures")
class Measure(
    var name: String? = null,
    @Column(nullable = false) @Enumerated(EnumType.STRING) var status: Status = Status.ACTIVE,
):BaseEntity()


@Entity
@Table(name = "products")
class Product(
    var name : String? =null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") var category: Category? = null,
    @Id
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @Column(unique = true) var productNumber: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "measure_id") var measure: Measure? = null,
):BaseEntity()


@Entity
@Table(name = "suppliers")
class Supplier(
    var name: String? = null,
    var phoneNumber: String? = null,
):BaseEntity()


@Entity
@Table(name = "transactions")
class Transaction(
    @Temporal(TemporalType.DATE) var date: Date? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id") var warehouse: Warehouse? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "supplier_id") var supplier: Supplier? = null,

    @Column(columnDefinition = "NUMERIC(10,2)") var totalAmount: BigDecimal? = BigDecimal.ZERO,
    @Id
    @SequenceGenerator(name = "stock_in_out_seq", sequenceName = "stock_in_out_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_in_out_seq")
    @Column(unique = true) var transactionNumber: Long? = null,
    @Enumerated(EnumType.STRING) var transactionType: TransactionType? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_transaction_id") var parentTransaction: Transaction? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id") var employee: Employee? = null,
):BaseEntity()


@Entity
@Table(name = "transaction_items")
class TransactionItem(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "transaction_id") var transaction: Transaction? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") var product: Product? = null,
    @Column(columnDefinition = "NUMERIC(10,1)") var quantity: BigDecimal? = BigDecimal.ZERO,
    @Column(columnDefinition = "NUMERIC(10,2)") var priceIn: BigDecimal? = null,
    @Column(columnDefinition = "NUMERIC(10,2)") var priceOut: BigDecimal? = null,
    @Temporal(TemporalType.DATE) var experiDate: Date? = null,
):BaseEntity()

@Entity
@Table(name = "notification_setting")
class NotificationSetting(
    @ColumnDefault("3") var beforeDay: LocalDate? = null,
//    var chatId: Long? = null,
):BaseEntity()


@Entity
@Table(name = "file_assets")
class FileAsset(

    @Id @SequenceGenerator(name = "image_hash_id_seq", sequenceName = "image_hash_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_hash_id_seq")
    @Column(unique = true) var hashId: Long? = null,
    var fileName: String? = null,
    var contentType: String? = null,
    var size: Long? = null,
    var path: String? = null
): BaseEntity()

@Entity
@Table(name = "product_images")
class ProductImage(
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") var product: Product? = null,

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "file_asset_id") var fileAsset: FileAsset? = null,

    @Column(nullable = false) var isPrimary: Boolean = false

): BaseEntity()






























