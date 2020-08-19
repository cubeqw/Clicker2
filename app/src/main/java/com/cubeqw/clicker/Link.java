package com.cubeqw.clicker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Link {
    @SerializedName("scanned")
    @Expose
    String scanned;
    @SerializedName("head")
    @Expose
    String head;
    @SerializedName("link")
    @Expose
    String link;
    @SerializedName("date")
    @Expose
    String date;

    public Link(String scanned, String head, String link, String date) {
        this.scanned = scanned;
        this.head = head;
        this.link = link;
        this.date = date;
    }

    public String getHead() {
        return head;
    }

    public String getLink() {
        return link;
    }

    public String getDate() {
        return date;
    }

    public String getScanned() {
        return scanned;
    }
}
