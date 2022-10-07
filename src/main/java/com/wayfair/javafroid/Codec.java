package com.wayfair.javafroid;

public interface Codec {

  byte[] encode(byte[] decoded);

  byte[] decode(byte[] encoded);

}
