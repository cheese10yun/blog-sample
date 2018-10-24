
## REST Docs 란 ?

블라블라

## Rest Docs vs Swagger

블라블라


## pom.xml

```xml
<properties>
    ...
    <snippetsDirectory>${project.build.directory}/generated-snippets</snippetsDirectory>
</properties>


<dependencies>
    ....
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
* `properties`에 `project.build.directory` generated-snippets 디렉토리를 지정합니다.
* `dependency` Rest Docs 의존성을 추가합니다.
* `plugin`을 설정합니다. `${snippetsDirectory}`은 `properties` 에서 지정한 디렉토리 입니다.

## Test Code 작성

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberControllerTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets"); // (1)

    @Autowired
    private WebApplicationContext context;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Before
        public void setUp() {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                    .apply(documentationConfiguration(this.restDocumentation)) // (2)
                    .alwaysDo(document("{class-name}/{method-name}/")) // (3)
                    .build();
    }

    @Test //(4)
    public void signUp() throws Exception {

        // given
        final SignUpDto signUpDto = SignUpDto.builder()
                .email(email)
                .address(address)
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email.value", is(email.getValue())));

    }
}
```