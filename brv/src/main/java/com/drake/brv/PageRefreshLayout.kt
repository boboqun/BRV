/*
 * Copyright (C) 2018, Umbrella CompanyLimited All rights reserved.
 * Project：BRV
 * Author：Drake
 * Date：9/12/19 1:55 PM
 */

package com.drake.brv

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.listener.OnMultiStateListener
import com.drake.statelayout.StateConfig
import com.drake.statelayout.StateLayout
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshComponent
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener


/**
 * 扩展SmartRefreshLayout
 *
 * 功能:
 * - 下拉刷新
 * - 上拉加载
 * - 分页加载
 * - 添加数据
 * - 缺省状态页
 */
@Suppress("UNUSED_PARAMETER")
open class PageRefreshLayout : SmartRefreshLayout, OnRefreshLoadMoreListener {


    var emptyLayout = View.NO_ID
        set(value) {
            field = value
            state?.emptyLayout = value
        }
    var errorLayout = View.NO_ID
        set(value) {
            field = value
            state?.errorLayout = value
        }
    var loadingLayout = View.NO_ID
        set(value) {
            field = value
            state?.loadingLayout = value
        }
    var index = startIndex
    var stateEnabled = true // 启用缺省页

    companion object {

        var startIndex = 1
    }


    private var hasMore = true
    private var adapter: BindingAdapter? = null
    private var autoEnabledLoadMoreState = false
    private var contentView: View? = null
    private var state: StateLayout? = null

    private var onRefresh: (PageRefreshLayout.() -> Unit)? = null
    private var onLoadMore: (PageRefreshLayout.() -> Unit)? = null

    // <editor-fold desc="构造函数">

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PageRefreshLayout)

        try {
            stateEnabled =
                attributes.getBoolean(R.styleable.PageRefreshLayout_stateEnabled, stateEnabled)

            mEnableLoadMoreWhenContentNotFull = false
            mEnableLoadMoreWhenContentNotFull = attributes.getBoolean(
                com.scwang.smart.refresh.layout.kernel.R.styleable.SmartRefreshLayout_srlEnableLoadMoreWhenContentNotFull,
                mEnableLoadMoreWhenContentNotFull
            )

            emptyLayout = attributes.getResourceId(
                R.styleable.PageRefreshLayout_empty_layout,
                View.NO_ID
            )
            errorLayout = attributes.getResourceId(
                R.styleable.PageRefreshLayout_error_layout,
                View.NO_ID
            )
            loadingLayout = attributes.getResourceId(
                R.styleable.PageRefreshLayout_loading_layout,
                View.NO_ID
            )
        } finally {
            attributes.recycle()
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        init()
    }

    internal fun init() {

        setOnRefreshLoadMoreListener(this)
        autoEnabledLoadMoreState = mEnableLoadMore


        if (autoEnabledLoadMoreState) {
            setEnableLoadMore(false)
        }


        if (contentView == null) {
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                if (view !is RefreshComponent) {
                    contentView = view
                    break
                }
            }
        } else return

        if (stateEnabled) {

            if (StateConfig.errorLayout == View.NO_ID && errorLayout == View.NO_ID) {
                stateEnabled = false
                return
            }

            state = StateLayout(context)

            state?.let {

                removeView(contentView)
                state!!.addView(contentView)
                state!!.setContentView(contentView!!)
                setRefreshContent(state!!)

                it.emptyLayout = emptyLayout
                it.errorLayout = errorLayout
                it.loadingLayout = loadingLayout
            }
        }
    }

    // </editor-fold>


    // <editor-fold desc="刷新数据">

    /**
     * 触发刷新 (不包含下拉动画)
     */
    fun refresh() {
        notifyStateChanged(RefreshState.Refreshing)
        onRefresh(this)
    }


    /**
     * 直接接受数据, 自动判断当前属于下拉刷新还是上拉加载更多
     *
     * @param data List<Any?>? 数据集
     * @param hasMore [@kotlin.ExtensionFunctionType] Function1<PageRefreshLayout, Boolean> 在函数参数中返回布尔类型来判断是否存在更多页
     */
    fun addData(data: List<Any?>?, hasMore: BindingAdapter.() -> Boolean) {

        if (contentView == null && contentView !is RecyclerView) {
            throw UnsupportedOperationException("PageRefreshLayout require direct child is RecyclerView")
        }

        adapter = adapter ?: (contentView as RecyclerView).adapter as? BindingAdapter

        if (adapter == null) {
            throw UnsupportedOperationException("PageRefreshLayout require RecyclerView set BindingAdapter")
        }

        val isRefreshState = getState() == RefreshState.Refreshing

        adapter?.let {
            if (isRefreshState) {
                it.models = data

                if (data.isNullOrEmpty()) {
                    showEmpty()
                    return
                } else index++

            } else {
                it.addModels(data)
                index++
            }
        }

        this.hasMore = adapter!!.hasMore()
        if (isRefreshState) showContent() else finish(true)
    }

    // </editor-fold>


    // <editor-fold desc="生命周期">

    fun onError(block: View.() -> Unit) {
        state?.onError(block)
    }

    fun onEmpty(block: View.() -> Unit) {
        state?.onEmpty(block)
    }

    fun onLoading(block: View.() -> Unit) {
        state?.onLoading(block)
    }

    fun onRefresh(block: PageRefreshLayout.() -> Unit) {
        onRefresh = block
    }

    fun onLoadMore(block: PageRefreshLayout.() -> Unit) {
        onLoadMore = block
    }


    /**
     * 监听多种状态, 不会拦截已有的刷新(onRefresh)和加载生命周期(onLoadMore)
     * @param onMultiStateListener OnMultiStateListener
     * @return PageRefreshLayout
     */
    fun setOnMultiStateListener(onMultiStateListener: OnMultiStateListener) {
        setOnMultiListener(onMultiStateListener)
    }

    // </editor-fold>


    /**
     * 关闭下拉加载|上拉刷新
     * @param success Boolean 刷新结果 true: 成功 false: 失败
     */
    fun finish(success: Boolean = true) {
        val currentState = getState()
        if (currentState == RefreshState.Refreshing) {
            finishRefresh(success)
            setEnableRefresh(true)
        } else if (currentState == RefreshState.Loading) {
            if (hasMore) {
                finishLoadMore(success)
            } else {
                finishLoadMoreWithNoMoreData()
            }
        }
        if (currentState != RefreshState.Loading && autoEnabledLoadMoreState) {
            setEnableLoadMore(success)
        }
    }


    // <editor-fold desc="缺省页">


    fun showEmpty() {
        state?.showEmpty()
        finish()
    }


    fun showError() {
        state?.showError()
        finish(false)
    }

    fun showLoading() {
        state?.showLoading()
        setEnableRefresh(false)
        notifyStateChanged(RefreshState.Refreshing)
        onRefresh(this)
    }

    fun showContent() {
        state?.showContent()
        finish()
    }

    // </editor-fold>

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        if (onLoadMore == null) {
            onRefresh?.invoke(this)
        } else {
            onLoadMore?.invoke(this)
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        index = startIndex

        setNoMoreData(false)
        onRefresh?.invoke(this)
    }

}