package com.vibecoding.flowerstore.Transfer; // Hoặc package bạn đã tạo file này

import android.view.View;
import androidx.annotation.NonNull;import androidx.viewpager2.widget.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(@NonNull View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // Trang này đã hoàn toàn đi ra khỏi màn hình bên trái.
            view.setAlpha(0f);

        } else if (position <= 0) { // [-1,0]
            // Sử dụng hiệu ứng mặc định khi trượt sang trái.
            view.setAlpha(1f);
            view.setTranslationX(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);

        } else if (position <= 1) { // (0,1]
            // Hiệu ứng làm mờ và lùi trang ra xa.
            // Làm mờ trang (Fade the page out).
            view.setAlpha(1 - position);

            // Di chuyển ngược lại với hiệu ứng trượt mặc định.
            view.setTranslationX(pageWidth * -position);

            // Thu nhỏ trang lại (scale the page down).
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // Trang này đã hoàn toàn đi ra khỏi màn hình bên phải.
            view.setAlpha(0f);
        }
    }
}
