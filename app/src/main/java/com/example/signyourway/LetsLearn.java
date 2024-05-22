package com.example.signyourway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LetsLearn extends AppCompatActivity {
    private Button moveToPlay;
    private Button moveToAlphabet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_letslearn);
        moveToAlphabet = findViewById(R.id.buttonx1);
        moveToPlay = findViewById(R.id.buttonx2);

        moveToAlphabet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LetsLearn.this, Learn2.class);
                startActivity(intent);
            }
        });

        moveToPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LetsLearn.this, Learning.class);
                startActivity(intent); // Start the activity
            }
        });

    }
}
