package com.vibecoding.flowerstore;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // CẤU HÌNH CLOUDINARY
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");
        // config.put("secure", "true"); // Tùy chọn: nếu muốn dùng URL https

        MediaManager.init(this, config);
    }
}
    