package com.company.dataextract.dto;

import java.util.ArrayList;
import java.util.List;

public class CollibraLoadResponse {
    private String filename;
    private String sourcePath;
    private List<String> referredExtractJsons = new ArrayList<>();
    private List<String> referredTransformJsons = new ArrayList<>();
    private String targetUrl;
    private int statusCode;
    private String responseBody;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<String> getReferredExtractJsons() {
        return referredExtractJsons;
    }

    public void setReferredExtractJsons(List<String> referredExtractJsons) {
        this.referredExtractJsons = referredExtractJsons;
    }

    public List<String> getReferredTransformJsons() {
        return referredTransformJsons;
    }

    public void setReferredTransformJsons(List<String> referredTransformJsons) {
        this.referredTransformJsons = referredTransformJsons;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
