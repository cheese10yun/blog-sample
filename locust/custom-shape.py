from locust import HttpUser, task, TaskSet, constant, LoadTestShape
import random


class Advance(HttpUser):
    wait_time = constant(1)  # 모든 요청 사이에 3초의 고정된 대기 시간 설정
    host = "http://localhost:8080"  # 테스트 대상 호스트 주소 지정

    @task
    def getOrder(self):
        order_params = [
            ("PENDING", "2023-01-01-2023-01-31"),
            ("SHIPPED", "2023-02-01-2023-02-28"),
            ("COMPLETED", "2023-03-01-2023-03-31")
        ]
        query_params = random.choice(order_params)
        params = {
            "status": query_params[0],
            "date": query_params[1]
        }
        self.client.get(
            "/api/v1/orders",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/orders"
        )

    @task
    def getShop(self):
        order_params = [
            ("PENDING", "2023-01-01-2023-01-31"),
            ("SHIPPED", "2023-02-01-2023-02-28"),
            ("COMPLETED", "2023-03-01-2023-03-31")
        ]
        query_params = random.choice(order_params)
        params = {
            "status": query_params[0],
            "date": query_params[1]
        }
        self.client.get(
            "/api/v1/shops",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/shops"
        )



# 사용자 정의 부하 모양을 정의하는 LoadTestShape 클래스
class CustomShape(LoadTestShape):
    time_limit = 60  # 부하 테스트의 총 시간 한계 설정
    spawn_rate = 20  # 초당 새로운 사용자를 생성하는 속도 설정

    def tick(self):
        run_time = self.get_run_time()  # 현재 실행 시간 가져오기

        if run_time < self.time_limit:
            # 실행 시간에 따라 사용자 수 증가
            user_count = run_time // 10
            return (user_count, self.spawn_rate)

        return None  # 시간 한계를 넘으면 테스트 종료