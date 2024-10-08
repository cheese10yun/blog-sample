# 실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 3: Given 지옥에서 벗어나기 - Given 재사용

## 시작하며

지난 시리즈 **실무에서 적용하는 테스트 코드 작성 방법과 노하우 Part 1**에서는 효율적으로 Mock 테스트를 진행하는 방법에 대해 살펴봤고, **Part 2: 테스트 코드로부터 피드백 받기**에서는 테스트 코드에서 주는 피드백을 통해 어떻게 구현 코드를 개선해 나갈 수 있는지에 대해 알아봤습니다. 이어서 **Part 3**에서는 객체 기반으로 테스트 코드를 작성할 때 겪는 어려움과 이를 극복하는 방법에 대해 다뤘습니다. 이번 글에서는 **Part 3 - Given 지옥에서 벗어나기: Given 재사용**에 대해 알아보겠습니다.

이번 글에서는 테스트 코드 작성 시 빈번히 등장하는 ‘Given 지옥’에서 벗어나는 방법에 대해 다뤄보겠습니다. 복잡한 비즈니스 로직을 테스트할 때, 때로는 매우 복잡한 Given 절이 필요하게 되는데, 이를 매번 새로 작성하는 대신 재사용할 수 있다면 테스트 코드를 더 효율적으로 관리할 수 있습니다. Given 절을 재사용하면 코드의 중복을 줄일 수 있을 뿐 아니라, 넓은 테스트 커버리지를 확보하여 더욱 의미 있는 테스트를 작성할 수 있게 됩니다. 이번 글에서는 **Given 재사용**의 방법과 그로 인한 장점들에 대해 구체적으로 알아보겠습니다.


## 목차 
* Given 재사용 해야하는 이유
* 멀티 모듈 환경 강조
* 재사용 못하는 경우 코드의 복잡성
* 테스트의 주요 관심사에 집중
* Mock 테스트 코드 작성시에도 간결화
* java-test-fixtures 소개
* Fixture 작성 팁

---------

### java-test-fixtures 플러그인을 사용한 Given 재사용 방법

테스트 코드 작성 시 Given 절이 지나치게 복잡해지면, 코드의 가독성이 떨어지고 관리가 어려워질 수 있습니다. 이를 해결하기 위한 방법 중 하나로 **java-test-fixtures** 플러그인을 활용할 수 있습니다. 이 플러그인을 사용하면 `testFixtures`라는 별도의 디렉토리가 생성되며, 여기에 `DomainFixture` 클래스를 작성하여 재사용 가능한 객체 생성을 관리할 수 있습니다.

예를 들어, 주문(Order) 객체를 생성하는 메서드를 `testFixtures` 디렉토리에 정의하면, 해당 객체는 여러 모듈에서 공통적으로 접근하여 사용할 수 있습니다. 심지어 배치 모듈과 같은 다른 모듈에서도 동일한 `Order` 객체를 재사용할 수 있어, 코드의 일관성과 관리 효율성이 크게 향상됩니다.


### Fixture 객체 작성 팁

Fixture 객체를 작성할 때는 기본 값(Default Value)을 설정하는 것이 중요합니다. 테스트 시 모든 필드를 반드시 사용할 필요는 없기 때문에, 필요한 필드만 초기화하고 나머지 필드는 기본 값으로 설정하는 것이 좋습니다.

예를 들어, 매월 1일에만 사용 가능한 할인 쿠폰을 적용하는 테스트 코드를 작성한다고 가정해보겠습니다. 이 경우 주문 객체에서 쿠폰 적용에 필요한 필드는 주문 날짜와 금액 정도로 한정되며, 나머지 필드는 불필요합니다. 따라서 테스트 코드에서 주문 날짜와 금액만 명시하고, 나머지 필드는 기본 값으로 처리함으로써 테스트의 주요 관심사를 명확하게 드러낼 수 있습니다. 이렇게 하면 불필요한 코드로 인해 테스트의 의도가 가려지지 않으며, 테스트가 간결해집니다.


