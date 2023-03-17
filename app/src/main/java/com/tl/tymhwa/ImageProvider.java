package com.tl.tymhwa;

import android.content.Context;
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
    private final Context context;
    int numberOfThreads = 0;

    public ImageProvider(Context context){this.context = context; updateFile("Logging 10,000 Years into the Future&&&https://www.asurascans.com/manga/logging-10000-years-into-the-future/");}

    @Override
    protected Void doInBackground(Void... params) {
        for (String source : readURL()){
            String[] var = source.split("&&&");

            String title = var[0];
            source = var[1];
            int chapter = 1;

            Log.d("SCRAPING", source);

            /*
            Alternative version of connection without using JSOUP
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.connect();
            */

            for (String url : getChapterList(source)){
                Document page = connectionHandler(url);
                //todo możliwe przepełnienie threadów przy dużym nakładzie chapterów
                try {
                    int finalChapter = chapter;
                    new Thread(() -> {
                        numberOfThreads++;
                        Log.i("THREADS", String.valueOf(numberOfThreads));
                        Elements images = page.select("#readerarea").select("img[src~=(?i)\\.(png|jpe?g)]");
                        images.first().remove();
                        images.last().remove();

                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Tymhwa/" + title + File.separator + String.format("%03d", finalChapter));
                        if (!dir.exists()) {dir.mkdirs();}
                        if (dir.list().length != images.size()) {
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
                } catch (NullPointerException e){Log.e("ERROR", e.getMessage());}
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
    private ArrayList<String> readURL(){
        ArrayList<String> urls = new ArrayList<>();
        try {
            InputStream inputStream = context.openFileInput("data.txt");
//            InputStream inputStream = context.getResources().openRawResource(R.raw.urls);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null){
                urls.add(line);
                line = reader.readLine();
            }
        } catch (Exception ignored) {}
        return urls;
    }

    //TODO Set file handler
    private void updateFile (String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("data.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private ArrayList<String> getChapterList(String url){
        ArrayList<String> chapters = new ArrayList<>();
        Document page = connectionHandler(url);

        if (page != null){
            Elements chaptersURL = page.select("#chapterlist").select("a");
            for (Element chapter : chaptersURL){
                chapters.add(chapter.attr("href"));
            }
        }
        Collections.reverse(chapters);
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