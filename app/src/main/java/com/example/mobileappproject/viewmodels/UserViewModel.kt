package com.example.mobileappproject.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mobileappproject.repository.UserRepository
import com.example.mobileappproject.states.RecipeAlarmState
import com.example.mobileappproject.states.RecipeState
import com.example.mobileappproject.states.SharedState
import com.example.mobileappproject.states.UserState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*
class UserViewModel: ViewModel() {
    // 현재 로그인한 유저를 추적
    private val _userStateFlow = MutableStateFlow(UserState())
    val userStateFlow = _userStateFlow.asStateFlow()

    // 회원가입한 유저들을 저장해둔 임시 저장소
    private val _userMapStateFlow = MutableStateFlow<Map<String, UserState>>(emptyMap())
    val userMapStateFlow = _userMapStateFlow.asStateFlow()

    init {
        initializeTestUsers()
    }

    // 테스트용 유저를 미리 회원가입시킨다.
    private fun initializeTestUsers() {
        val initialUsers = mapOf(
            "test" to UserState(nickname = "test", password = "test", shoppingToDoMap = mutableMapOf()),
            "test2" to UserState(nickname = "test2", password = "test2", shoppingToDoMap = mutableMapOf())
        )
        _userMapStateFlow.value = initialUsers
    }

    // 회원가입
    fun registerUser(nickname: String, password: String): Boolean {
        if (!_userMapStateFlow.value.containsKey(nickname)) {
            val newUser = UserState(nickname = nickname, password = password, shoppingToDoMap = mutableMapOf())
            _userMapStateFlow.value += (nickname to newUser)
            return true
        }
        return false
    }

    // 로그인
    fun loginUser(nickname: String, password: String): Boolean {
        val user = _userMapStateFlow.value[nickname]
        if (user != null && user.password == password) {
            _userStateFlow.update { user.copy(password = password) }
            return true
        }
        return false
    }

    // 로그아웃
    fun logoutUser() {
        _userStateFlow.update { UserState() }
    }

    // 레시피 추가
    fun addRecipe(recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedList = currentState.recipeList
        // 이미 추가된 레시피인지 체크 (이름이 같은 레시피가 있는지)
        if (updatedList.any { it.name == recipe.name }) {
            return false
        }

        updatedList.add(recipe)
        _userStateFlow.update { currentState.copy(recipeList = updatedList) }
        return true
    }

    // 지정한 날짜에 쇼핑 리스트 추가 (레시피 목록에 있는 레시피만 추가 가능)
    fun addItemToShoppingList(date: LocalDate, recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

        // recipeList에 있는 레시피만 추가할 수 있도록 체크
        if (!currentState.recipeList.any { it.name == recipe.name }) {
            // 레시피 목록에 없는 레시피라면 추가할 수 없음
            return false
        }

        val currentList = updatedMap[date] ?: mutableListOf()

        // 이미 추가된 레시피인지 체크 (이름이 같은 레시피가 있는지)
        if (currentList.any { it.name == recipe.name }) {
            return false
        }

        // 레시피가 없다면 추가
        currentList.add(recipe)
        updatedMap[date] = currentList // 수정된 리스트 업데이트
        _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

        return true
    }

    // 쇼핑 리스트에서 레시피 제거
    fun removeItemFromShoppingList(date: LocalDate, recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

        val currentList = updatedMap[date]
        if (currentList != null && currentList.any { it.name == recipe.name }) {
            val updatedList = currentList.filter { it.name != recipe.name }
            updatedMap[date] = updatedList.toMutableList()

            _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

            return true
        }
        return false
    }

    fun setRecipeState(recipe: RecipeState, selectedDate: LocalDate) {
        SharedState.recipeAlarmState.value = RecipeAlarmState(
            recipeName = recipe.name,
            localDate = selectedDate,
            nickname = _userStateFlow.value.nickname
        )
    }
}

 */

class UserViewModel : ViewModel() {

    private val _userStateFlow = MutableStateFlow(UserState())
    val userStateFlow = _userStateFlow.asStateFlow()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository(FirebaseDatabase.getInstance()) // UserRepository 초기화

