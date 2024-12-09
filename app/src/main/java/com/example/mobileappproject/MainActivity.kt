package com.example.mobileappproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileappproject.login.LoginScreen
import com.example.mobileappproject.register.RegisterScreen
import com.example.mobileappproject.ui.AlarmPage
import com.example.mobileappproject.ui.AlarmSettingPage
import com.example.mobileappproject.ui.MainScreen
import com.example.mobileappproject.ui.TodoListPage
import com.example.mobileappproject.ui.theme.MobileAppProjectTheme
import com.example.mobileappproject.viewmodels.AlarmViewModel
import com.example.mobileappproject.viewmodels.UserViewModel
import com.example.mobileappproject.ui.RecipeManagementScreen
import com.google.firebase.FirebaseApp
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Firebase 초기화
        enableEdgeToEdge()
        setContent {
            MobileAppProjectTheme {
                val alarmViewModel = viewModel<AlarmViewModel>()

                val navController = rememberNavController()
                val context = LocalContext.current
                alarmViewModel.ensureExactAlarmPermission(context)

                val userViewModel: UserViewModel = viewModel()
                val loggedInUserState by userViewModel.userStateFlow.collectAsState()


                NavHost(navController = navController, startDestination = "login") {
                    // 로그인 화면
                    composable("login") {
                        LoginScreen(navController, userViewModel)
                    }
                    // 회원가입 화면
                    composable("register") {
                        RegisterScreen(navController, userViewModel)
                    }

                    // 메인(캘린더) 화면
                    composable("main") {
                        MainScreen(
                            navController = navController,
                            userViewModel = userViewModel,
                            goToAlarmPage = { navController.navigate(route = "AlarmPage") },
                            setRecipeState = { recipeState, selectedDate -> userViewModel.setRecipeState(recipeState, selectedDate)
                            }
                        )
                    }

                    // 할 일 목록 화면
                    composable("TodoListPage") {
                        TodoListPage(
                            goToAlarmPage = { navController.navigate(route = "AlarmPage") }
                        )
                    }

                    // 알람 화면
                    composable(route = "AlarmPage") {
                        AlarmPage(
                            alarmViewModel = alarmViewModel,
                            context = context,
                            alarmDataToState = { alarmId -> alarmViewModel.alarmDataToState(alarmId) },
                            removeAlarm = { context, alarmId -> alarmViewModel.removeAlarm(context, alarmId) },
                            alarmSwitch = { context, alarmId -> alarmViewModel.alarmSwitch(context, alarmId) },
                            returnToMain = { navController.navigateUp() },
                            goToAlarmSettingPage = { navController.navigate(route = "AlarmSettingPage") }
                        )
                    }

                    // 알람 설정 화면
                    composable(route = "AlarmSettingPage") {
                        AlarmSettingPage(
                            context = context,
                            alarmViewModel = alarmViewModel,
                            returnToAlarmPage = { navController.navigateUp() },
                            saveAlarm = { context -> alarmViewModel.saveAlarm(context) },
                            updateAlarm = { context -> alarmViewModel.updateAlarm(context) },
                            resetState = { alarmViewModel.resetState() }
                        )
                    }

                    // 레시피 관리 화면
                    composable("recipe") {
                        RecipeManagementScreen(
                            userNickname = loggedInUserState.nickname,
                            onBack = { navController.navigateUp() }, // 뒤로가기 처리
                            ///////////////////////////////////////////////////////
                            userViewModel = userViewModel,
                            selectedDate = null
                        )
                    }

                    // 레시피 관리 화면 (선택한 날짜 포함)
                    composable("recipe/{selectedDate}") { backStackEntry ->
                        val selectedDate = backStackEntry.arguments?.getString("selectedDate")
                            ?.let { LocalDate.parse(it) }
                        RecipeManagementScreen(
                            userNickname = loggedInUserState.nickname,
                            onBack = { navController.navigateUp() }, // 동일 화면 호출
                            ///////////////////////////////////////////////////////
                            userViewModel = userViewModel,
                            selectedDate = selectedDate
                        )
                    }
                }
            }
        }
    }
}
