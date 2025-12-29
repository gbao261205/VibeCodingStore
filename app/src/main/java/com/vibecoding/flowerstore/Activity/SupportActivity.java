package com.vibecoding.flowerstore.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vibecoding.flowerstore.R;

public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Trợ giúp & Hỗ trợ");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup expandable items
        setupExpandableItem(R.id.layout_q1, R.id.tv_answer1);
        setupExpandableItem(R.id.layout_q2, R.id.tv_answer2);
        setupExpandableItem(R.id.layout_q3, R.id.tv_answer3);
        setupExpandableItem(R.id.layout_q4, R.id.tv_answer4);
        setupExpandableItem(R.id.layout_q5, R.id.tv_answer5);
        setupExpandableItem(R.id.layout_q6, R.id.tv_answer6);
        setupExpandableItem(R.id.layout_q7, R.id.tv_answer7);
        setupExpandableItem(R.id.layout_q8, R.id.tv_answer8);
        setupExpandableItem(R.id.layout_q9, R.id.tv_answer9);
        setupExpandableItem(R.id.layout_q10, R.id.tv_answer10);
        setupExpandableItem(R.id.layout_q11, R.id.tv_answer11);
        setupExpandableItem(R.id.layout_q12, R.id.tv_answer12);
        setupExpandableItem(R.id.layout_q13, R.id.tv_answer13);
        setupExpandableItem(R.id.layout_q14, R.id.tv_answer14);
        setupExpandableItem(R.id.layout_q15, R.id.tv_answer15);
    }

    private void setupExpandableItem(int layoutId, int answerTvId) {
        LinearLayout layout = findViewById(layoutId);
        TextView answerTv = findViewById(answerTvId);

        if (layout != null && answerTv != null) {
            layout.setOnClickListener(v -> {
                if (answerTv.getVisibility() == View.GONE) {
                    answerTv.setVisibility(View.VISIBLE);
                } else {
                    answerTv.setVisibility(View.GONE);
                }
            });
        }
    }
}
