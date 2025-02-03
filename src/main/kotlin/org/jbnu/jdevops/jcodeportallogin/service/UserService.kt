package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.UserDto
import org.jbnu.jdevops.jcodeportallogin.dto.toDto
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): UserDto? {
        return userRepository.findByEmail(email)?.toDto()
    }

    @Transactional
    fun deleteUser(email: String) {
        val user = userRepository.findByEmail(email) 
            ?: throw IllegalArgumentException("User not found with email: $email")
        userRepository.delete(user)
    }
} 