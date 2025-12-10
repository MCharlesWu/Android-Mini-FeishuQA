package com.example.feishuqa.common.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 通用 ViewModel 工厂工具类
 * 使用泛型和内联函数简化 ViewModel 工厂的创建
 */
object ViewModelFactoryUtils {
    
    /**
     * 创建一个通用的 ViewModel 工厂
     * @param creator ViewModel 创建函数
     */
    inline fun <reified VM : ViewModel> create(
        crossinline creator: () -> VM
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(VM::class.java)) {
                    return creator() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
    
    /**
     * 创建需要 Application 参数的 ViewModel 工厂
     * @param application Application 实例
     * @param creator ViewModel 创建函数
     */
    inline fun <reified VM : ViewModel> createWithApplication(
        application: Application,
        crossinline creator: (Application) -> VM
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(VM::class.java)) {
                    return creator(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}

/**
 * 便捷的扩展函数，用于创建 ViewModel 工厂
 * 
 * 使用示例：
 * ```
 * val factory = viewModelFactory { MyViewModel() }
 * ```
 */
inline fun <reified VM : ViewModel> viewModelFactory(
    crossinline creator: () -> VM
): ViewModelProvider.Factory = ViewModelFactoryUtils.create(creator)

/**
 * 便捷的扩展函数，用于创建需要 Application 参数的 ViewModel 工厂
 * 
 * 使用示例：
 * ```
 * // 方式1：使用 lambda
 * val factory = application.viewModelFactory { MainViewModel(it) }
 * 
 * // 方式2：使用函数引用
 * val factory = application.viewModelFactory(::MainViewModel)
 * ```
 */
inline fun <reified VM : ViewModel> Application.viewModelFactory(
    crossinline creator: (Application) -> VM
): ViewModelProvider.Factory = ViewModelFactoryUtils.createWithApplication(this, creator)


