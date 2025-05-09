# TourGuide Application

## Description

TourGuide is an application that helps users find nearby tourist attractions and access discounts on
hotels and show tickets. The application is built with Spring Boot and provides a REST API.

## Prerequisites

> Java 17  
> Spring Boot 3.X  
> JUnit 5

## Installing prerequisites

1.Install Java:
https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html

2.Install Maven:  
https://maven.apache.org/install.html

## Installing external services

This application needs three external services: gpsUtil, rewardCentral, and tripPricer.
You have to install them locally in your Maven repository.  
Go to the folder that contains the pom.xml file and execute the below command :

`mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar`  
`mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral
-Dversion=1.0.0 -Dpackaging=jar`  
`mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0
-Dpackaging=jar`

## Start the application

To run the application, you can :

- use the command : `mvn spring-boot:run`
- run the main class : `PoseidonApplication.java`

Then go to the URL http://localhost:8080/

## Use the test dataset

By default, the application uses the test dataset. For disabling it, you can set the property `testMode` to `false` in
the TourGuideService.CLASS.

## Testing

To run the tests from maven, go to the folder that contains the pom.xml file and execute the below command.

For unit and performance tests : `mvn clean test`

## Using the application

You can use the following endpoints:

- `GET /` : Welcoming message.
- `GET /getLocation?userName={userName}` : get the user location.
- `GET /getNearbyAttractions?userName={userName}` : get the five nearest attractions of the user.
- `GET /getRewards?userName={userName}` : get the rewards earned by the user for visiting attractions.
- `GET /getTripDeals?userName={userName}` : get special offers the user can buy with their rewards.

*With the test dataset, you can use 'internalUser0' as {userName}*