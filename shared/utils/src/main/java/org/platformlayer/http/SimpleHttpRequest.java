package org.platformlayer.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class SimpleHttpRequest {
    final HttpURLConnection httpConn;
    final URL url;
    final String method;

    public static SimpleHttpRequest build(String method, URI uri) throws IOException {
        return new SimpleHttpRequest(method, uri);
    }

    protected SimpleHttpRequest(String method, URI uri) throws IOException {
        this.method = method;

        this.url = uri.toURL();

        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);
        httpConn.setUseCaches(false);
        httpConn.setDefaultUseCaches(false);
        httpConn.setAllowUserInteraction(false);
        httpConn.setRequestMethod(method);
    }

    public void setRequestHeader(String key, String value) {
        httpConn.setRequestProperty(key, value);
    }

    SimpleHttpResponse response;

    public SimpleHttpResponse doRequest() throws IOException {
        if (response == null) {
            response = doRequest0();
        }
        return response;
    }

    protected SimpleHttpResponse doRequest0() throws IOException {
        return new SimpleHttpResponse();
    }

    public class SimpleHttpResponse {
        private final int responseCode;

        public SimpleHttpResponse() throws IOException {
            this(httpConn.getResponseCode());
        }

        public SimpleHttpResponse(int responseCode) {
            this.responseCode = responseCode;
        }

        public int getHttpResponseCode() throws IOException {
            return responseCode;
        }

        public Map<String, List<String>> getHeaderFields() {
            return httpConn.getHeaderFields();
        }

        public InputStream getErrorStream() {
            return httpConn.getErrorStream();
        }

        public String getResponseMessage() throws IOException {
            return httpConn.getResponseMessage();
        }

        InputStream is = null;

        public InputStream getInputStream() throws IOException {
            if (is == null) {
                is = httpConn.getInputStream();
            }
            return is;
        }

        public void close() throws IOException {
            if (is != null) {
                is.close();
                is = null;
            }
        }

        public String getResponseHeaderField(String name) {
            return httpConn.getHeaderField(name);
        }

        public Map<String, String> getHeadersRemoveDuplicates() {
            Map<String, String> allHeaders = Maps.newHashMap();
            Map<String, List<String>> headerFields = httpConn.getHeaderFields();
            for (Entry<String, List<String>> headerEntry : headerFields.entrySet()) {
                // Collapse multiple values (we don't expect duplicates)
                for (String headerValue : headerEntry.getValue()) {
                    allHeaders.put(headerEntry.getKey(), headerValue);
                }
            }
            return allHeaders;
        }

    }

    public OutputStream getOutputStream() throws IOException {
        return httpConn.getOutputStream();
    }

    public URL getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    // public void addTags(Tags tags) {
    // if (tags != null) {
    // for (Tag tag : tags.all()) {
    // httpConn.setRequestProperty("X-Tag-" + tag.key, tag.value);
    // }
    // }
    // }
}
