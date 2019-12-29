## 조건에 따라 테스트 실행

```kotlin
internal class SampleTest {

    @Test
    @EnabledOnOs(OS.MAC, OS.WINDOWS)
    internal fun `EnabledOnOs`() {
        println("운영체제에 따라서 실행...")
    }
    
    @Test
    @EnabledOnJre(JRE.JAVA_10, JRE.JAVA_11)
    internal fun `EnabledOnJre`() {
        println("JRE 버전에 따라서 실행...")
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "local", matches = "local")
    internal fun EnabledOnIf() {
        println("Env 에따라서 실행...")
    }
}
```

## Tag

```kotlin
internal class SampleTest {

@Test
    @Tag("slow")
    internal fun `tag slow`() {
        println("slow fast")
    }

    @Test
    @Tag("fast")
    internal fun `tag fast`() {
        println("tag fast")
    }
}
```