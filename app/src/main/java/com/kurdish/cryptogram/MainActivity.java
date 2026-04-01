package com.kurdish.cryptogram;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the screen layout and then apply the custom text styling.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        colorLetters();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // comment by copilot
    }

    // Highlight the matching characters in the cryptogram and cipher text.
    private void colorLetters() {
        TextView tv = findViewById(R.id.cryptogram);
        String text = tv.getText().toString(); // Reads "CRYPTOGRAM" from strings.xml.
        TextView tv2 = findViewById(R.id.tv_cipher_numbers);
        String cipher = tv2.getText().toString();

        SpannableString spannable = new SpannableString(text);

        // Keep the two R letters black so they stand out from the purple title.
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 7, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        tv.setText(spannable);

        spannable = new SpannableString(cipher);

        // Match the same black emphasis for the corresponding cipher positions.
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 14, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv2.setText(spannable);
    }
}