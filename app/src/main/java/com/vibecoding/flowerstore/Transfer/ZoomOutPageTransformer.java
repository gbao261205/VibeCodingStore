package com.vibecoding.flowerstore.Transfer;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    @Override
    public void transformPage(@NonNull View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // Trang này đã ra khỏi màn hình bên trái.
            view.setAlpha(0f);
        } else if (position <= 1) { // [-1,1]
            // Sửa đổi hiệu ứng trượt mặc định để thu nhỏ trang.
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Thu nhỏ trang lại.
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Làm mờ trang khi nó ra xa.
            view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        } else { // (1,+Infinity]
            // Trang này đã ra khỏi màn hình bên phải.
            view.setAlpha(0f);
        }
    }
}

