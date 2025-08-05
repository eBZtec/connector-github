package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SeatSearchResult<T> {
    @JsonProperty("total_seats")
    public int total_seats;

    @JsonProperty("seats")
    public T[] seats;
}