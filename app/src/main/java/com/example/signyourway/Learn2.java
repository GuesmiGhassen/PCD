package com.example.signyourway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

public class Learn2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn2);

        GridView listView = findViewById(R.id.gridView);
        String[] alphabets = new String[26];
        for (int i = 0; i < 26; i++) {
            alphabets[i] = String.valueOf((char) ('A' + i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alphabets);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Learn2.this, Alphabets.class);
                intent.putExtra("alphabet", alphabets[position]);
                startActivity(intent);
            }
        });
    }
}
