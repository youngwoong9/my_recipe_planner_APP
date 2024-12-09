package com.example.mobileappproject.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileappproject.components.InputField
import com.example.mobileappproject.viewmodels.UserViewModel

// 로그인 화면
@Composable
fun LoginScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MyRecipePlanner", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        InputField(
            value = email,
            onValueChange = { email = it },
            label = "이메일"
        )
        Spacer(modifier = Modifier.height(8.dp))

        InputField(
            value = password,
            onValueChange = { password = it },
            label = "비밀번호",
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                userViewModel.loginUser(
                    email = email,
                    password = password,
                    onSuccess = {
                        navController.navigate("main")
                    },
                    onError = {
                        keyboardController?.hide()
                        errorMessage = it
                    }
                )
                /////////////////////////////////////////////////////////////////////
            },
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("로그인")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.height(20.dp)) {
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red)
            }
        }

        TextButton(onClick = {
            navController.navigate("register")
            errorMessage = ""
        }) {
            Text("회원가입하기")
        }
    }
}