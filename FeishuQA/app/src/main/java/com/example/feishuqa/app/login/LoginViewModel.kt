package com.example.feishuqa.app.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.feishuqa.data.repository.UserRepository

/**
 * 登录界面ViewModel
 */
class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // 用户名
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    // 密码
    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    // 密码是否可见
    private val _passwordVisible = MutableLiveData(false)
    val passwordVisible: LiveData<Boolean> = _passwordVisible

    // 登录状态
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // 错误消息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * 设置用户名
     */
    fun setUsername(username: String) {
        _username.value = username
    }

    /**
     * 设置密码
     */
    fun setPassword(password: String) {
        _password.value = password
    }

    /**
     * 切换密码可见性
     */
    fun togglePasswordVisibility() {
        _passwordVisible.value = !(_passwordVisible.value ?: false)
    }

    /**
     * 执行登录
     */
    fun login() {
        val username = _username.value?.trim() ?: ""
        val password = _password.value?.trim() ?: ""

        // 验证输入
        if (username.isEmpty()) {
            _errorMessage.value = "请输入用户名"
            return
        }

        if (password.isEmpty()) {
            _errorMessage.value = "请输入密码"
            return
        }

        // 执行登录（模拟）
        performLogin(username, password)
    }

    private fun performLogin(username: String, password: String) {
        // TODO: 实际项目中调用后端API验证
        
        // 模拟登录成功
        userRepository.saveLoginState(username)
        _loginResult.value = LoginResult.Success(username)
    }

    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * 登录结果
     */
    sealed class LoginResult {
        data class Success(val username: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}


