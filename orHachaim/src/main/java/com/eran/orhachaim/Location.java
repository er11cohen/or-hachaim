package com.eran.orhachaim;

public class Location {

    private String time;
    private String humashEn;
    private String parshHe;
    private String parshEn;
    private int scrollY;

    public Location(String time, String humashEn, String parshHe, String parshEn, int scrollY) {
        this.time = time;
        this.humashEn = humashEn;
        this.parshHe = parshHe;
        this.parshEn = parshEn;
        this.scrollY = scrollY;
    }

    public Location(String parshHe, String parshEn, String humashEn)//for weekly learn
    {
        this.humashEn = humashEn;
        this.parshHe = parshHe;
        this.parshEn = parshEn;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHumashEn() {
        return humashEn;
    }

    public void setHumashEn(String humashEn) {
        this.humashEn = humashEn;
    }

    public String getParshHe() {
        return parshHe;
    }

    public void setParshEn(String parshEn) {
        this.parshEn = parshEn;
    }

    public String getParshEn() {
        return parshEn;
    }

    public void setParshHe(String parshHe) {
        this.parshHe = parshHe;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }
}
