package com.vibecoding.flowerstore.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    // Biến tĩnh (static) để lưu dữ liệu trong suốt quá trình app chạy
    // Khi tắt app hẳn thì dữ liệu này mới mất
    public static List<Product> cachedProducts = null;
    public static List<Category> cachedCategories = null;

    public static List<Product> cachedFavorites = null;
    public static Map<String, List<Product>> categoryCache = new HashMap<>();
}