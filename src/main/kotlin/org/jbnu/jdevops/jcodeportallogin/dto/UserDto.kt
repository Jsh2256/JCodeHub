package org.jbnu.jdevops.jcodeportallogin.dto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.SchoolType
import org.jbnu.jdevops.jcodeportallogin.entity.User

data class UserDto(
    val email: String,
    val name: String?,
    val role: RoleType = RoleType.STUDENT,
    val school: SchoolType,
    val student_num: Int?  // nullable
)

fun User.toDto() = UserDto(
    email = email,
    name = name,
    role = role,
    school = school,
    student_num = studentNum
)
