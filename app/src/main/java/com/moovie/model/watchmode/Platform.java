package com.moovie.model.watchmode;

public class Platform {
    private String id;
    private String name;
    private String logo; // URL to logo
    private String region;

    public Platform(String id, String name, String logo, String region) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.region = region;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLogo() { return logo; }
    public String getRegion() { return region; }
}
