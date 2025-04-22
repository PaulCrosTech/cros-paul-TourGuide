package com.openclassrooms.tourguide.dto;


/**
 * This class represents a nearby attraction with its details.
 */
public class NearByAttractionsDto {

    private String attractionName;
    private double attractionLatitude;
    private double attractionLongitude;
    private double distanceInMiles;
    private double rewardPoints;

    /**
     * Constructor
     * @param attractionName the attraction name
     * @param attractionLatitude the attraction latitude
     * @param attractionLongitude the attraction longitude
     * @param distanceInMiles the distance in miles
     * @param rewardPoints the reward points
     */
    public NearByAttractionsDto(String attractionName, double attractionLatitude,
                                double attractionLongitude, double distanceInMiles,
                                double rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.distanceInMiles = distanceInMiles;
        this.rewardPoints = rewardPoints;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    public void setAttractionLatitude(double attractionLatitude) {
        this.attractionLatitude = attractionLatitude;
    }

    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    public void setAttractionLongitude(double attractionLongitude) {
        this.attractionLongitude = attractionLongitude;
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
