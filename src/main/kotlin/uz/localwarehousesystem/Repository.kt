package uz.localwarehousesystem

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Date


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
}


class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted  = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}


@Repository
interface EmployeeRepository : BaseRepository<Employee>{
    fun findByEmployeeNumberAndDeletedFalse(employeeNumber: Long): Employee?
}

@Repository
interface WarehouseRepository : BaseRepository<Warehouse>{}

@Repository
interface CategoryRepository : BaseRepository<Category>{}

@Repository
interface ProductRepository : BaseRepository<Product>{}

@Repository
interface TransactionRepository : BaseRepository<Transaction>{}

@Repository
interface TransactionItemRepository : BaseRepository<TransactionItem>{
    fun findAllByTransaction(transaction: Transaction): List<TransactionItem>


    @Query("""
        select ti.product.id as productId,
               ti.product.name as productName,
               sum(ti.quantity) as totalQuantity,
               sum(ti.quantity * ti.priceIn) as totalAmount
        from TransactionItem ti
        join ti.transaction t
        where t.transactionType = 'STOCK_IN'
          and t.date = :date
        group by ti.product.id, ti.product.name
    """)
    fun findDailyInProducts(@Param("date") date: Date): List<DailyInStat>


    @Query("""
    select ti.product.id as productId,
           ti.product.name as productName,
           sum(ti.quantity) as totalQuantity
    from TransactionItem ti
    join ti.transaction t
    where t.transactionType = 'STOCK_OUT'
      and t.date = :date
    group by ti.product.id, ti.product.name
    order by sum(ti.quantity) desc
    """)
    fun findDailyTopOutProducts(@Param("date") date: Date): List<DailyOutStat>


    @Query("""
    select ti.product.id as productId,
           ti.product.name as productName,
           sum(ti.quantity) as quantity
    from TransactionItem ti
    where ti.experiDate <= :date
    group by ti.product.id, ti.product.name
    """)
    fun findExpiredProducts(@Param("date") date: Date): List<ExpiredProductStat>

    @Query("""
        SELECT ti FROM TransactionItem ti 
        WHERE DATE(ti.experiDate) = DATE(:targetDate)
        AND ti.deleted = false
    """)
    fun findByExperiDate(@Param("targetDate") targetDate: Date): List<TransactionItem>

}

@Repository
interface FileAssetRepository : BaseRepository<FileAsset>{}

@Repository
interface ProductImageRepository : BaseRepository<ProductImage>{}

@Repository
interface MeasureRepository : BaseRepository<Measure>{}

@Repository
interface SupplierRepository : BaseRepository<Supplier>{}

@Repository
interface NotificationSettingRepository : BaseRepository<NotificationSetting>{}