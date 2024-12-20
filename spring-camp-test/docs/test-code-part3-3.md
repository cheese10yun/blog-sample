## 1. 주제 및 독자 정리

**독자를 분석**하고 **주제 및 콘텐츠 방향을 명확하게** 정하면 전체 콘텐츠를 작성하는 데 큰 도움이 됩니다.

### 주제: 어떤 주제로 콘텐츠를 작성할 건지

- 실무에서 적용하는 테스트 코드 작성 방법과 노하우 시리즈글 Part 3: Given 지옥에서 벗어나기 - 스노우볼을 굴려라
- 테스트 코드의 Given 작성시 코드량이 많아 지며, 테스트 코드의 가독성이 떨어지는 문제를 해결하고자 하는 주제입니다.
- 작은 단위의 로직 부터 작성하여 이런 작은 단위의 로직들을 조합하고 연결하여 큰 단위의 로직을 작작성하게 됩니다. 이때 작은 단위의 로직 부터 작성한 테스트 코드의 Given 절이 큰 단위의 테스트에도 도움이 되어 스노우볼을 굴려나가는 방법을 설명합니다.
- 이런 형식으로 테스트 코드를 작성 해야 테스트 코드에 대한 컨텍스트가 일괄 되게 유지되며, 테스트 코드의 가독성을 높일 수 있습니다.

### 독자 분석

#### 1) 독자 역할 가정하기

이 글을 읽으려는 독자는 어떤 사람일까요?

- 테스트 코드 작성에 어려움을 겪는 개발자: 특히, 복잡한 시스템에서 다양한 데이터 조합을 테스트해야 하는 경우 어려움을 느끼는 개발자들이 주요 타겟입니다.
- 테스트 코드 품질 향상에 관심 있는 개발자: 테스트 커버리지를 높이고, 테스트 코드의 가독성을 향상시키고 싶어하는 개발자들이 해당됩니다.
- 테스트 코드 작성시 Given 작성시 코드량이 많아 문제를 겪고 있는 개발자: Given 절에서 많은 설정과 초기화 작업이 필요해 코드가 복잡해지는 경우, 이를 간소화하고 효율적으로 작성하는 방법을 찾고자 하는 개발자들이 주요 타겟입니다.

#### 2) 독자 니즈 정의하기

이 글을 읽고 독자는 무엇을 얻고 싶을까요?

- 테스트 코드를 작성시 다양한 테스트 케이스를 작성 해야할 하는데 이를 효율적으로 작성하는 방법을 배우고 싶어하는 독자
- Given 절에서 코드량이 많아져 가독성이 떨어지는 문제를 해결하고 싶어하는 독자

#### 3) 독자가 배워야 할 내용

이 글을 읽고 독자가 얻어 갔으면 하는 건 무엇인가요?

- 테스트 코드에는 테스트 코드에서 의도 하고자 하는 컨텍스트에 집중 할 수 있도록 Given 절을 간소화 하는 방법
- 작은 단위에서 큰 단위로 로직을 작성할 때 작은 단위 부터 작성한 테스트 코드의 Given 절이 큰 단위의 테스트에도 도움이 되어 스노우볼을 굴려나가는 방법
- 차근 차근 스노우볼을 굴려 내가 어렵게 작성한 테스트 코드들이 서로 연결되어 큰 단위의 테스트 코드를 작성하게 되며, 이로써 선순환 구조를 갖게 된다는 점

## 2. 목차 정리

# 실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 3: Given 지옥에서 벗어나기 - 스노우볼을 굴려라

## 시작하며

- 자기소개
- 이전 실무에서 적용하는 테스트 코드 작성 방법과 노하우 시리즈에 대한 설명
- 이번 Part 3: Given 지옥에서 벗어나기 - 스노우볼을 굴려라의 목적 설명
    - 산덤이 처럼 쌓여 있는 Given 절의 테스트 코드를 보고 있으면, 이게 무슨 테스트 코드인지 알기 어려운 문제
    - 작은 단위의 코드와 테스트 코드를 작성 하고 연결된 큰 단위의 테스트 코드를 작성할 때 Given 절을 재사용 하지 못하는 경우 문제
    - 이번 Part 3에서는 이런 문제를 해결 하고자 함

## Given 지옥 탈출: 효과적인 테스트 코드 작성 가이드

**Given 지옥**이란 테스트 코드에서 **Given 절**이 지나치게 길어지고 복잡해져 테스트 코드의 가독성이 저하되고 유지보수가 어려워지는 현상을 의미합니다. 이는 테스트 코드 작성 생산성을 저하시키고, 테스트 코드 자체의 신뢰도를 떨어뜨리는 주요 원인이 됩니다.

### Given 절로 인해 테스트 코드의 의도가 모호해지는 문제

