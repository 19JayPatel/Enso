package com.example.enso.admin.models

data class UserModel(
    var name: String = "",
    var email: String = "",
    var role: String = "",
    var status: String = "",
    var bookingsCount: Int = 0
)