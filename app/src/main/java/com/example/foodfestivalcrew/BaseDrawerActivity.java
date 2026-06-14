package com.example.foodfestivalcrew;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import com.example.foodfestivalcrew.api.RetrofitClient;
import com.example.foodfestivalcrew.model.TokenResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;

    protected void setupDrawer(int layoutResId) {
        setContentView(layoutResId);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        if (navView != null) {
            navView.setNavigationItemSelectedListener(this);
        }

        TextView tvFooterToken = findViewById(R.id.tvFooterToken);
        if (tvFooterToken != null) {
            RetrofitClient.getService().getToken().enqueue(new Callback<TokenResponse>() {
                @Override
                public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvFooterToken.setText("Remember Token: " + response.body().token);
                    }
                }
                @Override
                public void onFailure(Call<TokenResponse> call, Throwable t) {
                    tvFooterToken.setText("Token: Error");
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_food) {
            startActivity(new Intent(this, FoodOfDayActivity.class));
        } else if (id == R.id.nav_status) {
            startActivity(new Intent(this, PackagingStatusActivity.class));
        } else if (id == R.id.nav_info) {
            startActivity(new Intent(this, PackagingInfoActivity.class));
        } else if (id == R.id.nav_logout) {
            new SessionManager(this).clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}