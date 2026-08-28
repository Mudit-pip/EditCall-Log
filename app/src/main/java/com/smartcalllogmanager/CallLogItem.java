package com.smartcalllogmanager;

public class CallLogItem {
    private long id;
    private String number;
    private String name;
    private int type;
    private long date;
    private int duration;

    public CallLogItem(long id, String number, String name, int type, long date, int duration) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.type = type;
        this.date = date;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }
}
