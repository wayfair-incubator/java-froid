package com.wayfair.javafroid.model;

import java.util.List;

public interface BaseResponse {
  List<Error> getErrors();
}
