package zeroone.developers.paymentservice

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface UserService {
    fun getAll(page: Int, size: Int): Page<UserResponse>
    fun getAll(): List<UserResponse>
    fun getOne(id: Long): UserResponse
    fun getUserEntity(id: Long): User
    fun create(request: UserCreateRequest)
    fun update(id: Long, request: UserUpdateRequest)
    fun delete(id: Long)
}

interface CourseService {
    fun getAll(page: Int, size: Int): Page<CourseResponse>
    fun getAll(): List<CourseResponse>
    fun getOne(id: Long): CourseResponse
    fun create(request: CourseCreateRequest)
    fun update(id: Long, request: CourseUpdateRequest)
    fun delete(id: Long)
}

interface PaymentService {
    fun getAll(page: Int, size: Int): Page<PaymentResponse>
    fun getAll(): List<PaymentResponse>
    fun getOne(id: Long): PaymentResponse
    fun create(request: PaymentCreateRequest): PaymentResponse
    fun update(id: Long, request: PaymentUpdateRequest): PaymentResponse
    fun delete(id: Long)

//    // Payment Statistics
//    fun getUserPaymentStatistics(userId: Long): UserPaymentStatisticsResponse
//    fun getCoursePaymentStatistics(courseId: Long): CoursePaymentStatisticsResponse
//    fun getAllPaymentsStatistics(): PaymentStatisticsResponse
}




@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) : UserService {

    override fun getAll(page: Int, size: Int): Page<UserResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val usersPage = userRepository.findAllNotDeletedForPageable(pageable)
        return usersPage.map { userMapper.toDto(it) }
    }

    override fun getAll(): List<UserResponse> {
        return userRepository.findAllNotDeleted().map {
            userMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): UserResponse {
        userRepository.findByIdAndDeletedFalse(id)?.let {
            return userMapper.toDto(it)
        } ?: throw UserNotFoundException()
    }

    //meta method
    override fun getUserEntity(id: Long): User {
        return userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException()
    }

    override fun create(request: UserCreateRequest) {
        val existingUser = userRepository.findByUsernameAndDeletedFalse(request.username)
        if (existingUser != null) throw UserAlreadyExistsException()
        userRepository.save(userMapper.toEntity(request))
    }

    override fun update(id: Long, request: UserUpdateRequest) {
        val user = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
        userRepository.findByUsername(id, request.username)?.let { throw UserAlreadyExistsException() }

        val updateUser = userMapper.updateEntity(user, request)
        userRepository.save(updateUser)
    }

    override fun delete(id: Long) {
        userRepository.trash(id) ?: throw UserNotFoundException()
    }
}


@Service
class CourseServiceImpl(
    private val courseRepository: CourseRepository,
    private val courseMapper: CourseMapper
) : CourseService {

    override fun getAll(page: Int, size: Int): Page<CourseResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val coursesPage = courseRepository.findAllNotDeletedForPageable(pageable)
        return coursesPage.map { courseMapper.toDto(it) }
    }

    override fun getAll(): List<CourseResponse> {
        return courseRepository.findAllNotDeleted().map {
            courseMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): CourseResponse {
        courseRepository.findByIdAndDeletedFalse(id)?.let {
            return courseMapper.toDto(it)
        } ?: throw CourseNotFoundException()
    }

    override fun create(request: CourseCreateRequest) {
        val course = courseRepository.findByNameAndDeletedFalse(request.name)
        if (course != null) throw CourseAlreadyExistsException()
        courseRepository.save(courseMapper.toEntity(request))
    }

    override fun update(id: Long, request: CourseUpdateRequest) {
        val course = courseRepository.findByIdAndDeletedFalse(id) ?: throw CourseNotFoundException()
        courseRepository.findByName(id, request.name)?.let { throw CourseAlreadyExistsException() }
        val updateCourse = courseMapper.updateEntity(course, request)
        courseRepository.save(updateCourse)
    }

    override fun delete(id: Long) {
        courseRepository.trash(id) ?: throw CourseNotFoundException()
    }

}


@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentMapper: PaymentMapper
) : PaymentService {


    override fun getAll(page: Int, size: Int): Page<PaymentResponse> {
        val pageable: Pageable = PageRequest.of(page, size)
        val paymentsPage = paymentRepository.findAllNotDeletedForPageable(pageable)
        return paymentsPage.map { paymentMapper.toDto(it) }
    }

    override fun getAll(): List<PaymentResponse> {
        return paymentRepository.findAllNotDeleted().map {
            paymentMapper.toDto(it)
        }
    }


    override fun getOne(id: Long): PaymentResponse {
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException() }
        return paymentMapper.toDto(payment)
    }

    override fun create(request: PaymentCreateRequest): PaymentResponse {
        val payment = paymentMapper.toEntity(request)
        val savedPayment = paymentRepository.save(payment)
        return paymentMapper.toDto(savedPayment)
    }


    override fun update(id: Long, request: PaymentUpdateRequest): PaymentResponse {
        val payment = paymentRepository.findById(id)
            .orElseThrow { PaymentNotFoundException() }
        val updatedPayment = paymentMapper.updateEntity(payment, request)
        paymentRepository.save(updatedPayment)
        return paymentMapper.toDto(updatedPayment)
    }

    override fun delete(id: Long) {
        paymentRepository.trash(id) ?: throw PaymentNotFoundException()
    }

//    override fun getUserPaymentStatistics(userId: Long): UserPaymentStatisticsResponse {
//        val totalRevenue = paymentRepository.findAllByCourseId(courseId).sumOf { it.amount }
//        val paymentCount = paymentRepository.countByCourseId(courseId)
//        return CoursePaymentStatisticsResponse(courseId, totalRevenue, paymentCount)
//    }
//
//    override fun getCoursePaymentStatistics(courseId: Long): CoursePaymentStatisticsResponse {
//        val totalRevenue = paymentRepository.findAllByCourseId(courseId).sumOf { it.amount }
//        val paymentCount = paymentRepository.countByCourseId(courseId)
//        return CoursePaymentStatisticsResponse(courseId, totalRevenue, paymentCount)
//    }
//
//    override fun getAllPaymentsStatistics(): PaymentStatisticsResponse {
//        val totalRevenue = paymentRepository.findAll().sumOf { it.amount }
//        val paymentCount = paymentRepository.count()
//        return PaymentStatisticsResponse(totalRevenue, paymentCount)
//    }


}


