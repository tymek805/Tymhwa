package com.tl.tymhwa.DataManagement;

public class Item {
    private final String title;
    private final String link;
    private final int lastChapter; // Min val: 0

    public Item (String source){
        String[] var = source.split("&&&");
        title = var[0];
        link = var[1];
        lastChapter = Integer.parseInt(var[2]);
    }
    public Item (String title, String link, int lastChapter){
        this.title = title;
        this.link = link;
        this.lastChapter = lastChapter;
    }

    public String getTitle() {return title;}
    public String getLink() {return link;}
    public int getLastChapter() {return lastChapter;}
}
