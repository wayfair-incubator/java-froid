package com.wayfair.javafroid.model;

public class Location {

  private int line;
  private int column;

  public Location() {
  }

  public Location(int line, int column) {
    this.line = line;
    this.column = column;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int line;
    private int column;

    public Builder setLine(int line) {
      this.line = line;
      return this;
    }

    public Builder setColumn(int column) {
      this.column = column;
      return this;
    }

    public Location build() {
      return new Location(line, column);
    }
  }
}
