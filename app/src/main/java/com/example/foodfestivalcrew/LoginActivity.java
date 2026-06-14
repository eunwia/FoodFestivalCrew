package com.example.foodfestivalcrew;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodfestivalcrew.api.ApiService;
import com.example.foodfestivalcrew.api.RetrofitClient;
import com.example.foodfestivalcrew.model.LoginRequest;
import com.example.foodfestivalcrew.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (!session.getToken().isEmpty()) {
            startActivity(new Intent(this, FoodOfDayActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        Button btnLogin  = findViewById(R.id.btnLogin);
        TextView tvResult = findViewById(R.id.tvResult);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                tvResult.setText("Please enter your email.");
                return;
            }
            btnLogin.setEnabled(false);
            tvResult.setText("Checking...");

            ApiService api = RetrofitClient.getService();
            api.login(new LoginRequest(email)).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    btnLogin.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse body = response.body();
                        // Show dialog with result
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Login Result")
                                .setMessage(body.result)
                                .setPositiveButton("OK", (d, w) -> {
                                    if ("Authorized Credential".equals(body.result)) {
                                        new SessionManager(LoginActivity.this)
                                                .save(body.token != null ? body.token : "", body.email);
                                        startActivity(new Intent(LoginActivity.this, FoodOfDayActivity.class));
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        tvResult.setText("Server error. Check if CrewAPI is running.");
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    tvResult.setText("Connection failed: " + t.getMessage());
                }
            });
        });
    }
}