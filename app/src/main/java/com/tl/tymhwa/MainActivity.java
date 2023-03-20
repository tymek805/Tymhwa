package com.tl.tymhwa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.tl.tymhwa.DataManagement.DatabaseManager;
import com.tl.tymhwa.DataManagement.ImageProvider;
import com.tl.tymhwa.DataManagement.Item;
import com.tl.tymhwa.DataManagement.TextFileHandler;
import com.tl.tymhwa.UI.GalleryFragment;
import com.tl.tymhwa.UI.HomeFragment;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageProvider imageProvider;
    private Menu menu;
    private TextFileHandler textFileHandler;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbManager = new DatabaseManager(this);
        textFileHandler = new TextFileHandler(this);
        imageProvider = new ImageProvider(textFileHandler);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

        NavController navController = navHostFragment.getNavController();
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
            itemManagement(menuItem, 0, -1);
            return true;
        });
        menu.getItem(1).setOnMenuItemClickListener(menuItem -> {
            itemManagement(menuItem, 1, -1);
            return true;
        });
        menu.getItem(2).setOnMenuItemClickListener(menuItem -> {
            itemManagement(menuItem, 2, -1);
            return true;
        });
    }
    private void addExistingMenuItems(){
        for (Item item : dbManager.readItems()){
            menu.add(R.id.allGroup, 30, Menu.NONE, item.getTitle())
                    .setIcon(R.drawable.nav_nothing_out)
                    .setCheckable(true)
                    .setOnMenuItemClickListener(menuItem -> {
                        itemManagement(menuItem, 4, Math.max(item.getLastChapter(), 1));
                        return true;
                    });
        }
    }

    private void itemManagement(MenuItem item, int group, int chapter){
        getSupportActionBar().setTitle(item.getTitle());
        if (group == 0){
            HomeFragment newFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commit();
        } else if (group == 1){showAddNavigationItemDialog();
        } else if (group == 2){
            GalleryFragment newFragment = new GalleryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commit();
        } else if (group == 4){
            GalleryFragment newFragment = new GalleryFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, String.valueOf(item.getTitle()))
                    .commitNow();

            adjustingResources(item.getTitle().toString(), chapter);
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
        preparingNavButtons(title, chapter);
    }

    private void preparingNavButtons(String title, int chapter){
        int[] id = {R.id.nextBtnTop, R.id.nextBtnBot, R.id.prevBtnTop, R.id.prevBtnBot};
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Tymhwa/" + title);

        for (int i = 0; i < id.length; i++){
            if (i < 2){
                Button nextBtn = findViewById(id[i]);
                nextBtn.setOnClickListener(view -> {
                    if (dir.listFiles().length > chapter) {
                        GalleryFragment newFragment = new GalleryFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, newFragment, title)
                                .commitNow();

                        adjustingResources(title, chapter + 1);
                        textFileHandler.updateChapter(title, chapter + 1);
                    }
                });
            } else{
                Button prevBtn = findViewById(id[i]);
                prevBtn.setOnClickListener(view -> {
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

    private void showAddNavigationItemDialog() {
        final EditText titleInput = new EditText(this);
        final EditText linkInput = new EditText(this);
        final EditText chapterInput = new EditText(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(32, 16, 32, 16);

        titleInput.setLayoutParams(layoutParams);
        titleInput.setHint("Title");
        linkInput.setLayoutParams(layoutParams);
        linkInput.setHint("Link");
        chapterInput.setLayoutParams(layoutParams);
        chapterInput.setHint("Chapter");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(titleInput);
        layout.addView(linkInput);
        layout.addView(chapterInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setMessage("Enter the title, link and optionally last chapter read.")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString();
                    String link = linkInput.getText().toString();
                    String lastChapterRead = chapterInput.getText().toString();
                    if (lastChapterRead.equals("")) lastChapterRead = "0";
                    String joinedData = title + "&&&" + link + "&&&" + lastChapterRead;
                    textFileHandler.updateFile(joinedData);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }
}