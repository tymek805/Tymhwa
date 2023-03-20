package com.tl.tymhwa.DataManagement;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;

public class TextFileHandler {
    private final Context context;
    private final ArrayList<Item> itemArrayList = new ArrayList<>();

    public TextFileHandler(Context context){
        this.context = context;
        readFile();
    }

    public ArrayList<String> getNames(){
        ArrayList<String> names = new ArrayList<>();
        for (Item item : itemArrayList)
            names.add(item.getTitle());

        return names;
    }
    public ArrayList<String> getLinks(){
        ArrayList<String> names = new ArrayList<>();
        for (Item item : itemArrayList)
            names.add(item.getLink());

        return names;
    }
    public ArrayList<Integer> getLastChapter(){
        ArrayList<Integer> names = new ArrayList<>();
        for (Item item : itemArrayList)
            names.add(item.getLastChapter());

        return names;
    }
    public ArrayList<Item> getItems() {return itemArrayList;}
    public void updateChapter(String title, int chapter){
        for (Item item : itemArrayList){
            if (item.getTitle().equals(title)){
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    InputStream inputStream = context.openFileInput("data.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = reader.readLine();
                    while (line != null){
                        if (line.contains(title)){line = line.replace(line.split("&&&")[2], String.valueOf(chapter));}
                        stringBuffer.append(line);
                        stringBuffer.append("\n");
                        line = reader.readLine();
                    }
                    inputStream.close();
                    String inputString = stringBuffer.toString();

                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("data.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write(inputString);
                    outputStreamWriter.close();
                } catch (Exception ignored) {}
            }
        }
    }
    private void readFile(){
        try {
            InputStream inputStream = context.openFileInput("data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null){
                itemArrayList.add(new Item(line));
                line = reader.readLine();
            }
        } catch (Exception ignored) {}
    }
    public void writeFile(String data){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("data.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            Log.d("File", "Success in writing " + data + " into the data.txt file!");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }
    }
    public void updateFile(String data){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("data.txt", Context.MODE_APPEND));
            outputStreamWriter.append(data);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            Log.d("File", "Success in appending " + data + " into the data.txt file!");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }
    }
}
