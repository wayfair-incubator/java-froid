package com.wayfair.javafroid;

import java.util.function.Function;
import graphql.language.Document;

public interface DocumentProvider {

  Document apply(String query, Function<String, Document> parseFunction);
}
