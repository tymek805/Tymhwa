package com.tl.tymhwa.DataManagement;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class ImageProvider extends AsyncTask<Void, Void, Void> {
    private final TextFileHandler textFileHandler;
    private int numberOfThreads = 0;

    public ImageProvider(TextFileHandler textFileHandler){
        this.textFileHandler = textFileHandler;
//        this.textFileHandler.writeFile("Logging 10,000 Years into the Future&&&https://www.asurascans.com/manga/logging-10000-years-into-the-future/&&&5");
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (Item item : textFileHandler.getItems()){
            int chapter = item.getLastChapter();

            for (String url : getChapterList(item.getLink(), chapter)){
                Document page = connectionHandler(url);
                int finalChapter = chapter;

                //todo overflow can occur when huge amount of chapters initiated to download
                new Thread(() -> {
                    numberOfThreads++;
                    Log.i("THREADS", String.valueOf(numberOfThreads));
                    Elements images = page.select("#readerarea").select("img[src~=(?i)\\.(png|jpe?g)]");

                    // Specific to one website
                    //TODO make more flexible "junk" images removal
                    images.remove(images.size() - 2);
                    images.remove(images.size() - 1);
                    images.remove(0);

                    File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Tymhwa/" + item.getTitle() + File.separator + String.format("%03d", finalChapter));
                    if (!dir.exists()) {dir.mkdirs();}
                    if (dir.list() == null || dir.list().length != images.size()) {
                        for (Element image : images) {
                            Log.d("URL", image.attr("abs:src"));
                            File outputFile = new File(dir.getPath() + File.separator + "IMG_" + String.format("%03d", images.indexOf(image)) +".jpg");
                            storeImage(decodeImage(image.attr("abs:src")), outputFile);
                        }
                    } else {Log.d("MESSAGE", "Chapter " + finalChapter + " already downloaded");}
                    numberOfThreads--;
                    Log.e("THREADS", "One thread closed - " + numberOfThreads);
                }).start();
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException ignored){}
                chapter++;
            }
        }
        return null;
    }

    private Bitmap decodeImage(String imageURL){
        try {
            InputStream input = new URL(imageURL).openStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {Log.e("ERROR", e.getMessage());}
        return null;
    }
    private void storeImage(Bitmap image, File pictureFile) {
        if (pictureFile == null || image == null) {
            Log.e("STORAGE", "Error creating media file, check storage permissions: ");
            return;
        }
        Log.d("SAVING", "Saving image as " + pictureFile.getName());
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.d("DOWNLOADING", "Finished and save completed!");
        } catch (FileNotFoundException e) {
            Log.e("STORAGE", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.e("STORAGE", "Error accessing file: " + e.getMessage());
        }
    }
    private ArrayList<String> getChapterList(String url, int lastChapterRead){
        ArrayList<String> chapters = new ArrayList<>();
        Document page = connectionHandler(url);

        if (page != null){
            Elements chaptersURL = page.select("#chapterlist").select("a");
            for (Element chapter : chaptersURL){
                chapters.add(chapter.attr("href"));
            }
        }
        Collections.reverse(chapters);
        //TODO improve exception handling for index > chapters
        if (lastChapterRead <= chapters.size())
            chapters = new ArrayList<>(chapters.subList(Math.max(0, lastChapterRead - 1), chapters.size()));

        return chapters;
    }
    private Document connectionHandler(String url){
        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .ignoreHttpErrors(true);
            return connection.get();
        } catch (IOException e){
            Log.e("CONNECTION", "URL connection failed!");
        }
        return null;
    }
}