* **Given 절의 남발:** 테스트 시나리오와 직접적인 관련이 없는 불필요한 데이터를 준비하는 경우가 많습니다.
* **테스트 코드 가독성 저하:** Given 절이 길어질수록 테스트 코드 전체의 가독성이 떨어져 테스트 코드의 의도를 파악하기 어려워집니다.
* **유지보수 어려움:** 테스트 대상 코드가 변경될 때마다 Given 절도 함께 수정해야 하는 번거로움이 발생합니다.

### Given 절 재사용의 어려움

* **Given 절 중복:**
    * 유사한 테스트 케이스에서 동일한 Given 절을 반복적으로 작성해야 합니다.
* **멀티 모듈 환경:**
    * 다른 모듈에서 재사용 가능한 Given 절을 만들기 어렵습니다.
    * 예를 들어 도메인 모듈의 테스트 코드를 작성 했다면 해당 도메인을 기반으로 서비스 레이어 테스트 코드를 작성 해야하는데 이때 도메인 모듈의 given절을 그대로 사용할 수 없어 어려움이 있습니다.
* **테스트 데이터 관리의 어려움:**
    * 테스트 데이터의 일관성을 유지하고 관리하기가 어렵습니다.
    * 도메인 객체에 신규 필드가 추가됐을 경우 관련해서 모든 테스트 코드 Given 절을 수정해야 하는 번거로움이 있습니다. 또 멀티 모듈 환경의 경우 이런 번거로움이 더욱 심각해 집니다.

### 외부 시스템 연동 테스트의 복잡성

* **Mock Server 설정의 어려움:**
    * 외부 시스템과의 통신을 모방하기 위한 Mock Server 설정이 복잡하고 시간이 많이 소요됩니다.
    * 예를들어 외부 HTTP 통신을 통해 환율 정보를 11-01 ~ 11-31 까지 필요한 경우 Mock 서버를 통해서 가져오는 것의 어려움, 결과적으로 다양한 케이스에 대해서 작성의 어려움으로 이어진다.
* **다양한 테스트 케이스를 위한 데이터 준비:** 각 테스트 케이스마다 다른 Mock 데이터를 준비해야 하는 번거로움이 있습니다.
* **테스트 코드의 결합도 증가:** 외부 시스템과의 의존성이 높아져 테스트 코드의 독립성이 떨어집니다.

### Given 지옥 탈출: 스노우볼을 굴려라

**java-test-fixtures**를 활용하여 Given 절을 간소화하고 테스트 코드의 품질을 향상시킬 수 있습니다.

* **java-test-fixtures 소개:**
    * 테스트 데이터 생성 및 관리를 위한 도구
    * 테스트 코드 간의 의존성 관리
    * 테스트 케이스 재사용성 향상

* **java-test-fixtures 기반 Given 절 작성 팁:**
    * **default argument 활용:** 불필요한 데이터는 기본값으로 설정하여 테스트 코드를 간결하게 만듭니다.
    * **필요한 데이터만 노출:** 테스트에 필요한 데이터만 명확하게 노출하여 가독성을 높입니다.
    * **작은 단위의 테스트 코드 작성:** 재사용 가능한 작은 단위의 테스트 코드를 작성하여 Given 절을 공유합니다.

* **멀티 모듈 환경에서의 테스트 모듈 공유:**
    * 공통 테스트 데이터 및 유틸리티를 별도의 모듈로 분리하여 다른 모듈에서 재사용합니다.

### Given 절 개선을 통한 테스트 코드 품질 향상

* **코드량 감소 및 가독성 향상:**
    * 불필요한 코드를 제거하고 가독성을 높여 테스트 코드를 이해하기 쉽게 만듭니다.
* **테스트 케이스 재사용성 증가:**
    * Given 절을 재사용하여 테스트 케이스 작성 시간을 단축하고 유지보수를 용이하게 합니다.
* **외부 시스템 연동 테스트 간소화:**
    * Mock Server 설정을 간소화하고 테스트 데이터 관리를 효율적으로 수행합니다.

### 스노우볼 효과를 통한 지속적인 개선

* **작은 단위의 테스트 코드 작성:**
    * 재사용 가능한 테스트 케이스를 생성하여 테스트 코드의 안정성을 확보합니다.
* **테스트 코드 공유:**
    * 팀 내 테스트 코드 품질 향상
    * 개발 생산성 증대
* **지속적인 개선:**
    * 테스트 코드 리팩토링
    * 새로운 테스트 케이스 추가

**결론**

- 작은 단위의 테스트 코드를 작성하고, 이를 연결하여 큰 단위의 테스트 코드를 작성하며, 내가 이전의 작성한 테스트 코드로 인해 연결돤 다른 코드들의 테스트 코드들이 점차 쉬워 져야 하며 이러한 명확한 어드밴티지가 있어야 테스트 코드가 귀찮아도 작성해야하는 도구가 아닌, 실질적으로 나에게 도움이 되는 도구로 인식하게 되며 이로 인한 스노우볼을 계속 굴려 나가야 한다.
