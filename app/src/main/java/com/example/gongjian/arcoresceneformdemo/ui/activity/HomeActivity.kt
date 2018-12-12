package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.gongjian.arcoresceneformdemo.R
import com.example.gongjian.arcoresceneformdemo.R.id.UI_starCardView
import com.example.gongjian.arcoresceneformdemo.R.id.UI_textImageCardView
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initView()
    }

    private fun initView() {
        UI_starCardView.setOnClickListener {
            this@HomeActivity.startActivity(Intent().apply {
                setClass(this@HomeActivity, ModelActivity::class.java)
            })
        }

        UI_textImageCardView.setOnClickListener {
            this@HomeActivity.startActivity(Intent().apply {
                setClass(this@HomeActivity, TextImageActivity::class.java)
            })
        }

//        UI_test.setOnClickListener {
//            val intent = Intent()
//            intent.setClass(this@HomeActivity, TestActivity::class.java)
//            this@HomeActivity.startActivity(intent)
//        }
    }
}
