package zeroone.developers.courseservice

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


interface CourseService {
    fun getAll(page: Int, size: Int): Page<CourseResponse>
    fun getAll(): List<CourseResponse>
    fun getOne(id: Long): CourseResponse
    fun create(request: CourseCreateRequest)
    fun update(id: Long, request: CourseUpdateRequest)
    fun getCourseStats(): CourseStatsResponse
    fun delete(id: Long)
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

    override fun getCourseStats(): CourseStatsResponse {
        val totalCourses = courseRepository.count()
        val totalIncome = courseRepository.sumCoursePrice()
        return CourseStatsResponse(totalCourses, totalIncome)
    }

    override fun delete(id: Long) {
        courseRepository.trash(id) ?: throw CourseNotFoundException()
    }

}





