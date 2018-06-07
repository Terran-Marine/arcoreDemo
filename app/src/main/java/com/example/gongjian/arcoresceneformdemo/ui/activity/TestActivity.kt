package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.gongjian.arcoresceneformdemo.R
import com.jph.takephoto.app.TakePhotoActivity
import com.jph.takephoto.model.TResult
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : TakePhotoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        UI_btn.setOnClickListener {
            takePhoto.onPickMultiple(1)
        }

    }

    override fun takeSuccess(result: TResult) {
        super.takeSuccess(result)
        Glide.with(this@TestActivity)
                .load(result.image.compressPath)
                .error(R.drawable.error_image)
                .placeholder(R.drawable.error_image)
                .into(UI_iv)
    }
}
