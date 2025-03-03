from locust import HttpUser, task, constant
import random


class OrderApiTest(HttpUser):
    # wait_time = between(1, 2)  # 각 요청 사이의 대기 시간(초)
    wait_time = constant(0.1)  # 모든 요청 사이에 3초의 고정된 대기 시간 설정
    # wait_time = constant_throughput(1)  # 초당 최대 1번 작업 실행으로 조정하여 대기 시간 설정
    # wait_time = constant_pacing(10)  # 최소 10초 간격으로 작업 실행이 보장되도록 대기 시간 설정
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
            "/api/v1/members?page=200&size=1",
            headers = { "Content-Type": "application/json" },
            params = params,
            name = "/api/v1/members"
        )

#     @task
#     def getShop(self):
#         order_params = [
#             ("PENDING", "2023-01-01-2023-01-31"),
#             ("SHIPPED", "2023-02-01-2023-02-28"),
#             ("COMPLETED", "2023-03-01-2023-03-31")
#         ]
#         query_params = random.choice(order_params)
#         params = {
#             "status": query_params[0],
#             "date": query_params[1]
#         }
#         self.client.get(
#             "/api/v1/shops",
#             headers = { "Content-Type": "application/json" },
#             params = params,
#             name = "/api/v1/shops"
#         )
