package com.peter.slideview.sview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by wuyajun on 16/3/8.
 * Detail:侧边menu
 */
public class NNSliderView extends ViewGroup {
    //速度边界
    private final int VELOCITY_XY_SPEED = 4200;
    //持续滑动距离|如果持续滑动此距离 则 认为要收回menu
    private final int SLIDE_DISTANCE = 220;
    //点击 辩识 范围
    private final int CLICK_DISTANCE = 5;
    //滚动速度
    private final int SCROLL_SPEED = 450;


    private Scroller scroller;
    /* 触摸事件是否已分发给子view */
    private boolean dispatched;
    private boolean menuStatus = true;

    private int startScrollLeftOffset;
    private VelocityTracker mVelocityTracker;

    public NNSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
    }

    private View mChildAt0;
    private View mChildAt1;
    private int mSlideMaxLimit;//menu 3/4

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View view = getChildAt(i);
            if (view.getVisibility() != View.GONE) {
                view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
        }
        if (!menuStatus) return;
        scroller.startScroll(0, getTop(), arg3, 0, 0);//默认隐藏

        mChildAt0 = getChildAt(0);
        mChildAt1 = getChildAt(1);
        mSlideMaxLimit = (mChildAt1.getWidth() * 3 / 4) / 2;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!menuStatus) { //区域拦截
            if (ev.getX() < mChildAt1.getWidth() / 4) {
                return false;
            }
        }
        return true;
    }

    private final float minMove = 40;//平移移动距离-上、下、左、右
    private final float maxLFMove = 800;//左右倾斜移动距离限制
    private final float maxUDMove = 180;//上下倾斜移动距离限制 - 120

    private float mStartX;
    private float mStartY;
    private float mEndX;
    private float mEndY;

    /* 最后一次触摸的x位置 */
    private float touchX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mStartX = event.getX();
            mStartY = event.getY();

            mEndX = event.getX();
            mEndY = event.getY();

            touchX = event.getX();

            if (isSlided()) {//分发事件
                dispatched = dispatchTouchEventToView(mChildAt0, event);
            } else {
                dispatched = dispatchTouchEventToView(mChildAt1, event);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            mEndX = event.getX();
            mEndY = event.getY();

            if (dispatched) {//分发事件
                if (isSlided()) {
                    dispatchTouchEventToView(mChildAt0, event);
                } else {
                    if (Math.abs(mEndY - mStartY) > minMove && Math.abs(mEndX - mStartX) < maxUDMove) {//向上|向下
                        dispatchTouchEventToView(mChildAt1, event);
                    } else if (Math.abs(mEndX - mStartX) > minMove && Math.abs(mEndY - mStartY) < maxLFMove) {////向左|向右
                        setLayoutXY(event);
                    }
                }
            } else {//重置第一个子view位置
                setLayoutXY(event);
            }

            touchX = event.getX();
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            if (dispatched) {//分发事件
                if (isSlided()) {
                    dispatchTouchEventToView(mChildAt0, event);
                } else {
                    if (Math.abs(mEndX - mStartX) < CLICK_DISTANCE && Math.abs(mEndY - mStartY) < CLICK_DISTANCE) {
                        event.setLocation(event.getX(), event.getY());
                        dispatchTouchEventToView(mChildAt1, event);
                    }/* else if (Math.abs(mEndX - mStartX) > SLIDE_DISTANCE) {
                        setSlided(true);
                    } */ else {
                        patchView(event);
                    }
                }
            } else {
                patchView(event);
            }
        }
        return true;
    }

    private void patchView(MotionEvent event) {
        mVelocityTracker.computeCurrentVelocity(1200);//-1000 //判断速度
        if (touchX >= mChildAt1.getWidth() / 4) {
            int velocityX = (int) mVelocityTracker.getXVelocity();
            int velocityY = (int) mVelocityTracker.getYVelocity();

            if (velocityY > -VELOCITY_XY_SPEED) {
                if (velocityX > VELOCITY_XY_SPEED) {
                    setSlided(true);
                } else if (velocityX < -VELOCITY_XY_SPEED) {
                    setSlided(false);
                } else {
                    if (mChildAt1.getLeft() >= mSlideMaxLimit - 20) {
                        setSlided(true);
                    } else {
                        setSlided(false);
                    }
                }
            }else{
                setSlided(false);
            }

            event.setLocation(Integer.MAX_VALUE, 0);
            dispatchTouchEventToView(mChildAt1, event);
        } else {
            //事件分发给同级view，相应相应位置-分发之前做拦截-onInterceptTouchEvent
            dispatchTouchEventToView(mChildAt1, event);
            setSlided(false);
        }
        postInvalidate();
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    //设置布局位置
    private void setLayoutXY(MotionEvent event) {
        if (mChildAt1 != null) {
            float dx = event.getX() - touchX;
            int left = (int) (mChildAt1.getLeft() + dx);
            if (left >= 0) {
                mChildAt1.layout(left, mChildAt1.getTop(), mChildAt1.getWidth() + left, mChildAt1.getTop() + mChildAt1.getHeight());
            }
        }
    }

    public boolean isSlided() {
        return menuStatus;
    }

    /**
     * 设置是否滑动显示菜单状态，并动画滑动效果
     *
     * @param slided
     */
    public void setSlided(boolean slided) {
        if (mChildAt1 != null) {
            startScrollLeftOffset = mChildAt1.getLeft();
            if (slided) {
                scroller.startScroll(0, getTop(), mChildAt1.getWidth() - startScrollLeftOffset, 0, SCROLL_SPEED);
            } else {
                scroller.startScroll(0, getTop(), -startScrollLeftOffset, 0, SCROLL_SPEED);
            }
        }
        this.menuStatus = slided;
        postInvalidate();
    }

    public void closeMenu() {
        setSlided(true);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int left = startScrollLeftOffset + scroller.getCurrX();
            if (mChildAt1 != null) {
                mChildAt1.layout(left, mChildAt1.getTop(), left + mChildAt1.getWidth(), mChildAt1.getHeight());
            }
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public boolean dispatchTouchEventToView(View view, MotionEvent ev) {
        try {
            return view.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace(); //部分机型会抛异常
        }
        return false;
    }
}