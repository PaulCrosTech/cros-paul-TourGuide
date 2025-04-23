package com.openclassrooms.tourguide.dto;

import gpsUtil.location.Location;
/**
 * This class represents a nearby attraction with its details.
 */
public class NearByAttractionsDto {

    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private double distanceInMiles;
    private double rewardPoints;

    /**
     * Constructor
     * @param attractionName attraction name
     * @param attractionLocation attraction location
     * @param userLocation user location
     * @param distanceInMiles distance in miles between attraction and user
     * @param rewardPoints reward points
     */
    public NearByAttractionsDto(String attractionName, Location attractionLocation, Location userLocation, double distanceInMiles, double rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.userLocation = userLocation;
        this.distanceInMiles = distanceInMiles;
        this.rewardPoints = rewardPoints;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public double getDistanceInMiles() {
        return distanceInMiles;
    }

    public void setDistanceInMiles(double distanceInMiles) {
        this.distanceInMiles = distanceInMiles;
    }

    public double getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(double rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}
