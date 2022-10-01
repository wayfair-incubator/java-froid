package com.wayfair.javafroid;

import graphql.language.Document;
import java.util.function.Function;

public interface Froid {

  interface DocumentProvider {
    Document apply(String query,  Function<String, Document> parseFunction);
  }

  interface Encoder {
    byte[] encode(byte[] decoded);
  }

  interface Decoder {
    byte[] decode(byte[] encoded);
  }

  Encoder encoder();

  Decoder decoder();

  DocumentProvider documentProvider();
}
