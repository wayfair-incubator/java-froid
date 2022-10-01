package com.wayfair.javafroid;

public interface Froid {


  interface DocumentProvider {

  }

  interface Encoder {
    byte[] encode(byte[] decoded);
  }

  interface Decoder {
    byte[] decode(byte[] encoded);
  }

  Encoder encoder();

  Decoder decoder();

}
