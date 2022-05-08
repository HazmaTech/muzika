package com.example.muzika;

import android.net.Uri;

public class music {
    private String ID;
    private String musicName;
    private String author;
    private String desc;
    private int likes;
    private int listenCount;
    private String musicSourceRes;
    private String musicPicRes;

    public music(){}
    public music(String musicName, String author, String desc, String musicSourceRes, String musicPicRes) {
        this.musicName = musicName;
        this.author = author;
        this.desc = desc;
        this.likes = 0;
        this.listenCount = 0;
        this.musicSourceRes = musicSourceRes;
        this.musicPicRes = musicPicRes;
    }

    public String getMusicName() {
        return musicName;
    }

    public String getDesc() {
        return desc;
    }

    public int getLikes() {
        return likes;
    }

    public String getMusicSourceRes() {
        return musicSourceRes;
    }

    public String getMusicPicRes() {
        return musicPicRes;
    }

    public void setMusicSourceRes(String musicSourceRes) {
        this.musicSourceRes = musicSourceRes;
    }

    public void setMusicPicRes(String musicPicRes) {
        this.musicPicRes = musicPicRes;
    }

    public String getAuthor() {
        return author;
    }

    public int getListenCount() {
        return listenCount;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setListenCount(int listenCount) {
        this.listenCount = listenCount;
    }

    public String _getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}

