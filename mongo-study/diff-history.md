
* 블로그를 작성할꺼야 내가 작성하고자하는 부분을 강조해서 정리하면 너가 블로그 초안을 작성하는 거야
* 포스팅 내용은 히스토리 추적을 남기는 부분이야, 예를들어 주문에 대한 히스토리를 남겨야 한다고 가정하자 히스토리를 남길때 데이터가 어떻게 변경됐는지가 명확하게 남기는게 중요할 수있다.
* 만약 주문 내역에서 주소필드만 변경됐다면 데이터를 확인하는 운영자 입장에서는 어떤 데이터가 어떻게 변경됐는지 파악이 가능하다. 또 예를 들면 가맹점에 대한 수수료율을 변경하기 위해 승인권자가 어떤 데이터를 어떻게 변경됐는지 필드 단위로 확인 하는 그런 필드 단위 추적 시스템이 있을 수 있다. 이런 경우 가 적절할 수 있다.

예를 들어 보자

```json
{
  "order_id": "ORD123456",
  "customer": {
    "customer_id": "CUST7890",
    "name": "홍길동",
    "contact": {
      "email": "hong@example.com",
      "phone": "010-1234-5678",
      "address": {
        "street": "서울특별시 종로구",
        "city": "서울",
        "zip_code": "03000",
        "country": "KR"
      }
    }
  },
  "items": [
    {
      "product": {
        "product_id": "PROD001",
        "product_name": "노트북",
        "category": {
          "main_category": "전자제품",
          "sub_category": "컴퓨터"
        }
      },
      "quantity": 1,
      "price": 1500000
    }
  ],
  "payment": {
    "method": "신용카드",
    "transaction_id": "TXN987654321",
    "status": "완료"
  }
}

```

```json
{
  "order_id": "ORD123456",
  "customer": {
    "customer_id": "CUST7890",
    "name": "홍길동",
    "contact": {
      "email": "hong@example.com",
      "phone": "010-1234-5678",
      "address": {
        "street": "서울특별시 강남구",
        "city": "서울",
        "zip_code": "03000",
        "country": "KR"
      }
    }
  },
  "items": [
    {
      "product": {
        "product_id": "PROD001",
        "product_name": "노트북",
        "category": {
          "main_category": "전자제품",
          "sub_category": "컴퓨터"
        }
      },
      "quantity": 1,
      "price": 1400000
    }
  ],
  "payment": {
    "method": "신용카드",
    "transaction_id": "TXN987654322",
    "status": "완료"
  }
}


```

이런 데이터가 있을때 diff를 추적을 해야하는 경우가 있다. 이런 경우 어떤 필드게 어떻게 변경됐는지 필드 단위로 내역을 저장하고 있다면 좋을 수 있다.

이런 경우 diff를 가장 직관적으로 볼 수 있는 부분이 diff를 확인하는 방법이 있다. 예를 들어 인텔리제이에서도 해당 기능을 diff 기능을 제공한다 

![img_1.png](img_1.png)

위 이미지 처럼 diff에 해당하는 필드를 표시하고 있다. 이런 경우 해당 데이터를 저장하는 방식으로 진행 된다.

그러면 실제 코드 레벨에서 살펴보자
