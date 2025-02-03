package org.jbnu.jdevops.jcodeportallogin.controller

import org.jbnu.jdevops.jcodeportallogin.dto.RegisterUserDto
import org.jbnu.jdevops.jcodeportallogin.dto.UserDto
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.service.AuthService
import org.jbnu.jdevops.jcodeportallogin.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val authService: AuthService
) {
    
    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return userService.getAllUsers()
    }

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserDto> {
        return userService.getUserByEmail(email)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{email}")
    fun deleteUser(@PathVariable email: String): ResponseEntity<Unit> {
        return try {
            userService.deleteUser(email)
            ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // 학생 계정 추가
    @PostMapping("/student")
    fun registerStudent(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        val studentDto = registerUserDto.copy(role = RoleType.STUDENT)
        return authService.register(studentDto)
    }

    // 조교 계정 추가
    @PostMapping("/assistant")
    fun registerAssistant(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        val assistantDto = registerUserDto.copy(role = RoleType.ASSISTANCE)
        return authService.register(assistantDto)
    }

    // 교수 계정 추가
    @PostMapping("/professor")
    fun registerProfessor(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        val professorDto = registerUserDto.copy(role = RoleType.PROFESSOR)
        return authService.register(professorDto)
    }
} 