### 과도한 Mocking의 문제와 해결 방법

외부 인프라에 의존하는 코드를 테스트할 때는 필연적으로 Mocking 처리를 하게 됩니다. 하지만 너무 많은 부분을 Mocking하다 보면 테스트가 복잡해지고, 주요 관심사가 흐려질 수 있습니다.

예를 들어, `PartnerClient`를 의존하는 서비스 메서드를 테스트한다고 가정해보겠습니다. 이때 HTTP Mock 서버를 사용하여 외부 통신을 모의(Mock)할 수 있지만, 프로젝트 전체에 걸쳐 `PartnerClient`를 사용하는 구간이 많다면 모든 테스트에 Mock 서버를 구성해야 하는 문제가 발생합니다. 이렇게 과도한 Mocking은 테스트 코드가 장황해지고, 테스트의 주요 의도를 파악하기 어렵게 만듭니다.

해결책으로는 특정 구간에서만 Mocking을 집중적으로 사용하고, 그 외 부분은 Mock 객체를 활용하는 방식이 있습니다. 예를 들어, 외부 서버로부터 환율 정보를 조회하는 코드를 테스트할 때, 특정 날짜에 대한 환율 정보를 미리 설정된 Mock 객체를 통해 제공함으로써, HTTP Mock 서버를 대체할 수 있습니다. 이렇게 하면 코드가 간결해지고 테스트의 의도는 더욱 명확해집니다.


### 운영 코드에 사용되지 않는 검증용 코드 문제

테스트 코드에서만 사용되고 운영 코드에서는 사용되지 않는 검증용 코드가 존재할 수 있습니다. 이러한 검증 코드가 테스트의 편의성을 높이긴 하지만, 구현 코드에 추가로 작성해야 하는 경우가 발생할 수 있습니다. 만약 조회 로직이 따로 없다면, 테스트만을 위한 검증 코드를 추가로 작성해야 할지 고민이 됩니다. 또한, 조회 로직이 추가되더라도 요구사항 변화에 따라 로직이 변경될 수 있어 기존 테스트 코드에도 영향을 미칠 수 있습니다.

이 문제를 해결하기 위해 검증 로직은 테스트에서만 사용 가능한 로직으로 분리하여, 필요한 조회 검증을 수행하도록 구성하는 것이 좋습니다. 이를 통해 테스트의 유지보수성과 관리가 수월해집니다.


### SpringBootTestSupport 활용과 JPAQueryFactory 주입

테스트에 필요한 편리한 기능들을 제공하기 위해 `SpringBootTestSupport` 클래스를 작성하고, 여기에 `JPAQueryFactory`를 주입받아 사용할 수 있습니다. 이는 **Application Context 재사용**을 위한 상위 클래스로 만들어 테스트 코드의 반복을 줄여줍니다.

테스트 코드에서 직접 `JPAQueryFactory`를 기반으로 조회 코드를 작성할 수 있으며, 필요할 때마다 간단한 조회 검증 로직을 추가할 수 있습니다. 이를 통해 영속화된 데이터를 편리하게 검증하고, 리포지토리 의존 없이도 테스트 데이터 설정을 간단하게 처리할 수 있습니다. 테스트 코드 작성 시 의존성을 주입받는 흐름을 방해하지 않으며, 코드 작성의 흐름이 더욱 원활해집니다.


### 마무리

테스트 코드 작성에서 Given 절을 재사용하고, 과도한 Mocking을 피하며, 검증 로직을 적절하게 분리하는 방법은 테스트의 가독성과 유지보수성을 높이는 데 큰 도움이 됩니다. Fixture 객체를 잘 활용하고, SpringBootTest의 지원 기능을 적극적으로 사용한다면 효율적인 테스트 환경을 구성할 수 있을 것입니다.


이렇게 정리한 글은 블로그 독자가 쉽게 읽을 수 있도록 내용의 흐름을 자연스럽게 유지하며, 실무적인 팁과 전략을 구체적으로 제시하는 방식입니다.