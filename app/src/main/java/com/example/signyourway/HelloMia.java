package com.example.signyourway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class HelloMia extends AppCompatActivity {
    TextView texteMia;
    ImageView imageMia;
    Button buttonMia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hello_mia);

        // Correctly initialize the views
        imageMia = findViewById(R.id.miaImg); // Assuming you have an image view for Mia
        texteMia = findViewById(R.id.helloMia); // Assuming you have a text view for Mia
        buttonMia = findViewById(R.id.letsStartMia); // Assuming you have a button for Mia

        // Set the onClickListener for the button to start the main activity
        buttonMia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMia = new Intent(HelloMia.this, MainActivity.class);
                intentMia.putExtra("avatar", "Mia");
                startActivity(intentMia); // Start the main activity
            }
        });
    }
}
