# Liberty Features

[Liberty Features](https://jonasrutishauser.github.io/liberty-features) which extend [Open Liberty](https://openliberty.io/).

[![GNU Lesser General Public License, Version 3, 29 June 2007](https://img.shields.io/github/license/jonasrutishauser/liberty-features.svg?label=License)](http://www.gnu.org/licenses/lgpl-3.0.txt)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jonasrutishauser.liberty/features.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.jonasrutishauser.liberty%22%20a%3A%22features%22)
[![Build Status](https://img.shields.io/github/actions/workflow/status/jonasrutishauser/liberty-features/ci.yml.svg?label=Build)](https://github.com/jonasrutishauser/liberty-features/actions)

## Micrometer-1.0

Feature which makes the micrometer "API" visible for the application. This way an application can program against [Micrometer](https://micrometer.io/).

### Supported annotations

Micrometer does define two annotations, `@Counted` and `@Timed`, that can be added to methods. The annotations will wrap the execution of a method and will emit the following tags in addition to any tags defined on the annotation itself: class, method, and exception (either "none" or the simple class name of a detected exception).

Parameters to `@Counted` and `@Timed` can be annotated with `@MeterTag` to dynamically assign meaningful tag values.

`MeterTag.resolver` can be used to extract a tag from a method parameter, by creating a bean implementing `io.micrometer.common.annotation.ValueResolver` and referring to this class: `@MeterTag(resolver=CustomResolver.class)`

`MeterTag.expression` is also supported, but you have to implement the evaluation of the expression by creating a bean implementing `io.micrometer.common.annotation.ValueExpressionResolver` that can evaluate expressions.
