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

@TODO

## Library API

@TODO

## Usage

@TODO

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
