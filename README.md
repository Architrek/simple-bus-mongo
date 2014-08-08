# simple-bus-mongo
================

Following the [article reposted by Mondora] (https://mondora.com/#!/post/eafc1a14af194ebeb8d0ccc425cbd803) here's a simple implementation.

## Introduction

This implementation of a circular buffer to support a message queue is implemented with Spring and Mongo DB

## Environment

Check [this property file] (src/test/resources/META-INF/com/whiteandreetto/prototypes/simplebus/simplebus.properties) for details about environmental dependencies

## Build

[Gradle] (build.gradle) and [Maven] (pom.xml) build mechanisms are available.

## Spring

```xml

        <!-- Spring Framework - Core -->
        
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <!-- Spring Framework - Integration -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-core</artifactId>
            <version>${spring.integration.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-mongodb</artifactId>
            <version>${spring.integration.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

### XML-less configuration

This prototype uses java-based Spring configuration items.
Configuration entry point is [AppConfig.java] (master/src/main/java/com/whiteandreetto/prototypes/simplebus/AppConfig.java)




