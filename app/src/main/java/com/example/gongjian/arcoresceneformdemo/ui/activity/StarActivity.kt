package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.example.gongjian.arcoresceneformdemo.R
import com.example.gongjian.arcoresceneformdemo.arSubassembly.DoubleTapTransformableNode
import com.example.gongjian.arcoresceneformdemo.utils.DemoUtils
import com.example.gongjian.arcoresceneformdemo.utils.ToastUtils
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_star.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


/**
 * https://github.com/google-ar/arcore-android-sdk/releases  arcore 的apk下载地址
 * */
class StarActivity : AppCompatActivity() {
    val TAG = "StarActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star)

//        UI_ArSceneView

        initModelRenderable()
        setUpGesture()
    }

    private lateinit var earthModelRenderable: ModelRenderable
    private lateinit var luanModelRenderable: ModelRenderable
    private var hasFinishedLoading = false//初始化完成
    private lateinit var luanControlText: ViewRenderable
    private lateinit var earthControlText: ViewRenderable

    /**
     * 初始化ModelRenderable  渲染模型
     * */
    private fun initModelRenderable() {
        val earthFuture = ModelRenderable.builder()
                .setSource(this@StarActivity, Uri.parse("Earth.sfb"))
                .build()

        val luanFuture = ModelRenderable.builder()
                .setSource(this@StarActivity, Uri.parse("Luna.sfb"))
                .build()

        val luanControlTextFuture = ViewRenderable.builder()
                .setView(this@StarActivity, R.layout.control_text)
                .build()

        val earthControlTextFuture = ViewRenderable.builder()
                .setView(this@StarActivity, R.layout.control_text)
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
                    luanControlText = luanControlTextFuture.get()
                    earthControlText = earthControlTextFuture.get()
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

    private var existEarth = false
    private var existLuan = false
    private var addObjID = 0

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
                ToastUtils.getInstanc(this@StarActivity).showToast("稍等 初始化未完成")
                return@setOnTapArPlaneListener
            }

            if (addObjID == 0) {
                UI_hint.text = "选择一个需要放置的星球"
//                ToastUtils.getInstanc(this@StarActivity).showToast("选择需要放置的对象")
                return@setOnTapArPlaneListener
            }

            if ((addObjID == 1 && existEarth) || (addObjID == 2 && existLuan)) {
                UI_hint.text = "已存在这个星球,一种星球只能放置一个"
//                ToastUtils.getInstanc(this).showToast("已存在")
                return@setOnTapArPlaneListener
            }

            //创建一个锚点
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.setParent((UI_ArSceneView as ArFragment).arSceneView.scene)

            //创建一个可变换得到节点 在渲染对象放置在节点上
            val transformableNode = DoubleTapTransformableNode((UI_ArSceneView as ArFragment).transformationSystem)
            transformableNode.setParent(anchorNode)
            val controlsNode = Node()


            transformableNode.renderable = when (addObjID) {
                1 -> {
                    controlsNode.renderable = earthControlText
                    existEarth = true
                    earthModelRenderable
                }
                2 -> {
                    controlsNode.renderable = luanControlText
                    existLuan = true
                    luanModelRenderable
                }
                else -> {
                    null
                }
            }
            transformableNode.scaleController.maxScale = 2f
            transformableNode.scaleController.minScale = 0.01f

            controlsNode.setParent(transformableNode)

            controlsNode.localPosition = Vector3.up()

            (controlsNode.renderable as ViewRenderable).view.findViewById<TextView>(R.id.UI_controlMsg).setOnClickListener {
                anchorNode.removeChild(transformableNode)
                when (addObjID) {
                    1 -> {
                        existEarth = false
                    }
                    2 -> {
                        existLuan = false
                    }
                }
            }

            transformableNode.setOnDoubleTapListener {
                anchorNode.removeChild(transformableNode)
                when (addObjID) {
                    1 -> {
                        existEarth = false
                    }
                    2 -> {
                        existLuan = false
                    }
                }
            }
            addObjID = 0
            UI_hint.text = "放置成功"
            transformableNode.select()
        }
    }
}