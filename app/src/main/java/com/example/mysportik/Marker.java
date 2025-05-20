package com.example.mysportik;

public class Marker {
    private String id;
    public double lat;
    public double lon;
    public String name;
    private String note;
    private String status; // "public" или "private"
    private String userId;
    private long timestamp;

//
    public Marker() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return lat; }
    public double getLongitude() { return lon; }
    public String getNote() { return note; }
    public String getStatus() { return status; }
}
