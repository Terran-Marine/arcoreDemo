package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.gongjian.arcoresceneformdemo.R
import kotlinx.android.synthetic.main.activity_test.*
import permissions.dispatcher.*


@RuntimePermissions
class TestActivity : AppCompatActivity() {

    private val IMAGE_REQUEST_CODE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)


        UI_btn.setOnClickListener {
            showPhotoWithPermissionCheck()
            showPhoto()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode,grantResults)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
     fun showPhoto() {
        val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }


    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setMessage("需要这个权限获取照片")
                .setPositiveButton("同意", { dialog, button -> request.proceed() })
                .setNegativeButton("拒接", { dialog, button -> request.cancel() })
                .show()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun showDeniedForCamera() {
        Toast.makeText(this, "给个权限啦", Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun showNeverAskForCamera() {
        Toast.makeText(this, "给个权限啦", Toast.LENGTH_SHORT).show()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        super.onActivityResult(requestCode, resultCode, data)
        //在相册里面选择好相片之后调回到现在的这个activity中
        when (requestCode) {
            IMAGE_REQUEST_CODE//这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
            -> if (resultCode == Activity.RESULT_OK) {//resultcode是setResult里面设置的code值
                try {
                    val selectedImage = data.data //获取系统返回的照片的Uri
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = contentResolver.query(selectedImage!!,
                            filePathColumn, null, null, null)//从系统表中查询指定Uri对应的照片
                    cursor!!.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val path = cursor.getString(columnIndex)  //获取照片路径
                    cursor.close()
                    val bitmap = BitmapFactory.decodeFile(path)

                    loadImage(path)

//                    UI_iv.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // TODO Auto-generatedcatch block
                    e.printStackTrace()
                }

            }
        }
    }

    private fun loadImage(btmap: String?) {
//        UI_iv.setImageBitmap(btmap)
        Glide.with(this@TestActivity)
                .load(btmap)
                .into(UI_iv)
    }
}
