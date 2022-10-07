package com.wayfair.javafroid.model;

import java.util.ArrayList;
import java.util.List;

public class EntitiesResponse {

  private EntityList data;
  private List<Error> errors;

  public EntitiesResponse() {
  }

  public EntitiesResponse(EntityList data, List<Error> errors) {
    this.data = data;
    this.errors = errors;
  }

  public EntityList getData() {
    return data;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public void setData(EntityList data) {
    this.data = data;
  }

  public void setErrors(List<Error> errors) {
    this.errors = errors;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private EntityList data;
    private List<Error> errors;

    public Builder setData(EntityList data) {
      this.data = data;
      return this;
    }

    public Builder setError(Error error) {
      if (errors == null) {
        errors = new ArrayList<>();
      }
      errors.add(error);
      return this;
    }

    public Builder setErrors(List<Error> errors) {
      this.errors = errors;
      return this;
    }

    public EntitiesResponse build() {
      return new EntitiesResponse(data, errors);
    }
  }
}
