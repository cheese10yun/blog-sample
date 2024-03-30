# Locust

## Locust 란?

Locust는 오픈 소스 부하 테스트 도구로, 사용자가 Python으로 시나리오를 작성하여 웹 애플리케이션의 성능을 측정할 수 있게 해줍니다. 이 도구는 이벤트 기반 모델을 사용하여 수천 명의 사용자를 시뮬레이션하고, 웹사이트나 API 서버에 대한 부하 테스트를 실시간으로 실행할 수 있습니다. Locust는 사용자 친화적인 웹 인터페이스를 제공하여 테스트의 진행 상황을 모니터링하고, 결과를 분석할 수 있게 합니다.

## Locust 설치

```bash
$ pip install locust
```

pip를 통해서 `locust`를 설치합니다.

```pycon
from locust import HttpUser, task, constant
import random

class HelloWorldUser(HttpUser):
    wait_time = constant(1)  # 모든 요청 사이에 1초의 고정된 대기 시간 설정
    host = "http://localhost:8080"  # 테스트 대상 호스트 주소 지정

    @task
    def hello_world(self):
        self.client.get(
            "/api/v1/orders",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/orders"
        )
```

간단하게 HTTP GET 요청을 보내는 Locust 스크립트를 작성해보았습니다. `HttpUser` 클래스를 상속받아 사용자 클래스를 정의하고, `@task` 데코레이터를 사용하여 테스트 함수를 정의합니다. `self.client.get` 메서드를 사용하여 GET 요청을 보내고, `params`와 `name` 매개변수를 사용하여 요청 파라미터와 요청 이름을 설정합니다.

```bash
$ locust -f <file_name.py>
```

`locust` 명령어를 사용하여 Locust 스크립트를 실행합니다. `-f` 옵션을 사용하여 실행할 스크립트 파일을 지정합니다. 실행 후 웹 브라우저에서 `http://localhost:8089`로 접속하여 Locust 웹 인터페이스를 확인할 수 있습니다. 만약 파일명을 `locustfile.py` 으로 지정했다면 `locust` 명령어만 수행하면 됩니다.

## Locust의 특징

API 서버 성능 테스트 도구로 많이 알려진 도구들 중 JMeter와 nGrinder는 강력한 기능과 세밀한 설정 옵션으로 널리 사용되고 있습니다. 이러한 도구들은 복잡한 시나리오를 구현하고 대규모의 부하 테스트를 수행할 수 있는 뛰어난 능력을 가지고 있습니다. 그러나 이러한 기능성과 다양성이 더 간편하고 신속한 테스트 실행을 선호하는 사용자들에게는 설정과 실행 과정에서의 복잡성으로 인해 접근성이 떨어질 수 있습니다.

이에 비해 Locust는 사용자 친화적인 API 서버 성능 테스트 도구로, 그 사용의 용이성과 편리함에서 큰 장점을 가지고 있습니다. 특히, Locust는 Python으로 테스트 스크립트를 작성하기 때문에, 기존에 Python을 사용해본 경험이 있는 개발자라면 누구나 쉽게 접근할 수 있습니다. 이는 테스트 스크립트의 작성과 수정을 매우 간단하게 만들어 줍니다.

Locust의 설치 및 운용의 용이성은 테스트 프로세스를 대폭 단순화시킵니다. 몇 가지 간단한 명령어로 Locust를 설치할 수 있으며, 별도의 복잡한 설정 없이도 로컬 환경에서 바로 부하 테스트를 시작할 수 있습니다. 이러한 점은 개발 초기 단계에서 빠르게 API 성능을 평가하고자 할 때 특히 유용합니다.

또한, Locust로 작성된 테스트 스크립트는 Python 코드로 구성되어 있기 때문에, GitHub과 같은 원격 저장소에 코드를 올려두면 팀원이나 다른 개발자들이 언제든지 손쉽게 해당 스크립트를 클론하고, 필요한 부하 테스트를 즉시 실행할 수 있습니다. 이는 협업 환경에서의 테스트 과정을 매우 효율적으로 만들어 줍니다. 팀원들은 최신의 테스트 스크립트를 공유받아, 실시간으로 테스트 결과를 확인하고 성능 개선 작업을 진행할 수 있습니다.

이와 더불어, Locust는 실시간으로 테스트 결과를 웹 인터페이스를 통해 제공합니다. 사용자는 웹 브라우저를 통해 테스트의 진행 상황을 모니터링하고, 성능 지표를 실시간으로 확인할 수 있습니다. 이는 테스트 과정에서의 직관적인 데이터 분석과 신속한 의사 결정을 가능하게 합니다.

