dependencies {
    api(project(":domain"))
}

//tasks.register("testReport") {
//    val test by tasks
//    val jacocoTestReport by tasks
//
//    dependsOn(test, jacocoTestReport)
//}
//
//tasks.jacocoTestReport {
//    reports {
//        isEnabled = true
//        xml.isEnabled = true
//        csv.isEnabled = true
//        html.destination = file("$buildDir/reports/jacoco")
//    }
//}