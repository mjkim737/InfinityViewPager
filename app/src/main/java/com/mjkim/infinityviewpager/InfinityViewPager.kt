package com.mjkim.infinityviewpager

import android.content.Context
import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

public class InfinityViewPager() {
    private lateinit var mContext : Context
    private var mPeriod: Long = 0L
    private var mInitialDelay: Long = 0L
    private lateinit var mViewPagerTimer: Timer
    private var mFragmentLayout: Int = 0
    private var mIsTimerWorking = false
    private var mTimerEnabled = false
    private lateinit var mViewpager: ViewPager2

    public constructor(context : Context, fragment : Int, viewpager: ViewPager2, list: ArrayList<Any>) : this() {
        this.mContext = context
        this.mFragmentLayout = fragment
        this.mViewpager = viewpager
        initViewPager(list)
    }

    private val mViewPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            val fakeSize = mViewpager.adapter!!.itemCount
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                when (mViewpager.currentItem) {
                    0 -> mViewpager.setCurrentItem(fakeSize - 2, false)
                    fakeSize - 1 -> mViewpager.setCurrentItem(1, false)
                }
                if (!mIsTimerWorking) {
                    startViewPagerTimer()
                }
            } else if (state == ViewPager.SCROLL_STATE_DRAGGING && mIsTimerWorking) {
                stopViewPagerTimer()
            }
        }
    }

    private fun initViewPager(imageList: ArrayList<Any>) {
        val glideViewPagerAdapter = InfinityViewPagerAdapter(addTwoList(imageList), mFragmentLayout)

        mViewpager.apply {
            adapter = glideViewPagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(mViewPageChangeListener)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setCurrentItem(2, false)
        }
    }

    private fun addTwoList(originalList: ArrayList<Any>): ArrayList<Any> {
        val originalSize = originalList.size
        val tempList: ArrayList<Any> = ArrayList()
        for (i in 0 until originalSize + 2) {
            tempList.add(originalList[(i + originalSize - 2) % originalSize])
        }
        return tempList
    }

    private fun startViewPagerTimer() {
        if (mTimerEnabled) {
            mIsTimerWorking = true
            val fakeSize = mViewpager.adapter?.itemCount!!
            mViewPagerTimer = timer(period = mPeriod, initialDelay = mInitialDelay) {
                Handler(mContext.mainLooper).post {
                    when (mViewpager.currentItem) {
                        fakeSize - 1 -> {
                            mViewpager.setCurrentItem(2, false)
                        }
                        0 -> {
                            mViewpager.setCurrentItem(fakeSize - 1, false)
                        }
                        else -> {
                            mViewpager.setCurrentItem(mViewpager.currentItem + 1, false)
                        }
                    }
                }
            }
        }
    }

    private fun stopViewPagerTimer() {
        mIsTimerWorking = false
        mViewPagerTimer.cancel()
    }

    public fun setTimerEnabled(enabled: Boolean, period : Long = 3000, initialDelay: Long = 3000) {
        mTimerEnabled = enabled
        mPeriod = period
        mInitialDelay = initialDelay

        if (!mTimerEnabled && mIsTimerWorking) {
            stopViewPagerTimer()
        }else if (mTimerEnabled && !mIsTimerWorking) {
            startViewPagerTimer()
        }
    }
}