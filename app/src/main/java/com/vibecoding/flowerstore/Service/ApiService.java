package com.vibecoding.flowerstore.Service;

import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.Product; // Cần import lớp Product
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path; // Cần import lớp Path

public interface ApiService {

    // Giữ lại phương thức cũ để lấy tất cả sản phẩm
    @GET("api/v1/products")
    Call<ApiResponse> getProducts();

    // --- THÊM PHƯƠNG THỨC MỚI ---
    // Phương thức này sẽ lấy một sản phẩm dựa trên ID
    // URL sẽ được tạo thành: "api/v1/products/1", "api/v1/products/2",...
    @GET("api/v1/products/{id}")
    Call<Product> getProductById(@Path("id") int productId);
}
