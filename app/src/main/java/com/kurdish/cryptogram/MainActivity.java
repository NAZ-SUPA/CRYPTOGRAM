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

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    private void colorLetters() {
        TextView tv = findViewById(R.id.cryptogram);
        String text = tv.getText().toString(); // gets "CRYPTOGRAM" from XML
        TextView tv2 = findViewById(R.id.tv_cipher_numbers);
        String cipher = tv2.getText().toString();

        SpannableString spannable = new SpannableString(text);

        // Color index 1 (R) and index 8 (G) to yellow
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 7, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        tv.setText(spannable);

        spannable = new SpannableString(cipher);

        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 14, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv2.setText(spannable);
    }
}