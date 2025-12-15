package org.example;

import org.example.entities.Train;
import org.example.entities.User;
import org.example.services.UserBookingService;
import org.example.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {

        System.out.println("Running Train Booking System");

        Scanner scanner = new Scanner(System.in);
        int option = 0;

        UserBookingService userBookingService;
        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Failed to initialize application");
            ex.printStackTrace();
            return;
        }

        Train trainSelectedForBooking = null;

        while (option != 7) {
            System.out.println("\nChoose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");

            option = scanner.nextInt();

            switch (option) {

                case 1:
                    System.out.println("Enter username to signup:");
                    String signUpName = scanner.next();

                    System.out.println("Enter password to signup:");
                    String signUpPassword = scanner.next();

                    User userToSignup = new User(
                            signUpName,
                            signUpPassword,
                            UserServiceUtil.hashPassword(signUpPassword),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    boolean signupSuccess = userBookingService.signUp(userToSignup);
                    System.out.println(signupSuccess ? "Signup successful" : "Signup failed");
                    break;

                case 2:
                    System.out.println("Enter username to login:");
                    String loginName = scanner.next();

                    System.out.println("Enter password to login:");
                    String loginPassword = scanner.next();

                    User loginUser = new User(
                            loginName,
                            loginPassword,
                            null,
                            null,
                            null
                    );

                    try {
                        userBookingService = new UserBookingService(loginUser);
                        if (userBookingService.loginUser()) {
                            System.out.println("Login successful");
                        } else {
                            System.out.println("Invalid credentials");
                        }
                    } catch (IOException ex) {
                        System.out.println("Login failed");
                    }
                    break;

                case 3:
                    System.out.println("Fetching your bookings...");
                    userBookingService.fetchBookings();
                    break;

                case 4:
                    System.out.println("Enter source station:");
                    String source = scanner.next();

                    System.out.println("Enter destination station:");
                    String destination = scanner.next();

                    List<Train> trains = userBookingService.getTrains(source, destination);

                    if (trains.isEmpty()) {
                        System.out.println("No trains found");
                        break;
                    }

                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + ". Train ID: " + t.getTrainId());
                        for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                            System.out.println("   Station: " + entry.getKey() + " Time: " + entry.getValue());
                        }
                        index++;
                    }

                    System.out.println("Select train number:");
                    int trainChoice = scanner.nextInt();
                    trainSelectedForBooking = trains.get(trainChoice - 1);
                    break;

                case 5:
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please search and select a train first");
                        break;
                    }

                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);

                    for (List<Integer> row : seats) {
                        for (Integer seat : row) {
                            System.out.print(seat + " ");
                        }
                        System.out.println();
                    }

                    System.out.println("Enter row number:");
                    int row = scanner.nextInt();

                    System.out.println("Enter column number:");
                    int col = scanner.nextInt();

                    boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                    System.out.println(booked ? "Seat booked successfully!" : "Seat booking failed");
                    break;

                case 7:
                    System.out.println("Exiting application...");
                    break;

                default:
                    System.out.println("Invalid option");
            }
        }

        scanner.close();
    }
}
