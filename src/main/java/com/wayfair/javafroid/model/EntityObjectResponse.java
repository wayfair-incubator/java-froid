package com.wayfair.javafroid.model;

import java.util.List;
import java.util.Map;
import com.wayfair.javafroid.model.Error.Builder;

public class EntityObjectResponse {
  private Map<String, Object> data;
  private List<Error> error;
  private Map<String, Object> extensions;

  public EntityObjectResponse() {
  }

  public EntityObjectResponse(Map<String, Object> data, List<Error> error, Map<String, Object> extensions) {
    this.data = data;
    this.error = error;
    this.extensions = extensions;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public List<Error> getError() {
    return error;
  }

  public Map<String, Object> getExtensions() {
    return extensions;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public void setError(List<Error> error) {
    this.error = error;
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
