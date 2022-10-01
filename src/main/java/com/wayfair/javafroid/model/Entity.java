package com.wayfair.javafroid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wayfair.javafroid.model.Error.Builder;

public class Entity {
  @JsonProperty("__typename")
  private String typeName;
  private String id;

  public Entity() {
  }

  public Entity(String typeName, String id) {
    this.typeName = typeName;
    this.id = id;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getId() {
    return id;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public void setId(String id) {
    this.id = id;
  }

  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {

    private String typeName;
    private String id;

    public Builder setTypeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Entity build() {
      return new Entity(typeName, id);
    }
  }

}
