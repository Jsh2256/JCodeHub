package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.JCodeDto
import org.jbnu.jdevops.jcodeportallogin.entity.Jcode
import org.jbnu.jdevops.jcodeportallogin.repo.JCodeRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserCoursesRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class JCodeService(
    private val jCodeRepository: JCodeRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val userCoursesRepository: UserCoursesRepository,
    private val redisService: RedisService
) {
    // JCode 생성 (관리자 전용)
    fun createJCode(courseId: Long, jcodeUrl: String, userId: Long): JCodeDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val user = userRepository.findById(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val userCourse = userCoursesRepository.findByUserIdAndCourseId(user.id, course.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UserCourse not found")

        val storedJcode = jCodeRepository.findByUserIdAndCourseId(user.id, course.id)
        if (storedJcode != null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Jcode already exists")
        }

        val jCode = jCodeRepository.save(
            Jcode(
                jcodeUrl = jcodeUrl,
                course = course,
                user = user,
                userCourse = userCourse
            )
        )

        return JCodeDto(
            jcodeId = jCode.id,
            jcodeUrl = jCode.jcodeUrl,
            courseName = jCode.course.name
        )
    }

    // JCode 삭제 (관리자 전용)
    fun deleteJCode(userId: Long, courseId: Long) {
        val user = userRepository.findById(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val jCode = jCodeRepository.findByUserIdAndCourseId(user.id, course.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "JCode not found for the specified user and course")

        jCodeRepository.delete(jCode)

        // Redis에서도 해당 정보를 삭제
        redisService.deleteUserCourse(user.email, course.code, course.clss)
    }
}
