package org.satyadeep.javapagesize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

public class Application {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        List<String> urls = loadUrlsFromArgs(args);
        try (final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient()) {
            final long startTime = System.currentTimeMillis();
            List<CompletableFuture<Stat>> stats = new ArrayList<>();

            // first off each page asynchronously
            for (String url : urls) {
                stats.add(new Page(asyncHttpClient, url).resolve().thenApply(p -> p.getStats()));
            }

            // wait for get's to complete
            CompletableFuture.allOf(stats.toArray(new CompletableFuture[0])).join();

            Stat.headerToStdout();
            for (CompletableFuture<Stat> s : stats) {
                s.get().toStdout();
                System.out.println();
            }

            System.out.printf("Total time: %d ms", System.currentTimeMillis() - startTime);
        }
        System.exit(0);
    }

    static List<String> loadUrlsFromArgs(String[] args) throws IOException {
        List<String> urls = new ArrayList<>();

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("f", "file", true, "file name to read URLs from");
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                printHelpAndExit(options);
            }
            if (line.hasOption("f")) {
                try (BufferedReader br = new BufferedReader(new FileReader(line.getOptionValue("f")))) {
                    String s;
                    while ((s = br.readLine()) != null) {
                        urls.add(s.trim());
                    }
                }
            }
            urls.addAll(line.getArgList());
            if (urls.size() == 0) {
                printHelpAndExit(options);
            }
        } catch (ParseException pe) {
            System.out.println("Error parsing arguments: " + pe);
            System.exit(1);
        }
        return urls;
    }

    static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("javapagesize [options] [url] ... ", options);
        System.exit(0);
    }
}
