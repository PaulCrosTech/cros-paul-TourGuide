package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.NearByAttractionsDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * Tour guide service
 */
@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	public static final int NB_CLOSEST_ATTRACTIONS = 5;
	private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);

	/**
	 * Constructor
	 * @param gpsUtil gpsUtil
	 * @param rewardsService  rewardsService
	 */
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * Get the user rewards
	 * @param user the user
	 * @return a list of user rewards
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	/**
	 * Get the last visited location or the actual location
	 * @param user the user to get the location for
	 * @return visitedLocation
	 */
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	/**
	 * Get the user by userName
	 * @param userName the userName of the user
	 * @return the User
	 */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	/**
	 * Get all users
	 * @return a list of all users
	 */
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	/**
	 * Add a user to the internal user map
	 * @param user the user to add
	 */
	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * Track a user's location and add it to his visited locations
	 * @param user the user
	 * @return VisitedLocation
	 */
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	/**
	 * Track user location of users using ThreadPool
	 * @param users the list of users
	 */
	public void trackUsersLocation(List<User> users) {
		CompletableFuture<?>[] futures = users.stream()
				.map(u -> CompletableFuture.supplyAsync(
						() -> trackUserLocation(u), executorService)
				)
				.toArray(CompletableFuture[]::new);

		CompletableFuture.allOf(futures).join();
		executorService.shutdown();
	}

	/**
	 * Get NB_CLOSEST_ATTRACTIONS closest attractions to the user no matter how far away they are
	 * @param visitedLocation visited location of the user
	 * @return a list of NB_CLOSEST_ATTRACTIONS nearest attractions
	 */
	public List<NearByAttractionsDto> getNearByAttractions(VisitedLocation visitedLocation) {

		return gpsUtil.getAttractions()
				.parallelStream() // Utilisation d'un flux parallèle
				.map(attraction -> {

					double distanceInMiles = rewardsService.getDistance(attraction, visitedLocation.location);

					Location attractionLocation = new Location(attraction.latitude, attraction.longitude);

					int rewardPoints = rewardsService.getRewardsCentral()
							.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId);

					return new AbstractMap.SimpleEntry<>(distanceInMiles, new NearByAttractionsDto(
							attraction.attractionName,
							attractionLocation,
							visitedLocation.location,
							distanceInMiles,
							rewardPoints
					));

				})
				.sorted(Map.Entry.comparingByKey())
				.limit(NB_CLOSEST_ATTRACTIONS)
				.map(Map.Entry::getValue)
				.toList();
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * Methods Below: For Internal Testing
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	/**
	 * Initialize internal users
	 * This method creates a number of internal users and adds them to the internalUserMap
	 */
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}


	/**
	 * Generate a random user location history
	 * @param user the user
	 */
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	/**
	 * Generate a random longitude
	 * @return a random longitude
	 */
	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/**
	 * Generate a random latitude
	 * @return a random latitude
	 */
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/**
	 * Generate a random time
	 * @return a random time
	 */
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
