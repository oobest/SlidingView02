package cn.oobest.slidingview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * 右边View的Behavior
 */
public class RightViewBehavior extends CoordinatorLayout.Behavior<View> {

    private final int aboveViewId;

    public RightViewBehavior(int aboveViewId) {
        super();
        this.aboveViewId = aboveViewId;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, View dependency) {
        return dependency.getId() == aboveViewId;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, View child, View dependency) {
        float x = dependency.getX();
        int width = dependency.getWidth();
        child.setX(width + x);
        return true;
    }
}