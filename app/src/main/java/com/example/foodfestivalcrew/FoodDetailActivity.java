package com.example.foodfestivalcrew;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.foodfestivalcrew.api.RetrofitClient;
import com.example.foodfestivalcrew.model.Ingredient;
import com.example.foodfestivalcrew.model.IngredientResponse;
import com.example.foodfestivalcrew.model.PackagingResponse;
import com.example.foodfestivalcrew.model.PackagingStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailActivity extends AppCompatActivity {

    private SessionManager session;
    private int foodId;
    private String foodDesc;
    private List<Ingredient> ingredients = new ArrayList<>();
    private List<String> packagingOptions = new ArrayList<>();
    private List<PackagingResponse> loadedPackaging = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        session = new SessionManager(this);
        foodId = getIntent().getIntExtra("food_id", 0);
        String foodName  = getIntent().getStringExtra("food_name");
        String foodDate  = getIntent().getStringExtra("food_date");
        foodDesc = getIntent().getStringExtra("food_desc");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Food " + foodId);
        }

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        tvTitle.setText(foodName + " on\n" + foodDate);

        ImageButton btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> loadAndShowIngredients());

        loadPackagingOptions();
    }

    private void loadAndShowIngredients() {
        RetrofitClient.getService().getIngredients(session.getToken(), foodId)
                .enqueue(new Callback<IngredientResponse>() {
                    @Override
                    public void onResponse(Call<IngredientResponse> call, Response<IngredientResponse> res) {
                        if (res.isSuccessful() && res.body() != null && res.body().data != null && !res.body().data.isEmpty()) {
                            ingredients = res.body().data;
                            ingredients.sort((a, b) -> Integer.compare(a.position, b.position));
                            showIngredientDialog();
                        } else {
                            useFallbackIngredients();
                        }
                    }
                    @Override
                    public void onFailure(Call<IngredientResponse> call, Throwable t) {
                        useFallbackIngredients();
                    }
                });
    }

    private void useFallbackIngredients() {
        populateFallbackIngredients();
        showIngredientDialog();
    }

    private void populateFallbackIngredients() {
        ingredients.clear();
        // Diversify ingredients based on foodId to provide variety
        int set = foodId % 3;
        if (set == 0) {
            // Set 1: Asian Fusion
            ingredients.add(createDemoIngredient("Grilled Chicken", "GC", 1, 2, 5));
            ingredients.add(createDemoIngredient("Steamed Rice", "SR", 2, 3, 10));
            ingredients.add(createDemoIngredient("Fresh Salad", "FS", 3, 1, 8));
            ingredients.add(createDemoIngredient("Special Sauce", "SS", 4, 1, 2));
        } else if (set == 1) {
            // Set 2: Italian Feast
            ingredients.add(createDemoIngredient("Pasta", "PA", 1, 4, 6));
            ingredients.add(createDemoIngredient("Tomato Sauce", "TS", 2, 2, 12));
            ingredients.add(createDemoIngredient("Meatballs", "MB", 3, 5, 4));
            ingredients.add(createDemoIngredient("Parmesan", "PR", 4, 1, 15));
        } else {
            // Set 3: BBQ Party
            ingredients.add(createDemoIngredient("Beef Brisket", "BB", 1, 6, 4));
            ingredients.add(createDemoIngredient("Cornbread", "CB", 2, 2, 8));
            ingredients.add(createDemoIngredient("Coleslaw", "CS", 3, 1, 10));
            ingredients.add(createDemoIngredient("BBQ Glaze", "BG", 4, 1, 5));
        }
    }

    private void showIngredientDialog() {
        StringBuilder sb = new StringBuilder();
        if (foodDesc != null && !foodDesc.isEmpty()) {
            sb.append("Description:\n").append(foodDesc).append("\n\n");
        }
        sb.append("Main Ingredients:\n");
        for (Ingredient i : ingredients) {
            sb.append("• ").append(i.name).append(" (").append(i.code).append(")")
                    .append(" — Position: ").append(i.position)
                    .append("\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Food Details")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadPackagingOptions() {
        RetrofitClient.getService().getIngredients(session.getToken(), foodId)
                .enqueue(new Callback<IngredientResponse>() {
                    @Override
                    public void onResponse(Call<IngredientResponse> call, Response<IngredientResponse> res) {
                        if (res.isSuccessful() && res.body() != null && res.body().data != null && !res.body().data.isEmpty()) {
                            ingredients = res.body().data;
                            ingredients.sort((a, b) -> Integer.compare(a.position, b.position));
                        } else {
                            // Use consistent diversified fallback
                            populateFallbackIngredients();
                        }
                        calculateAndDisplayPackagingLocally();
                    }
                    @Override
                    public void onFailure(Call<IngredientResponse> call, Throwable t) {
                        // Use consistent diversified fallback
                        populateFallbackIngredients();
                        calculateAndDisplayPackagingLocally();
                    }
                });
    }

    private void calculateAndDisplayPackagingLocally() {
        loadedPackaging.clear();
        List<LocalResult> results = getAllPossibleCombinations(ingredients, 0, ingredients.size() - 1);
        
        for (LocalResult res : results) {
            PackagingResponse pr = new PackagingResponse();
            pr.option = res.label;
            pr.total_time = res.totalPackagingTime;
            pr.steps = res.steps;
            loadedPackaging.add(pr);
        }

        Collections.sort(loadedPackaging, (a, b) -> Double.compare(a.total_time, b.total_time));
        displayPackagingOptions();
    }

    private List<LocalResult> getAllPossibleCombinations(List<Ingredient> ings, int start, int end) {
        List<LocalResult> list = new ArrayList<>();
        if (start == end) {
            Ingredient ing = ings.get(start);
            LocalResult res = new LocalResult();
            res.label = ing.code + ing.position;
            res.time = ing.prepare_time;
            res.scoop = ing.scoop;
            res.totalPackagingTime = 0;
            res.steps = new ArrayList<>();
            list.add(res);
            return list;
        }

        for (int split = start; split < end; split++) {
            List<LocalResult> lefts = getAllPossibleCombinations(ings, start, split);
            List<LocalResult> rights = getAllPossibleCombinations(ings, split + 1, end);
            for (LocalResult L : lefts) {
                for (LocalResult R : rights) {
                    LocalResult combined = new LocalResult();
                    combined.label = "(" + L.label + " X " + R.label + ")";
                    
                    // Formula: (A X B) packaging_time = (A_time * B_scoop) + (B_time * A_scoop)
                    double stepTime = (L.time * R.scoop) + (R.time * L.scoop);
                    
                    // (A X B) time = A_time
                    combined.time = L.time;
                    
                    // (A X B) scoop = B_scoop
                    combined.scoop = R.scoop;
                    
                    combined.totalPackagingTime = L.totalPackagingTime + R.totalPackagingTime + stepTime;
                    
                    combined.steps = new ArrayList<>();
                    combined.steps.addAll(L.steps);
                    combined.steps.addAll(R.steps);
                    
                    PackagingStep step = new PackagingStep();
                    step.step = combined.steps.size() + 1;
                    step.combination = combined.label;
                    step.packaging_time = stepTime;
                    combined.steps.add(step);
                    
                    list.add(combined);
                }
            }
        }
        return list;
    }

    private static class LocalResult {
        String label;
        double time;
        double scoop;
        double totalPackagingTime;
        List<PackagingStep> steps;
    }

    private Ingredient createDemoIngredient(String name, String code, int pos, int time, int scoop) {
        Ingredient i = new Ingredient();
        i.name = name;
        i.code = code;
        i.position = pos;
        i.prepare_time = time;
        i.scoop = scoop;
        return i;
    }

    private void displayPackagingOptions() {
        LinearLayout layoutOptions = findViewById(R.id.layoutPackagingOptions);
        TextView tvCount = findViewById(R.id.tvTotalCount);
        tvCount.setText("Total Count: " + loadedPackaging.size());
        layoutOptions.removeAllViews();

        for (PackagingResponse pkg : loadedPackaging) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(0, 12, 0, 12);
            itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
            itemLayout.setClickable(true);
            itemLayout.setFocusable(true);

            TextView tvOption = new TextView(this);
            tvOption.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvOption.setText(pkg.option);
            tvOption.setTextSize(14f);

            TextView tvTime = new TextView(this);
            tvTime.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvTime.setText(String.format("%.3f min", pkg.total_time));
            tvTime.setTextSize(14f);

            itemLayout.addView(tvOption);
            itemLayout.addView(tvTime);
            layoutOptions.addView(itemLayout);

            itemLayout.setOnClickListener(v -> showPackagingStepsDialog(pkg));
        }
    }

    private void showPackagingStepsDialog(PackagingResponse pkg) {
        StringBuilder sb = new StringBuilder();
        if (pkg.steps != null) {
            for (PackagingStep s : pkg.steps) {
                sb.append("Step ").append(s.step).append(": ")
                        .append(s.combination)
                        .append(String.format(" — %.3f min\n", s.packaging_time));
            }
        }
        sb.append(String.format("\nTotal: %.3f min", pkg.total_time));
        new AlertDialog.Builder(this)
                .setTitle("Packaging: " + pkg.option)
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}