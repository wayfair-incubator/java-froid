package com.wayfair.javafroid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.wayfair.javafroid.ThrowingFunction.unchecked;

public class FroidService {

  private static final Base64.Encoder base64Encoder = Base64.getEncoder();
  private static final Base64.Decoder base64Decoder = Base64.getDecoder();
  private static final String TYPE_NAME = "__typename";
  private static final String NODE = "node";
  private static final String ID = "id";
  private static final String REPRESENTATIONS = "representations";
  private final Parser parser;
  private final ObjectMapper mapper;
  private final Froid.Encoder froidEncoder;
  private final Froid.Decoder froidDecoder;

  public FroidService(Froid froid) {
    this.froidEncoder = froid.encoder();
    this.froidDecoder = froid.decoder();
    parser = new Parser();
    mapper = new ObjectMapper();
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
      final Document document = parser.parseDocument(request.getQuery());

      if (request.getVariables() != null && request.getVariables().containsKey(REPRESENTATIONS)) {
        var representations = (List<Map<String, Object>>) request.getVariables().get(REPRESENTATIONS);
        return generateEntityObjectWithId(representations);
      } else {

        return document.getChildren()
            .stream()
            .findFirst()
            .map(unchecked(node -> generateEntityObjectsById(
                node,
                request.getVariables())))
            .orElseThrow();
      }
    } catch (Exception e) {
      var message = new StringBuilder("NODE RELAY ERROR");

      message
          .append("message: ").append(e.getMessage())
          .append("Class: ").append(e.getClass().getName());

      return EntitiesResponse.builder()
          .setErrors(List.of(Error.builder().setMessage(message.toString()).build()))
          .build();
    }
  }

  /**
   * Map a List of GraphQL representations to entity objects.
   * Each Entity has two properties:  __typename, id.
   * The id property is computed by Base64 encoding a string
   * comprised of [__typename>]:json_str(representation fields).
   *
   * @param representations List of representation objects
   * @return The EntitiesResponse
   */
  public EntitiesResponse generateEntityObjectWithId(List<Map<String, Object>> representations) {
    var entities = representations
        .stream()
        .map(unchecked(e -> {
          var typeName = e.get(TYPE_NAME).toString();
          var data = e.entrySet()
              .stream()
              .filter(it -> !it.getKey().equals(TYPE_NAME))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

          var encoded = froidEncoder.encode(mapper.writeValueAsBytes(data));

          return Entity.builder()
              .setTypeName(typeName)
              .setId(toGlobalId(typeName, base64Encoder.encodeToString(encoded)))
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
    var mapped = new HashMap<String, Object>();
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
            var variableName = ((VariableReference) idArg.getValue()).getName();
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
      var field = (Field) node;
      // if this is a node field, and it has an id argument take it.

      if (field.getName().equals(NODE)) {
        var idValue = findIdValue(field, variables);

        if (idValue.isPresent()) {
          var nodeName = field.getName();
          var nodeAlias = field.getAlias();
          var responseFieldName = (nodeAlias != null && !nodeAlias.isEmpty()) ? nodeAlias : nodeName;
          var globalId = fromGlobalId(idValue.get());
          var base64Decoded = base64Decoder.decode(globalId.getId());
          var froidDecoded = froidDecoder.decode(base64Decoded);
          var data = mapper.readValue(froidDecoded, Map.class);
          data.put(TYPE_NAME, globalId.getType());
          out.put(responseFieldName, data);
        }
      }
    }

    for (Node child : (List<Node>) node.getChildren()) {
      visitFields(child, variables, out);
    }
  }

  /**
   * Re-implementing the GlobalId helpers due to incompatibility with graphql-java Relay Encoder.
   * It is highly opinionated on non-padding.
   *
   * @param type the graphql type
   * @param id   the idS
   * @return the global id string
   */
  public String toGlobalId(String type, String id) {
    return base64Encoder.encodeToString((type + ":" + id).getBytes(StandardCharsets.UTF_8));
  }

  public ResolvedGlobalId fromGlobalId(String globalId) {
    String[] split = new String(base64Decoder.decode(globalId), StandardCharsets.UTF_8).split(":", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException(String.format("expecting a valid global id, got %s", globalId));
    }
    return new ResolvedGlobalId(split[0], split[1]);
  }
}
