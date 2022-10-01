package com.wayfair.javafroid.model;

import java.util.List;

public class Error {

  private String message;

  private List<Location> locations;

  public Error() {
  }

  public Error(String message, List<Location> locations) {
    this.message = message;
    this.locations = locations;
  }

  public String getMessage() {
    return message;
  }

  public List<Location> getLocations() {
    return locations;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setLocations(List<Location> locations) {
    this.locations = locations;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String message;
    private List<Location> locations;

    public Builder setMessage(String message) {
      this.message = message;
      return this;
    }

    public Builder setLocations(List<Location> locations) {
      this.locations = locations;
      return this;
    }

    public Error build() {
      return new Error(message, locations);
    }
  }
}
