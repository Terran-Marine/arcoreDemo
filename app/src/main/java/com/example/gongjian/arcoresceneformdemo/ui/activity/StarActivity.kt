package com.example.gongjian.arcoresceneformdemo.ui.activity

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.gongjian.arcoresceneformdemo.R
import com.example.gongjian.arcoresceneformdemo.arSubassembly.DoubleTapTransformableNode
import com.example.gongjian.arcoresceneformdemo.utils.DemoUtils
import com.example.gongjian.arcoresceneformdemo.utils.ToastUtils
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
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
    private lateinit var andyModelRenderable: ModelRenderable
    private lateinit var tableModelRenderable: ModelRenderable
    private var hasFinishedLoading = false//初始化完成
//    private lateinit var luanControlText: ViewRenderable
//    private lateinit var earthControlText: ViewRenderable

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

        val andy = ModelRenderable.builder()
                .setSource(this@StarActivity, Uri.parse("andy.sfb"))
                .build()

        val table = ModelRenderable.builder()
                .setSource(this@StarActivity, Uri.parse("file2018.sfb"))
                .build()

        CompletableFuture.allOf(
                earthFuture,
                luanFuture,
                andy,
                table
        ).handle<Any> { notUsed, throwable ->
            if (throwable != null) {
                DemoUtils.displayError(this, "无法加载渲染模型", throwable)
            } else {
                try {
                    earthModelRenderable = earthFuture.get()
                    luanModelRenderable = luanFuture.get()
                    andyModelRenderable = andy.get()
                    tableModelRenderable = table.get()
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
//                .setSource()
    }

    private var addObjID = 0

    /**
     * 设置手势
     * */
    private fun setUpGesture() {
        UI_addEarth.setOnClickListener {
            addObjID = 1
            UI_cancel.visibility = View.VISIBLE
            UI_cancel.text = "已选择Earth,点击已识别出来的平面放置,取消点此处"
        }

        UI_addLuan.setOnClickListener {
            addObjID = 2
            UI_cancel.visibility = View.VISIBLE
            UI_cancel.text = "已选择Luan,点击已识别出来的平面放置,取消点此处"
        }

        UI_addAndy.setOnClickListener {
            addObjID = 3
            UI_cancel.visibility = View.VISIBLE
            UI_cancel.text = "已选择andy,点击已识别出来的平面放置,取消点此处"
        }

        UI_addTable.setOnClickListener {
            addObjID = 4
            UI_cancel.visibility = View.VISIBLE
            UI_cancel.text = "已选择桌子,点击已识别出来的平面放置,取消点此处"
        }

        UI_cancel.setOnClickListener {
            addObjID = 0
            UI_cancel.visibility = View.GONE
        }

        (UI_ArSceneView as ArFragment).setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (!hasFinishedLoading) {
                ToastUtils.getInstanc(this@StarActivity).showToast("稍等 初始化未完成")
                return@setOnTapArPlaneListener
            }

            if (addObjID == 0) {

                ToastUtils.getInstanc(this@StarActivity).showToast("选择需要放置的物体")
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
                    earthModelRenderable
                }
                2 -> {
                    luanModelRenderable
                }
                3 -> {
                    andyModelRenderable
                }
                4 -> {
                    Log.i(TAG, "设置table的Renderable")
                    tableModelRenderable
                }
                else -> {
                    null
                }
            }
            transformableNode.scaleController.maxScale = 2f
            transformableNode.scaleController.minScale = 0.1f

//            controlsNode.setParent(transformableNode)
//
//            controlsNode.localPosition = Vector3.up()
//
//            (controlsNode.renderable as ViewRenderable).view.findViewById<TextView>(R.id.UI_controlMsg).setOnClickListener {
//                anchorNode.removeChild(transformableNode)
//                when (addObjID) {
//                    1 -> {
//                        existEarth = false
//                    }
//                    2 -> {
//                        existLuan = false
//                    }
//                }
//            }

            transformableNode.setOnDoubleTapListener {
                anchorNode.removeChild(transformableNode)
            }
            addObjID = 0
            UI_cancel.visibility = View.GONE
            transformableNode.select()
        }
    }
}