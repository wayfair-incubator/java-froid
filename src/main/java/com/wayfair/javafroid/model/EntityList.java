package com.wayfair.javafroid.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wayfair.javafroid.model.Error.Builder;

public class EntityList {

  @JsonProperty("_entities")
  private List<Entity> entities;

  public EntityList() {
  }

  public EntityList(List<Entity> entities) {
    this.entities = entities;
  }

  public List<Entity> getEntities() {
    return entities;
  }

  public void setEntities(List<Entity> entities) {
    this.entities = entities;
  }

  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {

    private List<Entity> entities;

    public Builder setEntities(List<Entity> entities) {
      this.entities = entities;
      return this;
    }

    public EntityList build() {
      return new EntityList(entities);
    }
  }

}
