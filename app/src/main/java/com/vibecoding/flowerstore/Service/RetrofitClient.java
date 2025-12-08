package com.vibecoding.flowerstore.Service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://holetinnghia-vibe-coding-store-api.hf.space/api/v1/";
    private static Retrofit retrofit;

    // Tách thành 2 biến riêng biệt để không bị xung đột
    private static Retrofit retrofitPublic; // Dùng cho Login, Register
    private static Retrofit retrofitAuth;   // Dùng cho Cart, Order, Profile

    // 1. Client cho API không cần Token (Login, Register...)
    public static Retrofit getClient(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
        if (retrofitPublic == null) {
            retrofitPublic = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
        return retrofitPublic;
    }

    // 2. Client cho API CẦN Token (Order, Cart...)
    public static Retrofit getClient(Context context) {
        if (retrofitAuth == null) {
            // --- Cấu hình Interceptor ---
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();

                    // Lấy token từ SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
                    String token = prefs.getString("ACCESS_TOKEN", null);

                    // Nếu có token thì gắn vào Header
                    if (token != null) {
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .method(originalRequest.method(), originalRequest.body())
                                .build();
                        return chain.proceed(newRequest);
                    }

                    // Không có token thì cứ gửi đi (phòng trường hợp logout hoặc lỗi)
                    return chain.proceed(originalRequest);
                }
            };

            // --- Cấu hình OkHttp ---
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // --- Tạo Retrofit Auth ---
            retrofitAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Gắn client đã cấu hình
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitAuth;
    }
}