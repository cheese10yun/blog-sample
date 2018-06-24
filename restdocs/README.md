
## 프로젝트 셋팅

### Maven

```xml

<properties>
    ...
    <snippetsDirectory>${project.build.directory}/generated-snippets</snippetsDirectory>
</properties>

<dependencies>
    ...
    <dependency>
        <groupId>org.springframework.restdocs</groupId>
        <artifactId>spring-restdocs-mockmvc</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<plugins>
    ...
    <plugin>
        <groupId>org.asciidoctor</groupId>
        <artifactId>asciidoctor-maven-plugin</artifactId>
        <version>1.5.6</version>
        <executions>
            <execution>
                <id>generate-docs</id>
                <phase>package</phase>
                <goals>
                    <goal>process-asciidoc</goal>
                </goals>
                <configuration>
                    <backend>html</backend>
                    <doctype>book</doctype>
                    <attributes>
                        <snippets>${snippetsDirectory}</snippets>
                    </attributes>
                    <sourceDirectory>src/docs/asciidocs</sourceDirectory>
                    <outputDirectory>target/generated-docs</outputDirectory>
                </configuration>
            </execution>
        </executions>
    </plugin>
</plugins>

```
