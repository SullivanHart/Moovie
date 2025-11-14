package com.moovie;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// THIS IS THE NEW, SIMPLE MAINACTIVITY
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // This links to your NEW layout file: activity_main.xml
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        // This line connects the nav bar to the navigation graph
        NavigationUI.setupWithNavController(navView, navController);
    }
}
