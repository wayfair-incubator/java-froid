package com.wayfair.javafroid.model;

import java.util.List;
import java.util.Map;

public class EntityObjectResponse implements BaseResponse {
  private Map<String, Object> data;
  private List<Error> errors;
  private Map<String, Object> extensions;

  public EntityObjectResponse() {
  }

  public EntityObjectResponse(Map<String, Object> data, List<Error> errors, Map<String, Object> extensions) {
    this.data = data;
    this.errors = errors;
    this.extensions = extensions;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public Map<String, Object> getExtensions() {
    return extensions;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public void setErrors(List<Error> errors) {
    this.errors = errors;
  }

  public void setExtensions(Map<String, Object> extensions) {
    this.extensions = extensions;
  }

  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {

    private Map<String, Object> data;
    private List<Error> error;
    private Map<String, Object> extensions;

    public Builder setData(Map<String, Object> data) {
      this.data = data;
      return this;
    }

    public Builder setError(List<Error> error) {
      this.error = error;
      return this;
    }

    public Builder setExtensions(Map<String, Object> extensions) {
      this.extensions = extensions;
      return this;
    }

    public EntityObjectResponse build() {
      return new EntityObjectResponse(data, error, extensions);
    }
  }
}
