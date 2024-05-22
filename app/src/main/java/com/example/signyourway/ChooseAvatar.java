package com.example.signyourway;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseAvatar extends AppCompatActivity {
    ImageView imageView1;
    ImageView imageView2;
    TextView textView;
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    Button skipButton;
    String avatar = "Alex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_avatar);

        // Initialize ImageView and TextView variables
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        textView = findViewById(R.id.textView);
        textView1 = findViewById(R.id.textView2);
        textView2 = findViewById(R.id.textView3);
        textView3 = findViewById(R.id.textView4);
        skipButton = findViewById(R.id.skipbutton);

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ChooseAvatar.this, MainActivity.class);
                startActivity(intent1); // Start the activity
            }
        });
        imageView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Scale up the image when touched
                        scaleImage(imageView1, 1.2f, 1.2f, 300L);
                        return true; // Consume the touch event
                    case MotionEvent.ACTION_UP:
                        // Scale down the image when touch is released
                        scaleImage(imageView1, 1.0f, 1.0f, 300L);
                        // Start the HelloMia activity
                        Intent intent2 = new Intent(ChooseAvatar.this, HelloAlex.class);
                        intent2.putExtra("avatar", "Alex");
                        startActivity(intent2);
                        return true; // Consume the touch event
                }
                return false;
            }
        });




        imageView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Scale up the image when touched
                        scaleImage(imageView2, 1.2f, 1.2f, 300L);
                        return true; // Consume the touch event
                    case MotionEvent.ACTION_UP:
                        // Scale down the image when touch is released
                        scaleImage(imageView2, 1.0f, 1.0f, 300L);
                        // Start the HelloMia activity
                        Intent intent3 = new Intent(ChooseAvatar.this, HelloMia.class);
                        intent3.putExtra("avatar", "Mia");
                        startActivity(intent3);
                        return true; // Consume the touch event
                }
                return false;
            }
        });

    }

    // Method to scale the image
    private void scaleImage(View view, float startScale, float endScale, long duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true); // Persist the transformation after the animation ends
        view.startAnimation(scaleAnimation);
    }
}
