package org.satyadeep.javapagesize;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

public class Application {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        try (final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient()) {
            CompletableFuture<Page> pageF = new Page(asyncHttpClient, "https://www.yahoo.com/").resolve();
            Page page = pageF.get();
            System.out.printf("Page size for %s: %d\n", page.getUrl(), page.getSize());
            System.out.printf("Total time taken: %d ms", page.getTimeTakenInMillis());
        }
        System.exit(0);
    }
}
