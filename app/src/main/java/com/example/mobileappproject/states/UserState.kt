package com.example.mobileappproject.states

import java.time.LocalDate

data class UserState(
    var nickname: String = "", // 파이어베이스 UID
    var recipeList: MutableList<RecipeState> = mutableListOf(), // 레시피 리스트
    var shoppingToDoMap: MutableMap<String, MutableList<RecipeState>> = mutableMapOf() // 쇼핑 리스트
)