package com.tl.tymhwa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;


public class LoadingResources extends AsyncTask<Void, Void, Void> {
    private final ArrayList<ImageView> images;
    private final LinearLayout linearLayout;
    private final String title;
    private final int chapter;

    public LoadingResources(ArrayList<ImageView> images, LinearLayout linearLayout, String title, int chapter){
        this.images = images;
        this.linearLayout = linearLayout;
        this.title = title;
        this.chapter = chapter;
    }

    @Override
    protected Void doInBackground(Void... params) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Tymhwa/" + title + File.separator + String.format("%03d", chapter));
        int num = 0;
        for (File page : dir.listFiles()){
            Log.e("PAGES", page.getPath());

            Bitmap image = BitmapFactory.decodeFile(page.getPath());
            ImageView imageView = images.get(num);
            imageView.setImageBitmap(image);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imageView.setLayoutParams(layoutParams);
            num++;
        }

        return null;
    }
}
