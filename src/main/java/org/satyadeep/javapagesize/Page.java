package org.satyadeep.javapagesize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.AsyncHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.satyadeep.javapagesize.ElementResource.AsResource;
import org.satyadeep.javapagesize.ElementResource.*;
import org.satyadeep.javapagesize.Resource.ResourceType;;

public class Page {
    final private AsyncHttpClient asyncHttpClient;
    final private Resource base;
    final private Map<ResourceType, Map<String, Resource>> assets = new HashMap<>();
    private long size;
    private long timeTakenInMillis;
    private long parseTimeInMillis;

    public Page(AsyncHttpClient asyncHttpClient, String url) {
        this.asyncHttpClient = asyncHttpClient;
        this.base = new Resource(asyncHttpClient, url, ResourceType.HTML);
    }

    public CompletableFuture<Page> resolve() {
        final long startTime = System.currentTimeMillis();
        CompletableFuture<Page> pageF = this.base.get().thenApplyAsync(rr -> {
            if (rr.resource.getError() != null) {
                return this;
            }
            this.size += rr.resource.getSize();
            this.loadResources(rr.response.getResponseBodyAsStream());
            return this;
        });
        return loadAssetsAsync(pageF, startTime);
    }

    private CompletableFuture<Page> loadAssetsAsync(CompletableFuture<Page> pageF, final long startTime) {
        return pageF.thenCompose(p -> {
            CompletableFuture<Page> result = CompletableFuture.completedFuture(p);
            for (Map<String, Resource> resByUrl : p.assets.values()) {
                for (Resource res : resByUrl.values()) {
                    result = res.get().thenCombine(result, (rr, p1) -> {
                        p1.size += rr.resource.getSize();
                        return p1;
                    });
                }
            }
            return result;
        }).thenApply(p -> {
            p.timeTakenInMillis = System.currentTimeMillis() - startTime;
            return p;
        });
    }

    public String getUrl() {
        return this.base.getUrl();
    }

    public long getSize() {
        return this.size;
    }

    public long getTimeTakenInMillis() {
        return this.timeTakenInMillis;
    }

    public Stat getStats() {
        Stat s = new Stat(this.base.getUrl(), this.timeTakenInMillis, this.size, null);
        s.addComponent(new Stat(this.base.getUrl(), this.base.getTimeTakenInMillis(), this.base.getSize(),
                this.base.getError()));
        s.addComponent(new Stat("parse", this.parseTimeInMillis, 0, null));
        for (Map<String, Resource> byType : assets.values()) {
            for (Resource r : byType.values()) {
                s.addComponent(new Stat(r.getUrl(), r.getTimeTakenInMillis(), r.getSize(), r.getError()));
            }
        }
        return s;
    }

    private void loadResources(InputStream is) {
        final long startTime = System.currentTimeMillis();
        List<Resource> resources = extractAssets(this.asyncHttpClient, is, this.getUrl());
        try {
            URL baseURL = new URL(this.base.getUrl());
            for (Resource res : resources) {
                if (!assets.containsKey(res.geResourceType())) {
                    assets.put(res.geResourceType(), new HashMap<>());
                }
                assets.get(res.geResourceType()).put(res.getUrl(), res.normalizeURL(baseURL));
            }
        } catch (MalformedURLException cause) {
            throw new RuntimeException("Not a valid URL", cause);
        } finally {
            this.parseTimeInMillis = System.currentTimeMillis() - startTime;
        }
    }

    static List<Resource> extractAssets(final AsyncHttpClient asyncHttpClient, InputStream is, String url) {
        try {
            Document doc = Jsoup.parse(is, null, url);
            return extractAssets(asyncHttpClient, doc);
        } catch (IOException cause) {
            throw new RuntimeException("Error parsing HTML", cause);
        }
    }

    static List<Resource> extractAssets(final AsyncHttpClient asyncHttpClient, Element element) {
        List<Resource> resources = new ArrayList<>();
        AsResource asRes = null;
        switch (element.tagName()) {
        case "link":
            asRes = new LinkElement(element);
            break;
        case "img":
            asRes = new ImgElement(element);
            break;
        case "script":
            asRes = new ScriptElement(element);
            break;
        }
        if (asRes != null) {
            Resource r = asRes.apply(asyncHttpClient);
            if (r != null) {
                resources.add(r);
            }
        }
        for (Element child : element.children()) {
            resources.addAll(extractAssets(asyncHttpClient, child));
        }
        return resources;
    }
}