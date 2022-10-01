package com.wayfair.javafroid.model;

import java.util.Map;
import com.wayfair.javafroid.model.Error.Builder;

public class Request {
  private String query;
  private Map<String, Object> variables;
  private String operationName;

  public Request() {
  }

  public Request(String query, Map<String, Object> variables, String operationName) {
    this.query = query;
    this.variables = variables;
    this.operationName = operationName;
  }

  public String getQuery() {
    return query;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String query;
    private Map<String, Object> variables;
    private String operationName;

    public Builder setQuery(String query) {
      this.query = query;
      return this;
    }

    public Builder setVariables(Map<String, Object> variables) {
      this.variables = variables;
      return this;
    }

    public Builder setOperationName(String operationName) {
      this.operationName = operationName;
      return this;
    }

    public Request build() {
      return new Request(query, variables, operationName);
    }
  }
}
