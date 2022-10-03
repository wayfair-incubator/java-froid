# `java-froid`: Java - Federated Relay Object Identification

[![Release](https://img.shields.io/github/v/release/wayfair-incubator/java-froid?display_name=tag)](CHANGELOG.md)
[![Lint](https://github.com/wayfair-incubator/java-froid/actions/workflows/lint.yml/badge.svg?branch=main)](https://github.com/wayfair-incubator/java-froid/actions/workflows/lint.yml)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.0-4baaaa.svg)](CODE_OF_CONDUCT.md)
[![Maintainer](https://img.shields.io/badge/Maintainer-Wayfair-7F187F)](https://wayfair.github.io)

## About The Project

### The problem

There isn't good support for the Relay's [Object Identification] spec in the
Federated GraphQL ecosystem. This makes it difficult to support common patterns
used to refrech objects from your graph to power things like cache TTLs and
cache-miss hydration.

### The solution

`@wayfair/java-froid` provides a key piece of functionality to the java
ecosystem:

- **id processing**: a solution that can be used to run a java-based subgraph
  dedicated to service your object identification implementation.

In order to support generation the schema needed to associate your java subgraph
with all entities that need `id` generation support, please see our [NodeJS
Froid] library.

## Getting Started

The `java-froid` dependency can be added to your Gradle or Maven configuration.

### Gradle

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation "com.wayfair:java-froid:0.1.0"
}
```

### Maven

```xml
<dependency>
  <groupId>com.wayfair</groupId>
  <artifactId>java-froid</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Library

### [Froid](src/main/java/com/wayfair/javafroid/Froid.java)

This class implements the core api of the library: `handleFroidRequest(Request req)`.
Pass it a [Request](src/main/java/com/wayfair/javafroid/model/Request.java) object and the library decides to decode IDs into Entities or encode Entities into IDs.


### [Codec](src/main/java/com/wayfair/javafroid/Codec.java)

Froid can be configured with a custom [Codec](src/main/java/com/wayfair/javafroid/Codec.java). During ID generation Entity keys are converted to a JSON structure and the bytes passed to the Codec.encode method. During Entity hydration the ID bytes are passed to the Codec.decode method.


This is a convenient way to introduce encryption if your use-case requires it.

### [DocumentProvider](src/main/java/com/wayfair/javafroid/DocumentProvider.java)

Froid also supports a DocumentProvider class that enables you to introduce a cache.

### [Froid.Builder](src/main/java/com/wayfair/javafroid/Froid.java)

Froid provides a Builder class that will generate defaults for required arguments if not set.

### [Model](src/main/java/com/wayfair/javafroid/model)

This package models the federated graphql protocol for FROID.

## Usage

The example below shows how to use Froid in a typical Spring Boot environment.
Spring manages serializing the request body into Froid [Request](src/main/java/com/wayfair/javafroid/model/Request.java) object.

```java
import com.wayfair.javafroid.Froid;
import com.wayfair.javafroid.model.Request;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphqlController {

  private final Froid froid = Froid.builder().build();

  @Operation(summary = "Swagger/OpenAPI info for graphql endpoint")
  @PostMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Object graphql(@RequestBody Request request) {
    return froid.handleFroidRequest(request);
  }
}
```

A more advanced setup with a DocumentProvider for caching and custom codec for encryption/decryption.

```java
@Service
public class FroidCodec implements Codec {
    @Override
    public byte[] encode(byte[] bytes) {
        return someEncryption(bytes);
    }

    @Override
    public byte[] decode(byte[] bytes) {
      return someDecryption(bytes);
    }
}
```
```java
@Configuration
public class CacheConfig {
  @Bean
  public DocumentProvider documentProvider() {
    Cache<Long, Document>  cache = Caffeine.newBuilder().maximumSize(1_000_000).build();
    return (query, documentProvider) -> {
      long queryKey = MurmurHash3.hash64(query.getBytes(StandardCharsets.UTF_8));
      return cache.get(queryKey, key -> documentProvider.apply(query));
    };
  }

}
```
```java
@RestController
public class GraphqlController {

  private final Froid froid;

  public GraphqlController(FroidCodec codec, DocumentProvider documentProvider) {
    froid = Froid
        .builder()
        .setCodec(codec)
        .setDocumentProvider(documentProvider)
        .build();
  }

  @Operation(summary = "Swagger/OpenAPI info for graphql endpoint")
  @PostMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public Object graphql(@RequestBody Request request) {
    return froid.handleFroidRequest(request);
  }
}
```

## Roadmap

See the [open issues](https://github.com/wayfair-incubator/java-froid/issues)
for a list of proposed features (and known issues).

## Contributing

Contributions are what make the open source community such an amazing place to
learn, inspire, and create. Any contributions you make are **greatly
appreciated**. For detailed contributing guidelines, please see
[CONTRIBUTING.md](CONTRIBUTING.md)

## License

Distributed under the `MIT` License. See [`LICENSE`][license] for more
information.

## Contact

Your Name - [@markjfaga](https://twitter.com/markjfaga)

Project Link:
[https://github.com/wayfair-incubator/java-froid](https://github.com/wayfair-incubator/java-froid)

## Acknowledgements

This template was adapted from
[https://github.com/othneildrew/Best-README-Template](https://github.com/othneildrew/Best-README-Template).

[license]: https://github.com/wayfair-incubator/java-froid/blob/main/LICENSE
[object identification]:
  https://relay.dev/docs/guides/graphql-server-specification/#object-identification
[nodejs froid]: https://github.com/wayfair-incubator/node-froid
