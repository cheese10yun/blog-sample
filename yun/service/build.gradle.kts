plugins{
    kotlin("plugin.jpa")
    kotlin ("kapt")
}

subprojects {
    dependencies {
        api(project(":domain"))
        api(project(":test-support"))
    }
}