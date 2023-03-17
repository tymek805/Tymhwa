package com.tl.tymhwa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavController navController;
    private ImageProvider imageProvider;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

        navController = navHostFragment.getNavController();
        menu = navView.getMenu();
        preparingMenu();

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery)
                        .setOpenableLayout(drawerLayout)
                        .build();

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void preparingMenu(){
        addExistingMenuItems();

        menu.getItem(0).setOnMenuItemClickListener(menuItem -> {
            itemManagement(menuItem, 0);
            return true;
        });
        menu.getItem(1).setOnMenuItemClickListener(menuItem -> {
            itemManagement(menuItem, 1);
            return true;
        });
        menu.getItem(2).setOnMenuItemClickListener(menuItem -> {
            itemManagement(menuItem, 2);
            return true;
        });

        imageProvider = new ImageProvider(getApplicationContext());
//        ImageProvider imageProvider = new ImageProvider(getApplicationContext());
//        imageProvider.execute();
    }
    private void addExistingMenuItems(){
       for (String name : getAllNames()) {
           menu.add(R.id.allGroup, 30, Menu.NONE, name)
                   .setIcon(R.drawable.nav_nothing_out)
                   .setCheckable(true)
                   .setOnMenuItemClickListener(menuItem -> {
                    itemManagement(menuItem, 4);
                    return true;
           });
       }
    }
    private ArrayList<String> getAllNames(){
        ArrayList<String> allNames = new ArrayList<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.all_titles);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine().split("&&&")[0];
            while (line != null){
                allNames.add(line);
                line = reader.readLine();
            }
        } catch (Exception ignored) {}

        return allNames;
    }
    private void itemManagement(MenuItem item, int group){
        getSupportActionBar().setTitle(item.getTitle());
        if (group == 0){
            HomeFragment newFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commit();
        } else if (group == 1){
            HomeFragment newFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commit();
        }else if (group == 2){
            GalleryFragment newFragment = new GalleryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commit();
        }else if (group == 4){
            GalleryFragment newFragment = new GalleryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commitNow();

            adjustingResources(String.valueOf(item.getTitle()), 1);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }
    private void adjustingResources(String title, int chapter){
        LinearLayout linearLayout = findViewById(R.id.gallery);
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Tymhwa/" + title + File.separator + String.format("%03d", chapter));
        File[] files = dir.listFiles();
        Bitmap[] images = new Bitmap[files.length];

        AtomicInteger numberOfThreads = new AtomicInteger();
        for (int i = 0; i < files.length; i++){
            final int idx = i;
            new Thread(() -> {
                images[idx] = BitmapFactory.decodeFile(files[idx].getPath());
                Log.wtf("", "Index number -> " + idx);
                numberOfThreads.getAndDecrement();
            }).start();
            numberOfThreads.getAndIncrement();
        }
        long startTime = System.currentTimeMillis();
        while (numberOfThreads.get() > 0){}

        for (int i = 0; i < images.length; i++){
            ImageView imageView = new ImageView(linearLayout.getContext());
            imageView.setImageBitmap(images[i]);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(params);
            linearLayout.addView(imageView);
        }
        long endTime = System.currentTimeMillis();
        Log.i("TIME", "Elapsed time: " + (endTime - startTime) + " milliseconds");
        preparingButtons(title, chapter);
    }

    private void preparingButtons(String title, int chapter){
        int[] id = {R.id.nextBtnTop, R.id.nextBtnBot, R.id.prevBtnTop, R.id.prevBtnBot};
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Tymhwa/" + title);

        for (int i = 0; i < id.length; i++){
            if (i < 2){
                Button nextBtn = findViewById(id[i]);
                nextBtn.setOnClickListener(view -> {
                    if (dir.listFiles().length > chapter) {
                        Log.wtf("BUTTON", "NEXT");
                        GalleryFragment newFragment = new GalleryFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, newFragment, title)
                                .commitNow();

                        adjustingResources(title, chapter + 1);
                    }
                });
            } else{
                Button prevBtn = findViewById(id[i]);
                prevBtn.setOnClickListener(view -> {
                    Log.wtf("BUTTON", "PREV");
                    if (chapter - 1 > 0){
                        GalleryFragment newFragment = new GalleryFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, newFragment, title)
                                .commitNow();

                        adjustingResources(title, chapter - 1);
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_settings, menu);
        menu.getItem(0).setOnMenuItemClickListener(menuItem -> {
            imageProvider.execute();
//            imageProvider.cancel();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }
}