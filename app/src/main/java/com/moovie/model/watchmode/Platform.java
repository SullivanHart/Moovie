package com.moovie.model.watchmode;

/**
 * Model class for a streaming platform.
 */
public class Platform {
    private String id;
    private String name;
    private String logo; // URL to logo
    private String region;

    /**
     * Constructor for Platform.
     * @param id The platform ID.
     * @param name The platform name.
     * @param logo The URL to the platform's logo.
     * @param region The region for the platform.
     */
    public Platform(String id, String name, String logo, String region) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.region = region;
    }

    // Getters

    /**
     * Gets the platform ID.
     * @return The platform ID.
     */
    public String getId() { return id; }

    /**
     * Gets the platform name.
     * @return The platform name.
     */
    public String getName() { return name; }

    /**
     * Gets the URL to the platform's logo.
     * @return The logo URL.
     */
    public String getLogo() { return logo; }

    /**
     * Gets the region for the platform.
     * @return The region.
     */
    public String getRegion() { return region; }
}
