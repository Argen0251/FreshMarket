package com.example.freshmarket.data.model

import java.util.Date

data class Review(
    val userId: String = "",
    val userName: String = "",  // тут можем хранить email
    val rating: Double = 0.0,
    val text: String = "",
    val timestamp: Date = Date(),
    val reviewId: String = ""  // добавим поле, чтобы потом можно было удалять документ
) {
    constructor() : this("", "", 0.0, "", Date(), "")
}
