package com.example.feishuqa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.feishuqa.app.main.MainView
import com.example.feishuqa.app.main.MainViewModel
import com.example.feishuqa.app.main.MainViewModelFactory
import com.example.feishuqa.databinding.ActivityMainBinding

/**
 * 主界面Activity
 * Activity层：只负责初始化和绑定View、ViewModel
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mainView: MainView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 创建ViewModel
        viewModel = ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java]

        // 创建View并初始化
        val drawerBinding = com.example.feishuqa.databinding.LayoutDrawerBinding.bind(binding.navDrawer.root)
        val inputBarBinding = com.example.feishuqa.databinding.LayoutInputBarBinding.bind(binding.inputBar.root)
        
        mainView = MainView(this, binding, drawerBinding, inputBarBinding, viewModel, this)
        mainView.init()
    }

    /**
     * 处理返回键
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
