package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Ticket;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {

    private static final String USER_FILE_PATH =
            "app/src/main/java/org/example/localDb/users.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;
    private User loggedInUser;

    // ---------- CONSTRUCTORS ----------
    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    public UserBookingService(User user) throws IOException {
        this.loggedInUser = user;
        loadUserListFromFile();
    }

    // ---------- FILE HANDLING ----------
    private void loadUserListFromFile() throws IOException {
        File file = new File(USER_FILE_PATH);

        if (!file.exists()) {
            userList = new ArrayList<>();
            objectMapper.writeValue(file, userList);
            return;
        }

        userList = objectMapper.readValue(
                file,
                new TypeReference<List<User>>() {}
        );
    }

    private void saveUserListToFile() throws IOException {
        objectMapper.writeValue(new File(USER_FILE_PATH), userList);
    }

    // ---------- AUTH ----------
    public boolean loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(u ->
                        u.getName().equals(loggedInUser.getName()) &&
                                UserServiceUtil.checkPassword(
                                        loggedInUser.getPassword(),
                                        u.getHashedPassword()
                                )
                )
                .findFirst();

        if (foundUser.isPresent()) {
            loggedInUser = foundUser.get(); // session set
            return true;
        }
        return false;
    }

    public boolean signUp(User newUser) {
        try {
            userList.add(newUser);
            saveUserListToFile();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    // ---------- BOOKINGS ----------
    public void fetchBookings() {
        if (loggedInUser != null) {
            loggedInUser.printTickets();
        }
    }

    public boolean cancelBooking(String ticketId) {
        if (loggedInUser == null || ticketId == null || ticketId.isEmpty()) {
            return false;
        }

        boolean removed = loggedInUser.getTicketsBooked()
                .removeIf(t -> t.getTicketId().equals(ticketId));

        if (removed) {
            try {
                saveUserListToFile();
            } catch (IOException ignored) {}
        }

        return removed;
    }

    // ---------- TRAINS ----------
    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public boolean bookTrainSeat(Train train, int row, int col) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();

            if (row < 0 || row >= seats.size()) return false;
            if (col < 0 || col >= seats.get(row).size()) return false;

            if (seats.get(row).get(col) == 0) {
                seats.get(row).set(col, 1);
                train.setSeats(seats);
                trainService.addTrain(train);
                return true;
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
}
