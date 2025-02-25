package com.xtreamity.utils;

public class ServerDetailsModal {
    private String url;
    private String username;
    private String password;

    private String region;

    public ServerDetailsModal(String url, String username, String password, String region) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.region = region;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}