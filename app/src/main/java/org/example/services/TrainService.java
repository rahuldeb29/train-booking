package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private static final String TRAIN_DB_PATH =
            "app/src/main/java/org/example/localDb/trains.json";

    public TrainService() throws IOException {
        File trainsFile = new File(TRAIN_DB_PATH);

        if (!trainsFile.exists()) {
            trainList = new ArrayList<>();
            objectMapper.writeValue(trainsFile, trainList);
        } else {
            trainList = objectMapper.readValue(
                    trainsFile,
                    new TypeReference<List<Train>>() {}
            );
        }
    }

    //  SEARCH
    public List<Train> searchTrains(String source, String destination) {

        String src = source.trim().toLowerCase();
        String dest = destination.trim().toLowerCase();

        return trainList.stream()
                .filter(train -> validTrain(train, src, dest))
                .collect(Collectors.toList());
    }

    //  ADD / UPDATE
    public void addTrain(Train newTrain) {

        Optional<Train> existingTrain = trainList.stream()
                .filter(t -> t.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if (existingTrain.isPresent()) {
            updateTrain(newTrain);
        } else {
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {

        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i ->
                        trainList.get(i)
                                .getTrainId()
                                .equalsIgnoreCase(updatedTrain.getTrainId())
                )
                .findFirst();

        if (index.isPresent()) {
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() {
        try {
            objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CORE ROUTE LOGIC
    private boolean validTrain(Train train, String src, String dest) {

        List<String> stationsLower = train.getStations()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        int sourceIndex = stationsLower.indexOf(src);
        int destinationIndex = stationsLower.indexOf(dest);

        return sourceIndex != -1
                && destinationIndex != -1
                && sourceIndex < destinationIndex;
    }
}
