package com.wayfair.javafroid;

import static com.wayfair.javafroid.ThrowingFunction.unchecked;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wayfair.javafroid.model.EntitiesResponse;
import com.wayfair.javafroid.model.Entity;
import com.wayfair.javafroid.model.EntityList;
import com.wayfair.javafroid.model.EntityObjectResponse;
import com.wayfair.javafroid.model.Error;
import com.wayfair.javafroid.model.Request;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.StringValue;
import graphql.language.VariableReference;
import graphql.parser.Parser;
import graphql.relay.Relay.ResolvedGlobalId;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Froid {

  private static final Base64.Encoder base64Encoder = Base64.getEncoder();
  private static final Base64.Decoder base64Decoder = Base64.getDecoder();
  private static final String TYPE_NAME = "__typename";
  private static final String NODE = "node";
  private static final String ID = "id";
  private static final String REPRESENTATIONS = "representations";
  private final Parser parser;
  private final ObjectMapper mapper;

  private final Codec codec;
  private final DocumentProvider documentProvider;

  private Froid(
      Parser parser,
      ObjectMapper mapper,
      Codec codec,
      DocumentProvider documentProvider
  ) {
    this.parser = parser;
    this.mapper = mapper;
    this.codec = codec;
    this.documentProvider = documentProvider;
    this.mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }

  /**
   * The handle entrypoint decides if this an ID or Entity Object request and
   * routes the call accordingly. If an exception is thrown its message is returned as an error.
   *
   * @param request The GraphQL request.
   * @return The response
   */
  public Object handleFroidRequest(Request request) {

    try {
      final Document document = documentProvider.apply(request.getQuery(), query -> parser.parseDocument(query));

      if (request.getVariables() != null && request.getVariables().containsKey(REPRESENTATIONS)) {
        List<Map<String, Object>> representations = (List<Map<String, Object>>) request.getVariables()
            .get(REPRESENTATIONS);
        return generateEntityObjectWithId(representations);
      } else {

        return document.getChildren()
            .stream()
            .findFirst()
            .map(unchecked(node -> generateEntityObjectsById(
                node,
                request.getVariables())))
            .orElseThrow(() -> new RuntimeException("failed to generate entity objects"));
      }
    } catch (Exception e) {
      StringBuilder message = new StringBuilder("NODE RELAY ERROR ");

      message
          .append("message: ").append(e.getMessage())
          .append("Class: ").append(e.getClass().getName());

      return EntitiesResponse.builder()
          .setError(Error.builder().setMessage(message.toString()).build())
          .build();
    }
  }

  /**
   * Map a List of GraphQL representations to entity objects.
   * Each Entity has two properties:  __typename, id.
   * The id property is computed by Base64 encoding the JSON byte representation.
   * Froid can be configured with an additional encoder applied to the JSON byte reprsentation.
   *
   * @param representations List of representation objects
   * @return The EntitiesResponse
   */
  public EntitiesResponse generateEntityObjectWithId(List<Map<String, Object>> representations) {
    List<Entity> entities = representations
        .stream()
        .map(unchecked(e -> {
          String typeName = e.get(TYPE_NAME).toString();
          Map<Object, Object> data = e.entrySet()
              .stream()
              .filter(it -> !it.getKey().equals(TYPE_NAME))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

          byte[] encoded = codec.encode(mapper.writeValueAsBytes(data));

          return Entity.builder()
              .setTypeName(typeName)
              .setId(toGlobalId(typeName, encoded))
              .build();
        })).collect(Collectors.toList());

    return EntitiesResponse
        .builder()
        .setData(EntityList
            .builder()
            .setEntities(entities)
            .build())
        .build();
  }

  /**
   * Map an ID to an Entity object. Search the parsed GraphQL document for
   * node fields with an id argument.
   *
   * @param root      The root node of the parsed GraphQL query.
   * @param variables The GraphQL variables passed as part of the request.
   * @return The EntityObjectResponse
   * @throws IOException Any JSON parsing errors
   */
  public EntityObjectResponse generateEntityObjectsById(Node root, Map<String, Object> variables) throws IOException {
    HashMap<String, Object> mapped = new HashMap<>();
    visitFields(root, variables, mapped);
    return EntityObjectResponse.builder().setData(mapped).build();
  }

  /**
   * The ID value to decode either resides in Graphql variables or passed directly to the node() field.
   *
   * @param node      The parsed node field.
   * @param variables The GraphQL variables passed as part of the request.
   * @return An optional string vlaue
   */
  private Optional<String> findIdValue(Field node, Map<String, Object> variables) {
    return node.getArguments()
        .stream()
        .filter(it -> it.getName().equals(ID))
        .findFirst()
        .map(idArg -> {
          if (idArg.getValue() instanceof VariableReference) {
            String variableName = ((VariableReference) idArg.getValue()).getName();
            return variables.get(variableName).toString();
          } else if (idArg.getValue() instanceof StringValue) {
            return ((StringValue) idArg.getValue()).getValue();
          } else {
            return null;
          }
        });
  }

  /**
   * Visit each node in the document as there may be multiple root node queries.
   *
   * @param node      The current node in the tree
   * @param variables The GraphQL variables passed in the request
   * @param out       Collects mapped Entity objects
   * @throws IOException Any JSON parsing errors
   */
  private void visitFields(Node node, Map<String, Object> variables, Map<String, Object> out) throws IOException {

    if (node instanceof Field) {
      Field field = (Field) node;
      // if this is a node field, and it has an id argument take it.

      if (field.getName().equals(NODE)) {
        Optional<String> idValue = findIdValue(field, variables);

        if (idValue.isPresent()) {
          String nodeName = field.getName();
          String nodeAlias = field.getAlias();
          String responseFieldName = (nodeAlias != null && !nodeAlias.isEmpty()) ? nodeAlias : nodeName;
          ResolvedGlobalId globalId = fromGlobalId(idValue.get().getBytes(StandardCharsets.UTF_8));
          byte[] froidDecoded = codec.decode(globalId.getId().getBytes(StandardCharsets.UTF_8));
          Map data = mapper.readValue(froidDecoded, Map.class);
          data.put(TYPE_NAME, globalId.getType());
          data.put(ID, idValue.get());
          out.put(responseFieldName, data);
        }
      }
    } else {
      for (Node child : (List<Node>) node.getChildren()) {
        visitFields(child, variables, out);
      }
    }
  }

  /**
   * Re-implementing the GlobalId helpers due to incompatibility with graphql-java Relay Encoder.
   * It is highly opinionated on non-padding.
   *
   * @param type the graphql type
   * @param id   the id string
   * @return the global id string
   */
  public static String toGlobalId(String type, String id) {
    return base64Encoder.encodeToString((type + ":" + id).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Re-implementing the GlobalId helpers due to incompatibility with graphql-java Relay Encoder.
   * It is highly opinionated on non-padding.
   *
   * @param type the graphql type
   * @param id   the id byte array
   * @return the global id string
   */
  public static String toGlobalId(String type, byte[] id) {
    ByteBuffer buffer = ByteBuffer.allocate(type.getBytes().length + id.length + 1);
    buffer.put(type.getBytes(StandardCharsets.UTF_8));
    buffer.put(":".getBytes(StandardCharsets.UTF_8));
    buffer.put(id);
    return base64Encoder.encodeToString(buffer.array());
  }

  /**
   * Decode the ID value into typename and the id string.
   *
   * @param globalId the id String
   * @return the Resolved typename and ID string.
   */
  public static ResolvedGlobalId fromGlobalId(String globalId) {
    String[] split = new String(base64Decoder.decode(globalId), StandardCharsets.UTF_8).split(":", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException(String.format("expecting a valid global id, got %s", globalId));
    }
    return new ResolvedGlobalId(split[0], split[1]);
  }

  /**
   * Decode the ID value into typename and the id string.
   *
   * @param globalId the byte[]
   * @return the Resolved typename and ID string.
   */
  public static ResolvedGlobalId fromGlobalId(byte[] globalId) {
    String[] split = new String(base64Decoder.decode(globalId), StandardCharsets.UTF_8).split(":", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException(String.format("expecting a valid global id, got %s", globalId));
    }
    return new ResolvedGlobalId(split[0], split[1]);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Parser parser;
    private ObjectMapper mapper;

    private Codec codec;

    private DocumentProvider documentProvider;

    public Builder setParser(Parser parser) {
      this.parser = parser;
      return this;
    }

    public Builder setMapper(ObjectMapper mapper) {
      this.mapper = mapper;
      return this;
    }

    public Builder setCodec(Codec codec) {
      this.codec = codec;
      return this;
    }

    public Builder setDocumentProvider(DocumentProvider documentProvider) {
      this.documentProvider = documentProvider;
      return this;
    }

    public Froid build() {
      if (parser == null) {
        parser = new Parser();
      }

      if (mapper == null) {
        mapper = new ObjectMapper();
      }

      if (codec == null) {
        codec = new Codec() {
          @Override
          public byte[] encode(byte[] decoded) {
            return decoded;
          }

          @Override
          public byte[] decode(byte[] encoded) {
            return encoded;
          }
        };
      }

      if (documentProvider == null) {
        documentProvider = (query, parseFunction) -> parseFunction.apply(query);
      }

      return new Froid(parser, mapper, codec, documentProvider);
    }
  }
}
