package com.example.feishuqa.app.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel 层：
 * - 获取用户输入
 * - 校验用户名密码
 * - 将结果通过 LiveData 通知 UI
 */
class LoginViewModel : ViewModel()
{

    private val loginModel = LoginModel()

    // LiveData：通知 UI 登录结果
    private val _loginMessage = MutableLiveData<String>()
    val loginMessage: LiveData<String> get() = _loginMessage

    /**
     * 登录逻辑：校验用户名密码（放在 ViewModel 中）
     */
    fun login(context: Context, username: String, password: String)
    {

        // 简单空判断（UI 层方便处理）
        if (username.isEmpty() || password.isEmpty()) {
            _loginMessage.value = "用户名或密码不能为空"
            return
        }

        // Model 读取 user.json
        val userList = loginModel.loadUsers(context)

        // 校验用户名密码（核心逻辑）
        val success = userList.any { user ->
            user.name == username && user.password == password
        }

        // 根据结果更新 UI
        _loginMessage.value = if (success) {
            "登录成功"
        } else {
            "用户名或密码错误"
        }
    }
}
