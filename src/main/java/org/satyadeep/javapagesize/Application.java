package org.satyadeep.javapagesize;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

public class Application {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        try (final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient()) {
            CompletableFuture<Stat> statF = new Page(asyncHttpClient, "https://www.yahoo.com/").resolve()
                .thenApply(p -> p.getStats());
            statF.get().toStdout();
        }
        System.exit(0);
    }
}
