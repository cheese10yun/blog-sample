# Jacoco 기본적인 사용법

## Jacoco 설정

### 플러그인 설정
```
plugins {
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.10"
}
```
Jacoco 필요 플러그인 설치 진행

### jacocoTestReport

```
tasks.jacocoTestReport {

    reports {
        // (1)
        xml.required.set(true) 
        csv.required.set(false) 
        html.required.set(true)
        // (2)
        html.outputLocation.set(file("$buildDir/reports/jacoco"))
    }

    classDirectories.setFrom(
       // (3)
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "com/example/prometheusgrafana/PrometheusGrafanaApplication*"
                    )
                }
            }
        )
    )
}
```

Jacoco Report 설정 (1) 설정은 리포트를 받은 파일일에 대한 설정 일반적으로 테스트 커버리지를 측정 및 분석을 xml 기반으로 하기 때문에 xml true, html 설정은 로컬에서 확인용으로 설정 (2) 설정으로 최종 결과물에 대한 경로 지정

### 리포트 결과물

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/prometheus-grafana/docs/img/jacoco-2.png)

### HTML 리포트

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/prometheus-grafana/docs/img/jacoco-1.png)

HTML 리포트으로 로컬에서 주로 확인 진행

### 테스트 제외

(3) 설정으로 특정 클래스에 대한 테스트 커버리지를 제외 가능 대표적으로 main 클래스, 각종 Config 설정등이 여기에 해당 `exclude`를 통해서 제외 가능 `PrometheusGrafanaApplication`을 제외하면

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/prometheus-grafana/docs/img/jacoco-3.png)

최종 결과물에서 제외

## jacocoTestCoverageVerification

```
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            enabled = true
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }

            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}
```

테스트 커버리에 대한 세부적인 규칙 설정 가능, `LINE`, `BRANCH` 으로 세부적인 규칙 설정 가능하며, `minimum` 설정으로 최소 커버리지 적용 가능하며 빌드시 해당 옵션으로 특정 룰에 만족하지 않으면 빌드 실패로 처리 가능