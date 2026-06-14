package com.example.foodfestivalcrew;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.foodfestivalcrew.api.RetrofitClient;
import com.example.foodfestivalcrew.model.FoodItem;
import com.example.foodfestivalcrew.model.FoodListResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodOfDayActivity extends BaseDrawerActivity {

    private TextView tvMonthYear;
    private ListView listFood;
    private SwipeRefreshLayout swipeRefresh;
    private int currentMonth, currentYear;
    private List<FoodItem> foodItems = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupDrawer(R.layout.activity_food_of_day);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Food of Day");

        session = new SessionManager(this);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        listFood = findViewById(R.id.listFood);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentYear  = cal.get(Calendar.YEAR);

        updateMonthLabel();
        loadFood();

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 1) { currentMonth = 12; currentYear--; }
            updateMonthLabel(); loadFood();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 12) { currentMonth = 1; currentYear++; }
            updateMonthLabel(); loadFood();
        });

        swipeRefresh.setOnRefreshListener(this::loadFood);

        listFood.setOnItemClickListener((parent, view, position, id) -> {
            FoodItem item = foodItems.get(position);
            Intent intent = new Intent(this, FoodDetailActivity.class);
            intent.putExtra("food_id", item.id);
            intent.putExtra("food_name", item.name);
            intent.putExtra("food_date", item.date);
            intent.putExtra("food_theme", item.theme);
            intent.putExtra("food_desc", item.description);
            startActivity(intent);
        });
    }

    private void updateMonthLabel() {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"};
        tvMonthYear.setText(months[currentMonth - 1] + " " + currentYear);
    }

    private void loadFood() {
        swipeRefresh.setRefreshing(true);
        RetrofitClient.getService().getFood(session.getToken(), currentMonth, currentYear)
                .enqueue(new Callback<FoodListResponse>() {
                    @Override
                    public void onResponse(Call<FoodListResponse> call, Response<FoodListResponse> res) {
                        swipeRefresh.setRefreshing(false);
                        if (res.isSuccessful() && res.body() != null && res.body().data != null) {
                            foodItems = res.body().data;
                            setupAdapter();
                        }
                    }
                    @Override
                    public void onFailure(Call<FoodListResponse> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void setupAdapter() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ArrayAdapter<FoodItem> adapter = new ArrayAdapter<FoodItem>(this,
                R.layout.item_food, foodItems) {
            @NonNull @Override
            public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.item_food, parent, false);

                FoodItem item = foodItems.get(pos);
                TextView tvName = convertView.findViewById(R.id.tvFoodName);
                TextView tvDate = convertView.findViewById(R.id.tvFoodDate);
                ImageView ivCal = convertView.findViewById(R.id.ivCalIcon);

                tvName.setText(item.name);
                tvDate.setText(item.date);

                // Grey out past foods
                boolean isPast = item.date != null && item.date.compareTo(today) < 0;
                tvName.setTextColor(isPast ? Color.GRAY : Color.BLACK);
                tvDate.setTextColor(isPast ? Color.LTGRAY : Color.parseColor("#888888"));

                // Calendar icon color based on theme
                int bgColor = themeToColor(item.theme);
                ivCal.setBackgroundColor(bgColor);

                return convertView;
            }
        };
        listFood.setAdapter(adapter);
    }

    private int themeToColor(String theme) {
        if (theme == null) return Color.parseColor("#FFCCBC");
        switch (theme.toLowerCase()) {
            case "red":    return Color.parseColor("#FFCDD2");
            case "blue":   return Color.parseColor("#BBDEFB");
            case "green":  return Color.parseColor("#C8E6C9");
            case "yellow": return Color.parseColor("#FFF9C4");
            case "orange": return Color.parseColor("#FFE0B2");
            case "purple": return Color.parseColor("#E1BEE7");
            default:       return Color.parseColor("#F5F5F5");
        }
    }
}