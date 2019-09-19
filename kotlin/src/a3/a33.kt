package a3

import java.lang.IllegalArgumentException

class User(val id: Int, val name: String, val address: String)

fun saveUser(user: User) {

    if (user.name.isEmpty()) {
        throw IllegalArgumentException("${user.name}: ..")
    }

    if (user.address.isEmpty()) {
        throw IllegalArgumentException("${user.address}: ...")
    }

    // user database save....
}

fun saveUser1(user: User) {
    fun validate(user: User, value: String, fieldName: String) {
        if (value.isEmpty()) {
            throw IllegalArgumentException("${user.id} : empty $fieldName")
        }
    }

    validate(user, user.name, "Name")
    validate(user, user.address, "Address")
}

fun saveUser2(user: User) {
    fun validate(value: String, fieldName: String) {
        if (value.isEmpty()) {
            throw IllegalArgumentException("${user.id} : empty $fieldName")
        }
    }

    validate(user.name, "name")
    validate(user.address, "Address")
}

fun User.validateBeforeSave() {

    fun validate(value: String, fieldName: String) {
        if (value.isEmpty()) {
            throw IllegalArgumentException("$id: empty $fieldName")
        }
    }

    validate(name, "Name")
    validate(address, "Address")
}

fun saveUser4(user: User) {
    user.validateBeforeSave()
}