요약하자면, Locust는 설치와 사용이 쉬우며, 로컬 환경에서의 빠른 구동 능력으로 인해 개발자가 신속하게 성능 테스트를 수행할 수 있게 해줍니다. Python 기반의 스크립트 작성 방식은 깃헙과 같은 원격 저장소를 통한 협업에 매우 유리하며, 이로 인해 개발 프로세스의 효율성과 속도를 크게 향상시킬 수 있습니다.

## Locust Dashboard

### Start new load test

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/locust/imag/locust_005.png)

Start new load test 버튼을 클릭하여 새로운 부하 테스트를 시작할 수 있습니다. 이 버튼을 클릭하면 다음과 같은 옵션을 설정할 수 있는 팝업 창이 나타납니다.

* Number of Users 
  * 정의: 테스트에서 동시에 시뮬레이션할 가상 사용자의 총 수입니다. 
  * 목적: 애플리케이션이 동시에 처리할 수 있는 사용자 수를 설정하여, 애플리케이션의 동시 사용자 처리 능력을 테스트합니다.
* Ramp Up (일반적으로 Ramp Up 시간을 의미하며, Locust에서는 Spawn Rate으로 표현될 수 있음)
  * 정의: 테스트 시작부터 설정된 전체 사용자 수에 도달하기까지의 시간 또는 사용자가 점진적으로 증가하는 속도입니다. 
  * 목적: 사용자 수가 점진적으로 증가하는 상황을 모델링하여, 애플리케이션이 사용자 증가 속도에 어떻게 대응하는지 평가합니다.

* Number of total users to simulate: 시뮬레이션할 총 사용자 수를 설정합니다.
* Hatch rate (users spawned/second): 초당 생성할 사용자 수를 설정합니다.

### Statistics

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/locust/imag/locust_001.png)

Statistics 섹션은 현재 진행 중인 테스트의 실시간 통계를 제공합니다. 이 테이블에는 각 요청 유형별로 세분화된 데이터가 포함되어 있으며, 다음과 같은 정보를 확인할 수 있습니다:

* Name: 요청의 이름이나 경로를 나타냅니다.
* requests: 해당 요청이 몇 번 실행되었는지 보여줍니다.
* failures: 요청 실패 횟수를 나타냅니다.
* Median response time: 응답 시간의 중앙값(밀리초 단위)을 보여줍니다. 이는 모든 요청 중간에 위치하는 응답 시간을 의미합니다.
* Average response time: 평균 응답 시간을 나타냅니다.
* Min/Max response time: 관찰된 최소 및 최대 응답 시간입니다.
* Request per second: 초당 요청 수를 보여줍니다.

### Charts

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/locust/imag/locust_002.png)

Charts 섹션은 테스트 동안 수집된 데이터를 그래프 형태로 시각화합니다. 이 차트는 테스트의 진행에 따라 동적으로 업데이트되며, 주로 다음과 같은 정보를 제공합니다.

* Requests per second (RPS): 시간에 따른 초당 요청 수의 변화를 나타냅니다.
* Response times: 다양한 응답 시간(평균, 최소, 최대)을 시간 경과에 따라 보여줍니다.
* Number of users: 시간에 따른 사용자 수의 변화를 보여줍니다.

### Download Data

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/locust/imag/locust_003.png)

Download Data 메뉴는 테스트 결과를 다운로드할 수 있는 옵션을 제공합니다. 테스트의 통계 및 차트 데이터를 CSV 파일 형식으로 내보낼 수 있으며, 이는 보다 심층적인 분석이나 문서화, 또는 다른 팀 구성원과의 공유를 위해 사용될 수 있습니다.

![](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/locust/imag/locust_004.png)

특히 Download the Report 메뉴를 통해 테스트 결과를 HTML 형식으로 다운로드할 수 있습니다. 이 HTML 리포트는 테스트의 요약 정보와 세부 통계, 그리고 차트 데이터를 포함하고 있으며, 테스트 결과를 보다 시각적으로 표현할 수 있습니다. 이 리포트는 테스트 결과를 문서화하거나, 다른 팀원과 공유할 때 유용하게 사용될 수 있습니다.

## 정리

Locust는 사용의 용이성과 빠른 테스트 실행 능력으로 개발자에게 탁월한 부하 테스트 도구를 제공합니다. Python 기반으로 간단한 설치와 함께, 누구나 쉽게 테스트를 시작할 수 있으며, 이는 빠른 성능 평가와 적시의 개선으로 이어집니다.

원격 저장소를 통한 테스트 스크립트 공유는 팀 내 협업을 강화하며, 모든 팀원이 필요한 테스트를 쉽게 실행할 수 있게 합니다. 이러한 접근성은 테스트의 재사용성을 높이고, 개발 프로세스의 효율성을 개선합니다.

Locust는 단순한 테스트 도구를 넘어, 성능 모니터링과 개선을 위한 협업의 핵심이 됩니다. 이를 통해, 사용자에게 최적의 경험을 제공하는 애플리케이션을 구축할 수 있습니다.
