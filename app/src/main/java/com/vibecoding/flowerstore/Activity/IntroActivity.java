package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.vibecoding.flowerstore.Adapter.IntroImageAdapter;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Transfer.DepthPageTransformer;

import java.util.Arrays;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 imageSliderPager;
    private List<View> indicators;
    private IntroImageAdapter adapter;

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

        // --- BẮT ĐẦU LOGIC KIỂM TRA LẦN ĐẦU MỞ APP ---
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (!isFirstRun) {
            // Nếu không phải lần đầu, vào thẳng MainActivity và kết thúc IntroActivity
            launchMainActivity();
            return; // Rất quan trọng: Dừng việc thực thi hàm onCreate tại đây
        }
        // --- KẾT THÚC LOGIC KIỂM TRA ---

        // Nếu là lần đầu, tiếp tục hiển thị giao diện Intro
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);

        imageSliderPager = findViewById(R.id.image_slider_pager);

        indicators = Arrays.asList(
                findViewById(R.id.indicator_1),
                findViewById(R.id.indicator_2),
                findViewById(R.id.indicator_3)
        );

        adapter = new IntroImageAdapter(imageList);
        imageSliderPager.setAdapter(adapter);
        imageSliderPager.setPageTransformer(new DepthPageTransformer());

        sliderRunnable = () -> {
            int currentPosition = imageSliderPager.getCurrentItem();
            int nextPosition = (currentPosition + 1) % adapter.getItemCount();
            imageSliderPager.setCurrentItem(nextPosition, true);
        };

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

        // Khi người dùng nhấn nút, lưu trạng thái và chuyển sang MainActivity
        findViewById(R.id.action_button).setOnClickListener(v -> {
            // --- LƯU TRẠNG THÁI ĐÃ XEM INTRO ---
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
            // --- KẾT THÚC LƯU TRẠNG THÁI ---

            launchMainActivity();
        });
    }

    private void updateIndicators(int currentPosition) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setBackgroundResource(i == currentPosition ? ACTIVE_INDICATOR : INACTIVE_INDICATOR);
        }
    }

    // Hàm tiện ích để khởi chạy MainActivity và kết thúc Activity hiện tại
    private void launchMainActivity() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sliderRunnable != null) { // Chỉ chạy slider nếu nó đã được khởi tạo
            sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
