package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.example.gongjian.arcoresceneformdemo.R
import com.example.gongjian.arcoresceneformdemo.arSubassembly.DoubleTapTransformableNode
import com.example.gongjian.arcoresceneformdemo.utils.DemoUtils
import com.example.gongjian.arcoresceneformdemo.utils.ToastUtils
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.PlaneRenderer
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_star.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


/**
 * https://github.com/google-ar/arcore-android-sdk/releases  arcore 的apk下载地址
 * */
class ModelActivity : AppCompatActivity() {
    val TAG = "ModelActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star)

        initView()
        initModelRenderable()
        setUpGesture()
    }

    private fun initView() {
        UI_cancel.visibility = View.INVISIBLE
        MaterialDialog.Builder(this@ModelActivity)
                .backgroundColorRes(R.color.context2)
                .contentColorRes(R.color.white)
                .content("1.先检测平面,多个角度尝试可以加快平面检测\n2.当出现网点平面,选择放置对象\n3.点击屏幕放置对象\n手势:双击-取消,按住-拖拽,双指-旋转缩放")
                .positiveText("确认")
                .show()
    }

    private lateinit var benchModelRenderable: ModelRenderable
    private lateinit var sofaChairModelRenderable: ModelRenderable
    private lateinit var tableModelRenderable: ModelRenderable
    private lateinit var sofaModelRenderable: ModelRenderable
    private var hasFinishedLoading = false//初始化完成

    /**
     * 初始化ModelRenderable  渲染模型
     * */
    private fun initModelRenderable() {
        val benchFuture = ModelRenderable.builder()
                .setSource(this@ModelActivity, Uri.parse("model-good.sfb"))
                .build()

        val sofaChairFuture = ModelRenderable.builder()
                .setSource(this@ModelActivity, Uri.parse("testsofa02.sfb"))
                .build()

        val tableFuture = ModelRenderable.builder()
                //使用网络加载
                .setSource(this@ModelActivity, Uri.parse("https://djfile.oss-cn-shanghai.aliyuncs.com/zhuozi_01.sfb"))
                .build()

        val sofaFuture = ModelRenderable.builder()
                .setSource(this@ModelActivity, Uri.parse("Distrcteight_01.sfb"))
                .build()

        CompletableFuture.allOf(
                benchFuture,
                sofaChairFuture,
                tableFuture,
                sofaFuture
        ).handle<Any> { notUsed, throwable ->
            if (throwable != null) {
                DemoUtils.displayError(this, "无法加载渲染模型", throwable)
            } else {
                try {
                    benchModelRenderable = benchFuture.get()
                    sofaChairModelRenderable = sofaChairFuture.get()
                    tableModelRenderable = tableFuture.get()
                    sofaModelRenderable = sofaFuture.get()
                    hasFinishedLoading = true
                    Log.i(TAG, "模型渲染完成,当前线程" + Thread.currentThread().name)
                } catch (ex: InterruptedException) {
                    DemoUtils.displayError(this, "Unable to load renderable", ex)
                } catch (ex: ExecutionException) {
                    DemoUtils.displayError(this, "Unable to load renderable", ex)
                }
            }
            return@handle null
        }

        (UI_ArSceneView as ArFragment).arSceneView.planeRenderer.material.thenAccept { material ->
            material.setFloat3(PlaneRenderer.MATERIAL_COLOR, com.google.ar.sceneform.rendering.Color(0.3f, 0.3f, 0.3f, 0.1f))
        }
    }

    private var addObjID = 0

    /**
     * 设置手势
     * */
    private fun setUpGesture() {
        //长椅
        UI_addEarth.setOnClickListener {
            addObjID = 1
            UI_cancel.visibility = View.VISIBLE
            UI_cancelIcon.setImageResource(R.drawable.icon001)
        }

        //沙发凳子
        UI_addAndy.setOnClickListener {
            addObjID = 2
            UI_cancel.visibility = View.VISIBLE
            UI_cancelIcon.setImageResource(R.drawable.icon002)
        }

        //桌子
        UI_addTable.setOnClickListener {
            addObjID = 3
            UI_cancel.visibility = View.VISIBLE
            UI_cancelIcon.setImageResource(R.drawable.icon003)
        }

        //沙发
        UI_addLuan.setOnClickListener {
            addObjID = 4
            UI_cancel.visibility = View.VISIBLE
            UI_cancelIcon.setImageResource(R.drawable.sofa)
        }

        UI_cancel.setOnClickListener {
            addObjID = 0
            UI_cancel.visibility = View.GONE
        }

        (UI_ArSceneView as ArFragment).setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (!hasFinishedLoading) {
                ToastUtils.getInstanc(this@ModelActivity).showToast("稍等 初始化未完成")
                return@setOnTapArPlaneListener
            }

            if (addObjID == 0) {
                ToastUtils.getInstanc(this@ModelActivity).showToast("选择需要放置的物体")
                return@setOnTapArPlaneListener
            }

            //创建一个锚点
            val anchorNode = AnchorNode(hitResult.createAnchor()).apply {
                setParent((UI_ArSceneView as ArFragment).arSceneView.scene)
            }

            //创建一个可变换得到节点 在渲染对象放置在节点上
            val transformableNode = TransformableNode((UI_ArSceneView as ArFragment).transformationSystem).apply {
                setParent(anchorNode)
            }

            transformableNode.renderable = when (addObjID) {
                1 -> {
                    benchModelRenderable
                }
                2 -> {
                    sofaChairModelRenderable
                }
                3 -> {
                    tableModelRenderable
                }
                4 -> {
                    sofaModelRenderable
                }
                else -> {
                    null
                }
            }
            addObjID = 0
            transformableNode.apply {
                scaleController.maxScale = 2f
                scaleController.minScale = 0.1f
//                setOnDoubleTapListener {
//                    anchorNode.removeChild(this)
//                }
                select()
            }
            UI_cancel.visibility = View.GONE
        }
    }
}