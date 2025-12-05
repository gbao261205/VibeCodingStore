package com.vibecoding.flowerstore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.vibecoding.flowerstore.Adapter.IntroImageAdapter;
// Đảm bảo đường dẫn import này chính xác
import com.vibecoding.flowerstore.Transfer.DepthPageTransformer;
import com.vibecoding.flowerstore.Transfer.ZoomOutPageTransformer;

import java.util.Arrays;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 imageSliderPager;
    private List<View> indicators; // Biến này sẽ được khởi tạo trong onCreate
    private IntroImageAdapter adapter;

    // --- PHẦN CODE TỰ ĐỘNG TRƯỢT ---
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private static final long SLIDE_DELAY_MS = 5000; // 5 giây

    private final int ACTIVE_INDICATOR = R.drawable.indicator_active;
    private final int INACTIVE_INDICATOR = R.drawable.indicator_inactive;

    private final List<Integer> imageList = Arrays.asList(
            R.drawable.slide_image_1,
            R.drawable.slide_image_2,
            R.drawable.slide_image_3
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        imageSliderPager = findViewById(R.id.image_slider_pager);

        // --- SỬA LỖI: ÁNH XẠ CÁC INDICATORS ---
        // Đảm bảo trong file activity_intro.xml bạn có các View với ID này
        indicators = Arrays.asList(
                findViewById(R.id.indicator_1),
                findViewById(R.id.indicator_2),
                findViewById(R.id.indicator_3)
        );
        // ------------------------------------------

        adapter = new IntroImageAdapter(imageList);
        imageSliderPager.setAdapter(adapter);

        // Áp dụng hiệu ứng chuyển trang
        imageSliderPager.setPageTransformer(new DepthPageTransformer());

        // Logic tự động trượt
        sliderRunnable = () -> {
            int currentPosition = imageSliderPager.getCurrentItem();
            int nextPosition = (currentPosition + 1) % adapter.getItemCount();
            imageSliderPager.setCurrentItem(nextPosition, true);
        };

        // Gọi updateIndicators lần đầu
        updateIndicators(0);

        imageSliderPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
            }
        });

        // Nút chuyển sang MainActivity
        findViewById(R.id.action_button).setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateIndicators(int currentPosition) {
        // Bây giờ 'indicators' sẽ không còn là null
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setBackgroundResource(i == currentPosition ? ACTIVE_INDICATOR : INACTIVE_INDICATOR);
        }
    }

    // --- Quản lý vòng đời ---

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
