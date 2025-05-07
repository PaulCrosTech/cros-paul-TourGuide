package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

/**
 * RewardsService class
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

    /**
     * Constructor
     *
     * @param gpsUtil       the GPS utility
     * @param rewardCentral the rewards central
     */
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Calculate the rewards for a user
     *
     * @param user the User
     */
    // TODO : Code revue, new Thread Version
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtil.getAttractions();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (VisitedLocation visitedLocation : userLocations) {
            for (Attraction attraction : attractions) {
                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    if (nearAttraction(visitedLocation, attraction)) {

                        futures.add(CompletableFuture.runAsync(() -> {
                            user.addUserReward(
                                    new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user))
                            );
                        }, executorService));
                    }
                }
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Calculate the rewards for a list of users with threads
     *
     * @param users List of users
     */
    // TODO : Code revue, new Thread Version
    public void calculateUsersRewards(List<User> users) {
        users.parallelStream().forEach(this::calculateRewards);
    }

    /**
     * Check if a location is within the proximity of an attraction
     *
     * @param attraction the attraction
     * @param location   the location
     * @return true if the location is within the proximity of the attraction, false otherwise
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) <= attractionProximityRange;
    }

    /**
     * Check if a visited location is near an attraction
     *
     * @param visitedLocation the visited location
     * @param attraction      the attraction
     * @return true if the visited location is near the attraction, false otherwise
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }

    /**
     * Get rewards points for an attraction
     *
     * @param attraction the attraction
     * @param user       the user
     * @return the rewards point
     */
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    /**
     * Get RewardsCentral
     *
     * @return the rewards central
     */
    public RewardCentral getRewardsCentral() {
        return rewardsCentral;
    }

    /**
     * Calculate the distance between two locations
     *
     * @param loc1 location one
     * @param loc2 location two
     * @return the distance in miles
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
