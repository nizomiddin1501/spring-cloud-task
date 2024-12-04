package zeroone.developers.paymentservice

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): List<T>
    fun findAllNotDeletedForPageable(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): List<T> = findAll(isNotDeletedSpecification, pageable).content
    override fun findAllNotDeletedForPageable(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)

    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }

    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}

@Repository
interface UserRepository : BaseRepository<User> {
    fun findByUsernameAndDeletedFalse(username: String): User?

    @Query("""
        select u from users u
        where u.id != :id
        and u.username = :username
        and u.deleted = false 
    """)
    fun findByUsername(id: Long, username: String): User?

}


@Repository
interface CourseRepository : BaseRepository<Course> {

    @Query(value = "select count(*) > 0 from category c where c.name = :name", nativeQuery = true)
    fun existsByName(@Param("name") name: String): Boolean

    fun findByName(id: Long, name: String): Course?

    fun findByNameAndDeletedFalse(name: String): Course?

}

@Repository
interface PaymentRepository : BaseRepository<Payment> {

    fun findByUserId(userId: Long?): List<Payment>

    fun findByUserIdAndCourseId(userId: Long, courseId: Long): Optional<Payment>

    fun countByCourseId(courseId: Long): Int

    @Query(value = "select sum(amount) from payments where status = 'SUCCESS'", nativeQuery = true)
    fun sumAmount(): BigDecimal

    @Query(value = "select count(*) from payments", nativeQuery = true)
    fun countAllPayments(): Long

    @Query(value = "select * from payments where user_id = :userId and status = 'SUCCESS'", nativeQuery = true)
    fun findPaymentsByUser(@Param("userId") userId: Long): List<Payment>

    @Modifying
    @Query(value = "update payments set status = :status where id = :paymentId", nativeQuery = true)
    fun updatePaymentStatus(@Param("paymentId") paymentId: Long, @Param("status") status: String): Int

}










