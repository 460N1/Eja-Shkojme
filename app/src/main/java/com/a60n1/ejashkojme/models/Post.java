package com.a60n1.ejashkojme.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String title;
    public String body;
    public String date;
    public String time;
    public String origin;
    public String destination;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body, String date, String time, String origin, String destination) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.date = date;
        this.time = time;
        this.origin = origin;
        this.destination = destination;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("date", date);
        result.put("time", time);
        result.put("origin", origin);
        result.put("destination", destination);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }

}