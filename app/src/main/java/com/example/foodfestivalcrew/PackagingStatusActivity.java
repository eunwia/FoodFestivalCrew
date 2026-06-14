package com.example.foodfestivalcrew;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.foodfestivalcrew.api.RetrofitClient;
import com.example.foodfestivalcrew.model.StatusMessage;
import com.example.foodfestivalcrew.model.StatusResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackagingStatusActivity extends BaseDrawerActivity {

    private LinearLayout layoutMessages;
    private ScrollView scrollStatus;
    private Button btnRefresh;
    private SessionManager session;
    private final List<Integer> shownIds = new ArrayList<>();
    private boolean isDone = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupDrawer(R.layout.activity_packaging_status);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Packaging Status");

        session = new SessionManager(this);
        layoutMessages = findViewById(R.id.layoutMessages);
        scrollStatus = findViewById(R.id.scrollStatus);
        btnRefresh = findViewById(R.id.btnRefreshStatus);

        btnRefresh.setOnClickListener(v -> {
            shownIds.clear();
            isDone = false;
            layoutMessages.removeAllViews();
            btnRefresh.setVisibility(View.GONE);
            loadStatus();
        });

        loadStatus();
    }

    private void loadStatus() {
        if (isDone) return;

        RetrofitClient.getService().getStatus(session.getToken())
                .enqueue(new Callback<StatusResponse>() {
                    @Override
                    public void onResponse(Call<StatusResponse> call, Response<StatusResponse> res) {
                        if (res.isSuccessful() && res.body() != null) {
                            StatusResponse body = res.body();
                            List<StatusMessage> messages = body.data;

                            if (messages != null) {
                                List<StatusMessage> newMessages = new ArrayList<>();
                                for (StatusMessage msg : messages) {
                                    if (!shownIds.contains(msg.id)) {
                                        newMessages.add(msg);
                                    }
                                }
                                if (!newMessages.isEmpty()) {
                                    displayMessagesOneByOne(newMessages, body.done);
                                } else if (body.done) {
                                    isDone = true;
                                    btnRefresh.setVisibility(View.VISIBLE);
                                } else {
                                    handler.postDelayed(() -> loadStatus(), 2000);
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<StatusResponse> call, Throwable t) {
                        handler.postDelayed(() -> loadStatus(), 3000);
                    }
                });
    }

    private void displayMessagesOneByOne(List<StatusMessage> newMessages, boolean doneAtFetch) {
        if (newMessages.isEmpty()) {
            if (doneAtFetch) {
                isDone = true;
                btnRefresh.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(() -> loadStatus(), 2000);
            }
            return;
        }

        StatusMessage msg = newMessages.remove(0);
        shownIds.add(msg.id);
        addMessageView(msg.message);

        if (msg.message.startsWith("[Alert]")) {
            showAlertDialog(msg.message);
        }

        scrollStatus.post(() -> scrollStatus.fullScroll(View.FOCUS_DOWN));

        handler.postDelayed(() -> displayMessagesOneByOne(newMessages, doneAtFetch), 1000);
    }

    private void addMessageView(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setPadding(16, 12, 16, 12);
        tv.setTextSize(14f);
        if (message.startsWith("[Alert]")) {
            tv.setBackgroundColor(Color.parseColor("#FFEBEE"));
            tv.setTextColor(Color.parseColor("#C62828"));
        } else {
            tv.setBackgroundColor(Color.parseColor("#F5F5F5"));
            tv.setTextColor(Color.BLACK);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        tv.setLayoutParams(params);
        layoutMessages.addView(tv);
    }

    private void showAlertDialog(String message) {
        runOnUiThread(() ->
                new AlertDialog.Builder(this)
                        .setTitle("⚠ Alert")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}