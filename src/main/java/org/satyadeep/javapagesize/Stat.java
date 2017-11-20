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
            int prefix = len/2 - 2;
            int suffix = len/2 - 1;
            if (len%2 == 1) {
                    prefix++; // if we have odd length, we can include an extra char in prefix
            }
            return s.substring(0, prefix) + "..." + s.substring(s.length() - suffix);
        }
        return s;
    }

    public void toStdout() {
        if (this.components.size() > 2) {
            this.components.subList(2, this.components.size()).sort((c1, c2) -> (int)(c2.timeTaken - c1.timeTaken));
        }
        System.out.printf("%-50s, %12d, %12d\n", trimToLength(this.url, 50), this.timeTaken, this.size);
        for (Stat s : this.components) {
            System.out.printf("  %-48s, %12d, %12d\n", trimToLength(s.url, 48), s.timeTaken, s.size);
        }
    }

    public static void headerToStdout() {
        System.out.printf("%-50s, %12s, %12s\n", "URL", "Time (ms)", "Size (bytes)");
    }
}