package org.satyadeep.javapagesize;

import java.util.function.Function;
import org.asynchttpclient.AsyncHttpClient;
import org.jsoup.nodes.Element;
import org.satyadeep.javapagesize.Resource.ResourceType;

public class ElementResource {
    public static abstract class AsResource implements Function<AsyncHttpClient, Resource> {
        protected final Element element;

        protected AsResource(Element element) {
            this.element = element;
        }
    }

    public static class LinkElement extends AsResource {
        public LinkElement(Element element) {
            super(element);
        }

        @Override
        public Resource apply(final AsyncHttpClient asyncHttpClient) {
            String href = this.element.attr("href");
            if (href == null || href.equals("")) {
                return null;
            }
            switch (this.element.attr("href")) {
            case "icon":
                return new Resource(asyncHttpClient, href, ResourceType.IMG);
            case "stylesheet":
                return new Resource(asyncHttpClient, href, ResourceType.CSS);
            }
            return null;
        }
    }

    public static class ImgElement extends AsResource {
        public ImgElement(Element element) {
            super(element);
        }

        @Override
        public Resource apply(final AsyncHttpClient asyncHttpClient) {
            String src = this.element.attr("src");
            if (src != null && !src.equals("")){
                return new Resource(asyncHttpClient, src, ResourceType.IMG);
            }
            return null;
        }
    }

    public static class ScriptElement extends AsResource {
        public ScriptElement(Element element) {
            super(element);
        }

        @Override
        public Resource apply(final AsyncHttpClient asyncHttpClient) {
            String src = this.element.attr("src");
            if (src != null && !src.equals("")){
                return new Resource(asyncHttpClient, src, ResourceType.SCRIPT);
            }
            return null;
        }
    }

}