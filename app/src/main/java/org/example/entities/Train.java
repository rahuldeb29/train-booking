package org.example.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Train {

    private String trainId;
    private String trainNo;

    // 2D seat layout (0 = available, 1 = booked)
    private List<List<Integer>> seats;

    // Station -> Time (metadata only)
    private Map<String, String> stationTimes;

    // Route order (THIS FIXES SEARCH)
    private List<String> stations;

    public String getTrainInfo() {
        return String.format("Train ID: %s | Train No: %s", trainId, trainNo);
    }
}
