package com.example.mobileappproject.states

import java.util.UUID

//data class RecipeState(
//    val id: String = UUID.randomUUID().toString(),
//    val userNickname: String = "",  // User ID to associate recipes with the logged-in user
//    val name: String = "",
//    val ingredients: List<String> = emptyList(),
//    val method: List<String> = emptyList(),
//    var category: List<String> = emptyList(),
//    val bookMarked: Boolean = false,
//)


data class RecipeState(
    val id: String = UUID.randomUUID().toString(),
    val userNickname: String = "",  // User ID to associate recipes with the logged-in user
    val name: String = "",
    val ingredients: List<String> = emptyList(),
    val method: List<String> = emptyList(),
    var category: List<String> = emptyList(),
    val bookMarked: Boolean = false,
    val checkList: List<Boolean> = List(ingredients.size) { false }
) {
    // 기본 생성자 추가 (파라미터가 없는 생성자)
    constructor() : this(
        id = UUID.randomUUID().toString(),
        userNickname = "",
        name = "",
        ingredients = emptyList(),
        method = emptyList(),
        category = emptyList(),
        bookMarked = false,
        checkList = emptyList()
    )

    fun calculateCheckList(): List<Boolean> {
        return List(ingredients.size) { false }
    }
}

