package org.satyadeep.javapagesize;

import java.util.ArrayList;
import java.util.List;

public class Stat {
    private String url;
    private long timeTaken;
    private long size;
    private List<Stat> components = new ArrayList<>();

    public Stat(String url, long timeTaken, long size) {
        this.url = url;
        this.timeTaken = timeTaken;
        this.size = size;
    }

    public void addComponent(Stat stat) {
        this.components.add(stat);
    }

    private String trimToLength(String s, int len) {
        if (s.length() > len) {
            return s.substring(0, len);
        }
        return s;
    }

    public void toStdout() {
        System.out.printf("%-50s, %10d, %10d\n", trimToLength(this.url, 50), this.timeTaken, this.size);
        for (Stat s : this.components) {
            System.out.printf("  %-48s, %10d, %10d\n", trimToLength(s.url, 48), s.timeTaken, s.size);
        }
    }
}