# Locust 기능

## on_start 및 on_stop 메서드

```python
class OrderApiTest(HttpUser):
    def on_start(self):
        self.client.post("/login")
    
    def on_stop(self):
        self.client.post("/logout")
```

`on_start`와 `on_stop` 메서드는 사용자의 세션 시작과 종료 시 특정 작업을 실행하는 데 사용됩니다. 로그인과 로그아웃 외에도, 사용자가 시나리오를 시작하기 전에 필요한 데이터를 세팅하거나, 시나리오 종료 후 사용한 리소스를 정리하는 데 사용할 수 있습니다. 예를 들어, 시나리오 시작 시 특정 API를 호출하여 필요한 설정을 하거나, 시나리오가 끝난 후 생성된 데이터를 삭제하는 등의 작업이 있을 수 있습니다. 이러한 메서드를 통해 테스트의 사전 준비와 후처리를 자동화할 수 있습니다. `on_start`는 사용자가 시작될 때 호출되며, `on_stop`은 사용자가 종료될 때 호출됩니다. 강제로 loucst를 종료하면 `on_stop` 메서드가 호출되지 않습니다.


## @task를 이용한 API 요청 비율 조정

```python

class Advance(HttpUser):
    ...
    @task(3)
    def getOrder(self):
        ...
        self.client.get(
            "/api/v1/orders",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/orders"
        )

    @task(1)
    def getShop(self):
        ...
        self.client.get(
            "/api/v1/shops",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/shops"
        )

```

[](https://raw.githubusercontent.com/cheese10yun/blog-sample/master/locust/imag/locust-1-1.png)

`@task` 데코레이터는 Locust에서 작업의 실행 빈도나 우선순위를 지정하는 데 사용됩니다. 숫자를 인자로 제공함으로써, 특정 작업이 다른 작업들에 비해 상대적으로 얼마나 자주 실행될지 결정할 수 있습니다. 예를 들어, `@task(3)`은 해당 작업이 같은 TaskSet 내 다른 `@task(1)` 작업보다 세 배 더 많이 실행됨을 의미합니다. 이를 통해 실제 사용자 행동을 더 잘 모방한 부하 테스트 시나리오를 구성할 수 있습니다.

## 순차적 TaskSets로 워크플로우 시뮬레이션

```python
class OrderTaskSet(TaskSet):

    @task
    def getOrder(self):
        ...
        self.client.get(
            "/api/v1/orders",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/orders"
        )

    @task
    def getShop(self):
        ...
        self.client.get(
            "/api/v1/shops",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/shops"
        )

class OrderTask(HttpUser):
    wait_time = constant_pacing(2)  # 최소 10초 간격으로 작업 실행이 보장되도록 대기 시간 설정
    host = "http://localhost:8080"  # 테스트 대상 호스트 주소 지정

    tasks = [OrderTaskSet]
```

순차적 TaskSets를 사용하는 워크플로우 시뮬레이션은 사용자가 실제 애플리케이션을 사용할 때의 행동 순서를 모방하는 데 사용됩니다. 이 방식에서는 TaskSet 클래스 내에서 각각의 `@task` 함수가 사용자의 다음 동작을 시뮬레이션합니다. 이 예제에서는 `OrderTaskSet` 내의 `getOrder`와 `getShop`이 동일한 비율로 실행되며, 사용자는 이 두 작업 사이를 순차적으로, 또는 랜덤으로 전환하면서 진행할 수 있습니다. `constant_pacing` 설정을 통해 각 작업 사이의 실행 간격을 조절함으로써, 실제 사용자 경험에 더 가까운 테스트 환경을 구성할 수 있습니다. 자세한 내용은 [Locust 공식 문서](https://docs.locust.io/en/stable/tasksets.html#tasksets)를 참조하세요.

공식 문서는 정확한 비율의 작업 호출을 달성하기 위해 루프와 제어문 사용을 권장합니다. `@task`를 이용한 간단한 호출 비율 조정은 대략적인 작업 순서에 적합하지만, 정확한 작업 순서가 필요한 경우, 공식 문서의 권장 사항을 따르는 것이 더 바람직합니다.

## 맞춤형 부하 형태 시뮬레이션

```python
class Advance(HttpUser):
    wait_time = constant(1)  # 모든 요청 사이에 3초의 고정된 대기 시간 설정
    host = "http://localhost:8080"  # 테스트 대상 호스트 주소 지정

    @task
    def getOrder(self):
        ...
        self.client.get(
            "/api/v1/orders",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/orders"
        )

    @task
    def getShop(self):
        ...
        self.client.get(
            "/api/v1/shops",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/shops"
        )



# 사용자 정의 부하 모양을 정의하는 LoadTestShape 클래스
class CustomShape(LoadTestShape):
    time_limit = 600  # 부하 테스트의 총 시간 한계 설정
    spawn_rate = 20  # 초당 새로운 사용자를 생성하는 속도 설정

    def tick(self):
        run_time = self.get_run_time()  # 현재 실행 시간 가져오기

        if run_time < self.time_limit:
            # 실행 시간에 따라 사용자 수 증가
            user_count = run_time // 10
            return (user_count, self.spawn_rate)

        return None  # 시간 한계를 넘으면 테스트 종료
```

위 코드는 Locust를 사용한 사용자 정의 부하 테스트 시나리오를 설정하는 예시입니다. 테스트 시작부터 시간이 600초(10분)에 이르기까지 실행 시간에 따라 사용자 수를 점진적으로 증가시킵니다. `tick` 함수는 현재 실행 시간을 기반으로 사용자 수를 결정하고, 실행 시간이 10초마다 사용자 수를 1명씩 증가시키는 로직을 포함하고 있습니다. 시간 한계에 도달하면, 즉 실행 시간이 600초를 초과하면, 테스트는 자동으로 종료됩니다. 이를 통해 초기 단계에서는 부하가 점점 증가하다가 설정된 시간이 지나면 테스트가 종료되는 시나리오를 구현할 수 있습니다.


## 정리

사용자 세션 시작과 종료에 필요한 동작을 자동화하는 `on_start`와 `on_stop` 메서드, 다양한 API 요청의 실행 비율을 조절하는 `@task`, 실제 사용자 워크플로우 시뮬레이션에 유용한 순차적 `TaskSets`, 그리고 테스트 동안 사용자 부하를 동적으로 조절할 수 있는 맞춤형 부하 형태 `CustomShape`에 대해 설명합니다. 이 방법들은 Locust를 활용하여 보다 실제적이고 유연한 성능 테스트를 구현하는 데 도움을 줍니다.