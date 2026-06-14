package com.example.foodfestivalcrew;

import android.os.Bundle;
import android.widget.TextView;
import com.example.foodfestivalcrew.R;

public class PackagingInfoActivity extends BaseDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupDrawer(R.layout.activity_packaging_info);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Packaging Information");

        TextView tvExplanation = findViewById(R.id.tvPackagingExplanation);
        tvExplanation.setText(
                "Multi-Process Packaging is a strategy used to package ingredients by pairing any two adjacent ingredients until all ingredients are packaged into a food container.\n\n" +
                "Each food ingredient is labelled with a Prepare time(minute), Scoop (number of serving produced after the prepare time), and the ingredients' stall position.\n\n" +
                "Rules:\n" +
                "• Ingredients are represented as A1 (Ingredient A at position 1).\n" +
                "• Adjacent ingredients of A is {B} only; but Adjacent ingredients of B is {A, C}.\n" +
                "• A1 and B2 can be packaged together resulting in (A1 X B2).\n" +
                "• Switching positions is STRICTLY PROHIBITED. (e.g., A1 and B2 cannot be packaged as (B2 X A1)).\n\n" +
                "Formula for (A X B):\n" +
                "• Packaging time = (A_time * B_scoop) + (B_time * A_scoop)\n" +
                "• Time = A_time\n" +
                "• Scoop = B_scoop\n\n" +
                "Total Packaging time of a packaging option = sum of the packing time from the steps of combination all ingredients in a container."
        );
    }
}