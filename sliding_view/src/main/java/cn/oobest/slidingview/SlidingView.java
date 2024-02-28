package cn.oobest.slidingview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.coordinatorlayout.widget.CoordinatorLayout;


public class SlidingView extends CoordinatorLayout {


    public interface SlidingListener {

        /**
         * @param direction 方向 1：left，0:right
         */
        void onStartSliding(int direction);

        /**
         * @param status 当前状态  1：打开，0:关闭
         */
        void onEndSliding(int status);
    }

    private View mBelowView;

    // 上面遮盖层视图
    private View mAboveView;

    private float mStartX;

    private float mAboveViewStartX;

    private long mStartTimeMillis;
    private float mLastX;

    private final float mTouchSlop;

    private boolean isSliding = false; //是否处于滑动状态

    private boolean slideEnable;

    private SlidingListener slidingListener;

    private final int aboveViewId;
    private final int belowViewId;


    public SlidingView(Context context) {
        this(context, null);
    }

    public SlidingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SlidingView, 0, 0);

        aboveViewId = a.getResourceId(R.styleable.SlidingView_above_view, 0);
        belowViewId = a.getResourceId(R.styleable.SlidingView_below_view, 0);

        a.recycle();
    }


    public void setSlidingListener(SlidingListener slidingListener) {
        this.slidingListener = slidingListener;
    }

    public boolean isSlideEnable() {
        return slideEnable;
    }

    /**
     * @param slideEnable 是否可以左滑
     */
    public void setSlideEnable(boolean slideEnable) {
        if (this.mBelowView == null) {
            this.slideEnable = false;
        } else {
            this.slideEnable = slideEnable;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (aboveViewId != 0) {
            this.mAboveView = findViewById(aboveViewId);
        }
        if (this.mAboveView == null) {
            throw new NullPointerException("this.mAboveView=null, please set app:above_view");
        }
        if (belowViewId != 0) {
            this.mBelowView = findViewById(belowViewId);
        }
        if (mBelowView != null) {
            LayoutParams layoutParams = (LayoutParams) mBelowView.getLayoutParams();
            layoutParams.setBehavior(new RightViewBehavior(aboveViewId));
            slideEnable = true;
        } else {
            slideEnable = false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mAboveViewStartX = mAboveView.getX();
                mStartX = x;
                mStartTimeMillis = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveDistance = x - mLastX; //手指滑动的距离
                if (!isSliding && Math.abs(moveDistance) > mTouchSlop) {
                    isSliding = true;
                    if (slidingListener != null) {
                        slidingListener.onStartSliding(moveDistance < 0 ? 1 : 0);
                    }
                }

                if (isSliding) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float moveViewX = mAboveView.getX();
                    float tempX = moveViewX + moveDistance; //预计moveView滑动后的x
                    // moveView的X的区间：-rightViewWidth < x < leftViewWidth
                    if (tempX >= 0) { //向右滑动
                        mAboveView.setX(0);
                    } else { //向左滑动
                        if (slideEnable) {
                            int rightViewWidth = mBelowView.getWidth();
                            mAboveView.setX(Math.max(-rightViewWidth, tempX));
                        } else {
                            mAboveView.setX(0);
                        }
                    }
                    mLastX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isSliding) {
                    float moveViewX = mAboveView.getX();
                    long diffTime = System.currentTimeMillis() - mStartTimeMillis;
                    if (diffTime == 0) {
                        diffTime = 1;
                    }
                    float distanceX = x - mStartX;
                    // 当前滑动速度
                    float velocityX = distanceX / diffTime;
                    if (slideEnable) {
                        float halfWidth = mBelowView.getWidth() / 2f;
                        float targetX;
                        if (mAboveViewStartX == 0 && velocityX < -1.4) {
                            targetX = -mBelowView.getWidth();
                        } else if (mAboveViewStartX != 0 && velocityX > 1.4) {
                            targetX = 0f;
                        } else if (moveViewX < -halfWidth) {
                            targetX = -mBelowView.getWidth();
                        } else {
                            targetX = 0f;
                        }
                        ObjectAnimator animator = ObjectAnimator.ofFloat(mAboveView, "X", mAboveView.getX(), targetX);
                        animator.setDuration(200);
                        animator.start();
                    }
                    isSliding = false;
                    if (slidingListener != null) {
                        slidingListener.onEndSliding(moveViewX < 0 ? 1 : 0);
                    }
                    return true;
                }

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isSliding || super.onTouchEvent(ev);
    }

    /**
     * 重置view状态
     */
    public void setClose() {
        mAboveView.setX(0f);
        isSliding = false;
    }

    public void animateClose() {
        if (mAboveView.getX() < 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mAboveView, "X", mAboveView.getX(), 0f);
            animator.setDuration(200);
            animator.start();
        }
        isSliding = false;
    }

    public void setOpen() {
        mAboveView.setX(-mBelowView.getWidth());
        isSliding = false;
    }

}
