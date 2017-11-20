package org.satyadeep.javapagesize;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

public class Resource {
    public static enum ResourceType {
        HTML,
        CSS,
        IMG,
        SCRIPT
    }

    private final ResourceType resType;
    private final AsyncHttpClient asyncHttpClient;
    private final String url;
    private int size;
    private long timeTakenInMillis;

    public Resource(AsyncHttpClient httpClient, String url, ResourceType resType) {
        this.resType = resType;
        this.asyncHttpClient = httpClient;
        this.url = url;
    }

    public CompletableFuture<ResourceResponse> get() {
        final long startTime = System.currentTimeMillis();
        return asyncHttpClient.prepareGet(url).execute().toCompletableFuture().thenApplyAsync(resp -> {
            this.timeTakenInMillis = System.currentTimeMillis() - startTime;
            this.size = resp.getResponseBodyAsBytes().length;
            return new ResourceResponse(this, resp);
        });
    }

    public Resource normalizeURL(URL base) {
        if (url.startsWith("//")) {
            return new Resource(this.asyncHttpClient, base.getProtocol() + ":" + url, this.resType);
        } else if (url.startsWith("/")) {
            return new Resource(this.asyncHttpClient, base.getProtocol() + "://" + base.getHost() + url, this.resType);
        }
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public int getSize() {
        return this.size;
    }

    public long getTimeTakenInMillis() {
        return this.timeTakenInMillis;
    }

    public ResourceType geResourceType() {
        return this.resType;
    }

    public static class ResourceResponse {
        public final Resource resource;
        public final Response response;

        public ResourceResponse(Resource resource, Response response) {
            this.resource = resource;
            this.response = response;
        }
    }
}