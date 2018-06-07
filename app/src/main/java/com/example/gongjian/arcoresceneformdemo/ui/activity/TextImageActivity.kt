package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.gongjian.arcoresceneformdemo.R
import com.example.gongjian.arcoresceneformdemo.arSubassembly.DoubleTapTransformableNode
import com.example.gongjian.arcoresceneformdemo.utils.DemoUtils
import com.example.gongjian.arcoresceneformdemo.utils.ToastUtils
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.jph.takephoto.app.TakePhotoFragmentActivity
import com.jph.takephoto.model.TImage
import com.jph.takephoto.model.TResult
import kotlinx.android.synthetic.main.activity_text_image.*
import java.lang.Exception
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


class TextImageActivity : TakePhotoFragmentActivity() {
    val TAG="TextImageActivity";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_image)

        initViewRenderable()
        initView()
        setUpGesture()
    }


    private var hasFinishedLoading: Boolean = false

    private lateinit var textRenderable: ViewRenderable
    private lateinit var imageRenderable: ViewRenderable

    private var addObjID: Int = 0
    private val IMAGE = 1

    private var existText = false
    private var existImage = false

    private fun initViewRenderable() {
        val textFuture = ViewRenderable.builder()
                .setView(this@TextImageActivity, R.layout.renderable_text)
                .build()

        val imageFuture = ViewRenderable.builder()
                .setView(this@TextImageActivity, R.layout.renderable_image)
                .build()

        CompletableFuture.allOf(textFuture, imageFuture)
                .handle<Any> { notUsed, throwable ->
                    if (throwable != null) {
                        DemoUtils.displayError(this, "无法加载渲染模型", throwable)
                    } else {
                        try {
                            textRenderable = textFuture.get()
                            imageRenderable = imageFuture.get()
                            hasFinishedLoading = true
                        } catch (ex: InterruptedException) {
                            DemoUtils.displayError(this, "无法加载渲染模型", ex)
                        } catch (ex: ExecutionException) {
                            DemoUtils.displayError(this, "无法加载渲染模型", ex)
                        }
                    }
                    return@handle null
                }
    }

    lateinit var materialDialog: MaterialDialog.Builder

    private fun initView() {
        materialDialog = MaterialDialog.Builder(this)
                .title(R.string.input)
                .inputRangeRes(2, 20, R.color.colorPrimaryDark)
                .input(null, null, { dialog, input ->
                    //这里生成文字
                    textRenderable.view.findViewById<TextView>(R.id.UI_msg).text = input
                    addObjID = 1
                })
    }

    private fun setUpGesture() {
        //添加文字btn
        UI_addText.setOnClickListener {
            materialDialog.show()
        }

        //图片btn
        UI_addImage.setOnClickListener {
            takePhoto.onPickMultiple(1)
        }

        (UI_ArSceneView as ArFragment).setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (!hasFinishedLoading) {
                ToastUtils.getInstanc(this@TextImageActivity).showToast("稍等 初始化未完成")
                return@setOnTapArPlaneListener
            }

            if (addObjID == 0) {
                UI_hint.text = "选择一个需要放置的控件"
                return@setOnTapArPlaneListener
            }

            if ((addObjID == 1 && existText) || (addObjID == 2 && existImage)) {
                UI_hint.text = "当前控件已存在"
                return@setOnTapArPlaneListener
            }

            //创建一个锚点
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.setParent((UI_ArSceneView as ArFragment).arSceneView.scene)

            //创建一个可变换得到节点 在渲染对象放置在节点上
            val transformableNode = DoubleTapTransformableNode((UI_ArSceneView as ArFragment).transformationSystem)
            transformableNode.setParent(anchorNode)
            transformableNode.renderable = when (addObjID) {
                1 -> {
                    existText = true
                    textRenderable
                }
                2 -> {
                    existImage = true
                    imageRenderable
                }
                else -> {
                    null
                }
            }

            transformableNode.setOnDoubleTapListener {
                anchorNode.removeChild(transformableNode)
                when (addObjID) {
                    1 -> {
                        existText = false
                    }
                    2 -> {
                        existImage = false
                    }
                }
                addObjID = 0

            }
            addObjID = 0
            UI_hint.text = "放置成功"
            transformableNode.select()
        }
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        //获取图片路径
//        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
//            val selectedImage = data.data
//            val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
//            val c = contentResolver.query(selectedImage!!, filePathColumns, null, null, null)
//            c!!.moveToFirst()
//            val columnIndex = c.getColumnIndex(filePathColumns[0])
//            val imagePath = c.getString(columnIndex)
//            loadImage(imagePath)
//            c.close()
//        }
//    }

    override fun takeSuccess(result: TResult) {
        super.takeSuccess(result)
        loadImage(result.image)
    }

    private fun loadImage(image: TImage) {
        Glide.with(this@TextImageActivity)
                .load(image.compressPath)
                .error(R.drawable.error_image)
                .placeholder(R.drawable.error_image)
                .into(imageRenderable.view.findViewById(R.id.UI_image))

        Glide.with(this@TextImageActivity)
                .load(image.compressPath)
//                .apply(myOptions)
                .error(R.drawable.error_image)
                .placeholder(R.drawable.error_image)
                .into(object : GlideDrawableImageViewTarget(UI_testImage){
                    override fun onLoadStarted(placeholder: Drawable?) {
                        super.onLoadStarted(placeholder)
                        Log.i(TAG,"开始加载")
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        Log.i(TAG,"加载失败"+e.toString())
                    }

                    override fun onResourceReady(resource: GlideDrawable?, animation: GlideAnimation<in GlideDrawable>?) {
                        super.onResourceReady(resource, animation)
                        Log.i(TAG,"加载成功")
                    }
                })

        addObjID = 2

    }


//    val myOptions = RequestOptions().error(R.drawable.error_image).placeholder(R.drawable.error_image)

//    private fun loadImage(imagePath: String) {
//
//        val bm = BitmapFactory.decodeFile(imagePath)
//        imageRenderable.view.findViewById<ImageView>(R.id.UI_image).setImageBitmap(bm)
//        UI_testImage.setImageBitmap(bm)
//
//        Glide.with(this@TextImageActivity)
//                .load(imagePath)
//                .apply(myOptions)
//                .into(imageRenderable.view.findViewById(R.id.UI_image))
//
//
//        Glide.with(this@TextImageActivity)
//                .load(imagePath)
//                .apply(myOptions)
//                .into(UI_testImage)
//
//        addObjID = 2
//    }
}
