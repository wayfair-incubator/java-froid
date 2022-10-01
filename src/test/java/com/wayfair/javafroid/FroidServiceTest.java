package com.wayfair.javafroid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wayfair.javafroid.model.EntitiesResponse;
import com.wayfair.javafroid.model.EntityObjectResponse;
import com.wayfair.javafroid.model.Request;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FroidServiceTest {

  private static String DEMO_AUTHOR_1 = "RGVtb0F1dGhvcjpleUpoZFhSb2IzSkpaQ0k2TVgwPQ==";
  private static String DEMO_AUTHOR_4 = "RGVtb0F1dGhvcjpleUpoZFhSb2IzSkpaQ0k2TkgwPQ==";
  private static String DEMO_BOOK_1 = "RGVtb0Jvb2s6ZXlKaWIyOXJTV1FpT2pGOQ==";
  private static String DEMO_BOOK_2 = "RGVtb0Jvb2s6ZXlKaWIyOXJTV1FpT2pKOQ==";

  private FroidService service = new FroidService(new Froid() {

    @Override
    public Encoder encoder() {
      return decoded -> decoded;
    }

    @Override
    public Decoder decoder() {
      return encoded -> encoded;
    }

    @Override
    public DocumentProvider documentProvider() {
      return (query, parseFunction) -> {
        // maybe do a cache lookup here...
        return parseFunction.apply(query);
      };
    }
  });

  @Test
  void testEntitiesResponse() {
    Request request = Request
        .builder()
        .setQuery("query booksByGenre__node_relay_service__1($representations:[_Any!]!) {"
            + "_entities(representations:$representations){...on DemoBook{id}}"
            + "}")
        .setVariables(new HashMap<String, Object>() {{
          put("representations", new ArrayList<Object>() {{
            add(new HashMap<String, Object>() {{
              put("__typename", "DemoBook");
              put("bookId", 1);
            }});
            add(new HashMap<String, Object>() {{
              put("__typename", "DemoBook");
              put("bookId", 2);
            }});
          }});
        }})
        .setOperationName("booksByGenre__node_relay_service__1")
        .build();

    EntitiesResponse response = (EntitiesResponse) service.handleFroidRequest(request);

    assertEquals(2, response.getData().getEntities().size());

    List<String> ids = new ArrayList<String>() {{
      add(DEMO_BOOK_1);
      add(DEMO_BOOK_2);
    }};

    for (int i = 0; i < response.getData().getEntities().size(); ++i) {
      assertEquals("DemoBook", response.getData().getEntities().get(i).getTypeName());
      assertEquals(ids.get(i), response.getData().getEntities().get(i).getId());
    }
  }

  @Test
  void testEntityObjectsMultiAliasVariables() {
    Request request = Request
        .builder()
        .setQuery("query author__node_relay_service__0($nodeId:ID!$nodeId2:ID!$nodeId3:ID!) {"
            + "a:node(id:$nodeId){__typename ...on DemoAuthor{__typename authorId}}"
            + "b:node(id:$nodeId2){__typename ...on DemoAuthor{__typename authorId}}"
            + "c:node(id:$nodeId3){__typename ...on DemoBook{__typename bookId}}"
            + "}")
        .setVariables(new HashMap<String, Object>() {{
          put("nodeId", DEMO_AUTHOR_4);
          put("nodeId2", DEMO_AUTHOR_1);
          put("nodeId3", DEMO_BOOK_2);
        }})
        .setOperationName("author__node_relay_service__0")
        .build();

    EntityObjectResponse response = (EntityObjectResponse) service.handleFroidRequest(request);

    assertEquals("DemoAuthor", ((Map) response.getData().get("a")).get("__typename"));
    assertEquals(4, ((Map) response.getData().get("a")).get("authorId"));
    assertEquals("DemoAuthor", ((Map) response.getData().get("b")).get("__typename"));
    assertEquals(1, ((Map) response.getData().get("b")).get("authorId"));
    assertEquals("DemoBook", ((Map) response.getData().get("c")).get("__typename"));
    assertEquals(2, ((Map) response.getData().get("c")).get("bookId"));
  }

  @Test
  void testEntityObjectsMultiAliasValues() {
    Request request = Request
        .builder()
        .setQuery("query author__node_relay_service__0 {"
            + "a:node(id:\"" + DEMO_AUTHOR_4 + "\"){__typename ...on DemoAuthor{__typename authorId}}"
            + "b:node(id:\"" + DEMO_AUTHOR_1 + "\"){__typename ...on DemoAuthor{__typename authorId}}"
            + "c:node(id:\"" + DEMO_BOOK_2 + "\"){__typename ...on DemoBook{__typename bookId}}"
            + "}")
        .setOperationName("author__node_relay_service__0")
        .build();

    EntityObjectResponse response = (EntityObjectResponse) service.handleFroidRequest(request);

    assertEquals("DemoAuthor", ((Map) response.getData().get("a")).get("__typename"));
    assertEquals(4, ((Map) response.getData().get("a")).get("authorId"));
    assertEquals("DemoAuthor", ((Map) response.getData().get("b")).get("__typename"));
    assertEquals(1, ((Map) response.getData().get("b")).get("authorId"));
    assertEquals("DemoBook", ((Map) response.getData().get("c")).get("__typename"));
    assertEquals(2, ((Map) response.getData().get("c")).get("bookId"));
  }

  @Test
  void testEntityObjectsVariables() {
    Request request = Request
        .builder()
        .setQuery("query author__node_relay_service__0($nodeId:ID!$nodeId2:ID!$nodeId3:ID!) {"
            + "node(id:$nodeId){__typename ...on DemoAuthor{__typename authorId}}"
            + "}")
        .setVariables(new HashMap<String, Object>() {{
          put("nodeId", DEMO_AUTHOR_4);
        }})
        .setOperationName("author__node_relay_service__0")
        .build();

    EntityObjectResponse response = (EntityObjectResponse) service.handleFroidRequest(request);

    assertEquals("DemoAuthor", ((Map) response.getData().get("node")).get("__typename"));
    assertEquals(4, ((Map) response.getData().get("node")).get("authorId"));
  }

  @Test
  void testEntityObjectsValue() {
    System.out.println("oooowww");
    Request request = Request
        .builder()
        .setQuery("query author__node_relay_service__0 {"
            + "node(id:\"" + DEMO_AUTHOR_4 + "\"){__typename ...on DemoAuthor{__typename authorId}}"
            + "}")
        .setOperationName("author__node_relay_service__0")
        .build();

    EntityObjectResponse response = (EntityObjectResponse) service.handleFroidRequest(request);

    assertEquals("DemoAuthor", ((Map) response.getData().get("node")).get("__typename"));
    assertEquals(4, ((Map) response.getData().get("node")).get("authorId"));
  }

  @Test
  void testErrors() {
    Request request = Request
        .builder()
        .setQuery("query author__node_relay_service__0($nodeId:ID!) {"
            + "a:node(id:$nodeId){__typename ...on DemoAuthor{__typename authorId}}"
            + "}")
        .setVariables(new HashMap<String, Object>() {{
          put("nodeId", "RGVtb0F1dGhvcjp7ImF1dGhvcklkIjasdfo0fQ==");
        }})
        .setOperationName("author__node_relay_service__0")
        .build();

    EntitiesResponse response = (EntitiesResponse) service.handleFroidRequest(request);
    assertEquals(1, response.getErrors().size());
  }

}
