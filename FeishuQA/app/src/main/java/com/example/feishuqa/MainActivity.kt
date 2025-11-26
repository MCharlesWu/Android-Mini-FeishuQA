package com.example.feishuqa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.feishuqa.app.login.LoginViewModel
import com.example.feishuqa.common.utils.JsonUtils
import com.example.feishuqa.common.utils.theme.FeishuQATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 测试简单的登录验证
        val viewModel = LoginViewModel()

        viewModel.loginMessage.observe(this) { msg ->
            android.util.Log.d("LoginTest", "登录结果 = $msg")
        }

        // 测试一下
        viewModel.login(this, "alice", "123456")
        setContent {
            FeishuQATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello, this is $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FeishuQATheme {
        Greeting("Android")
    }
}