    // 로그인 성공 시 UserState 업데이트
    init {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            // Firebase UID를 nickname으로 사용하여 초기화
            _userStateFlow.value = UserState(
                nickname = it.uid // Firebase UID를 nickname으로 설정
            )
            loadUserData(it.uid) // Firebase에서 사용자 데이터 로드
        }
    }

    // Firebase에서 사용자 데이터를 로드하는 함수
    private fun loadUserData(userId: String, onSuccess: (UserState) -> Unit = {}, onFailure: (String) -> Unit = {}) {
        userRepository.loadUserData(userId, onSuccess = { userState ->
            // 사용자 데이터가 있으면 상태 업데이트
            _userStateFlow.value = userState
            onSuccess(userState)
        }, onFailure = { error ->
            onFailure(error)
        })
    }

    // Firebase에 UserState 데이터를 저장
    private fun saveUserData(userState: UserState) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        userRepository.saveUserData(userState, onSuccess = {
            Log.d("Firebase", "User data saved successfully")
        }, onFailure = { error ->
            Log.e("Firebase", "Failed to save user data: $error")
        })
    }

    // 로그인 기능
    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        // 로그인한 사용자 UID로 데이터 로드
                        loadUserData(it.uid, onSuccess = {
                            _userStateFlow.value = it // UserState 업데이트
                            onSuccess()
                        },
                            onFailure = { error ->
                                onError("사용자 데이터를 로드하는 데 실패했습니다: $error")
                            })
                    }
                } else {
                    onError("로그인 실패: ${task.exception?.message}")
                }
            }
    }

    // 회원가입 기능
    fun registerUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        // Firebase UID를 nickname으로 사용하여 UserState 설정
                        _userStateFlow.value = UserState(
                            nickname = it.uid // UID를 nickname으로 설정
                        )
                        // Firebase에 저장
                        saveUserData(_userStateFlow.value)
                        onSuccess()
                    }
                } else {
                    onError("회원가입 실패: ${task.exception?.message}")
                }
            }
    }

    // 로그아웃
    fun logoutUser() {
        firebaseAuth.signOut()
        _userStateFlow.update { UserState() }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 레시피 추가
    fun addRecipe(recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedList = currentState.recipeList
        if (updatedList.any { it.name == recipe.name }) { return false }
        updatedList.add(recipe)
        _userStateFlow.update { currentState.copy(recipeList = updatedList) }

        saveUserData(currentState) // Firebase에 저장
        return true
    }

    fun setRecipeState(recipe: RecipeState, selectedDate: LocalDate) {
        SharedState.recipeAlarmState.value = RecipeAlarmState(
            recipeName = recipe.name,
            localDate = selectedDate,
            nickname = _userStateFlow.value.nickname
        )
    }

    // 쇼핑 리스트 추가
    fun addItemToShoppingList(date: LocalDate, recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

//        if (!currentState.recipeList.any { it.name == recipe.name }) { return false }

        val currentList = updatedMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)] ?: mutableListOf()
        if (currentList.any { it.name == recipe.name }) { return false }

        currentList.add(recipe)
        updatedMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)] = currentList
        _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

        saveUserData(currentState) // Firebase에 저장
        return true
    }

    // 쇼핑 리스트에서 레시피 제거
    fun removeItemFromShoppingList(date: LocalDate, recipe: RecipeState): Boolean {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

        val currentList = updatedMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)]
        if (currentList != null && currentList.any { it.name == recipe.name }) {
            val updatedList = currentList.filter { it.name != recipe.name }
            updatedMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)] = updatedList.toMutableList()

            _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

            saveUserData(currentState) // Firebase에 저장
            return true
        }
        return false
    }

    // 쇼핑 리스트 아이템 체크 상태 토글
    fun toggleCheckState(date: LocalDate, recipe: RecipeState, ingredientIndex: Int) {
        val currentState = _userStateFlow.value
        val updatedMap = currentState.shoppingToDoMap

        val currentList = updatedMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)] ?: mutableListOf()
        val updatedList = currentList.map {
            if (it.id == recipe.id) {
                val updatedCheckList = it.checkList.toMutableList().apply {
                    this[ingredientIndex] = !this[ingredientIndex]
                }
                it.copy(checkList = updatedCheckList)
            } else {
                it
            }
        }

        updatedMap[date.format(DateTimeFormatter.ISO_LOCAL_DATE)] = updatedList.toMutableList()
        _userStateFlow.update { currentState.copy(shoppingToDoMap = updatedMap) }

        saveUserData(currentState) // Firebase에 저장
    }
}
