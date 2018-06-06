package com.example.gongjian.arcoresceneformdemo

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.gongjian.arcoresceneformdemo.utils.DemoUtils
import com.example.gongjian.arcoresceneformdemo.utils.ToastUtils
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


/**
 * https://github.com/google-ar/arcore-android-sdk/releases  arcore 的apk下载地址
 * */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        UI_ArSceneView

        initModelRenderable()
        setUpGesture()
    }


    lateinit var earthModelRenderable: ModelRenderable
    lateinit var luanModelRenderable: ModelRenderable
    private var hasFinishedLoading = false//初始化完成

    /**
     * 初始化ModelRenderable  渲染模型
     * */
    private fun initModelRenderable() {
        val earthFuture = ModelRenderable.builder()
                .setSource(this@MainActivity, Uri.parse("Earth.sfb"))
                .build()

        val luanFuture = ModelRenderable.builder()
                .setSource(this@MainActivity, Uri.parse("Luna.sfb"))
                .build()

        CompletableFuture.allOf(
                earthFuture,
                luanFuture
        ).handle<Any> { notUsed, throwable ->
            if (throwable != null) {
                DemoUtils.displayError(this, "无法加载渲染模型", throwable)
            } else {
                try {
                    earthModelRenderable = earthFuture.get()
                    luanModelRenderable = luanFuture.get()
                    hasFinishedLoading = true
                } catch (ex: InterruptedException) {
                    DemoUtils.displayError(this, "Unable to load renderable", ex)
                } catch (ex: ExecutionException) {
                    DemoUtils.displayError(this, "Unable to load renderable", ex)
                }
            }
            return@handle null
        }

//                .setSource()
    }


    var existEarth = false
    var existLuan = false
    var addObjID = 0

    /**
     * 设置手势
     * */
    private fun setUpGesture() {
        UI_addEarth.setOnClickListener {
            addObjID = 1
        }

        UI_addLuan.setOnClickListener {
            addObjID = 2
        }



        (UI_ArSceneView as ArFragment).setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (!hasFinishedLoading) {
                ToastUtils.getInstanc(this@MainActivity).showToast("稍等 初始化未完成")
                return@setOnTapArPlaneListener
            }

            if (addObjID == 0) {
                ToastUtils.getInstanc(this@MainActivity).showToast("选择需要放置的对象")
                return@setOnTapArPlaneListener
            }

            if ((addObjID == 1 && existEarth) || (addObjID == 2 && existLuan)) {
                ToastUtils.getInstanc(this).showToast("已存在")
            }


            //创建一个锚点
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.setParent((UI_ArSceneView as ArFragment).arSceneView.scene)

            //创建一个可变换得到节点 在渲染对象放置在节点上
            val transformableNode = TransformableNode((UI_ArSceneView as ArFragment).transformationSystem)
            transformableNode.setParent(anchorNode)
            transformableNode.renderable = when (addObjID) {
                1 -> {
                    existEarth = true
                    earthModelRenderable
                }
                2 -> {
                    existLuan = true
                    luanModelRenderable
                }
                else -> {
                    null
                }
            }
            transformableNode.select()
        }
    }
}
