# Mermaid: 코드로 그리는 다이어그램

Mermaid는 간단한 텍스트 형식의 코드를 통해 다양한 다이어그램을 생성할 수 있는 강력한 도구입니다. 특히, 개발자들에게 큰 이점을 제공하며, 프로젝트 문서화 작업에서 필수적인 역할을 할 수 있습니다. 이번 글에서는 Mermaid가 어떤 이점을 가지고 있는지, 그리고 이를 활용하여 프로젝트 문서화를 어떻게 개선할 수 있는지에 대해 설명드리겠습니다.

## 코드 베이스 관리의 큰 이점

Mermaid의 가장 큰 이점 중 하나는 **코드 베이스 관리의 용이성**입니다. 다이어그램을 코드로 관리하면 다음과 같은 장점이 있습니다:

- **유지보수성**: 다이어그램이 코드로 작성되어 있기 때문에, 코드와 함께 버전 관리 시스템에서 추적할 수 있어 변경 이력을 쉽게 관리할 수 있습니다. 새로운 기능 추가나 수정이 있을 때 다이어그램을 업데이트하는 것도 매우 간단합니다.
- **수정의 용이성**: GUI로 그려진 다이어그램은 수정이 어렵고 시간이 많이 걸리지만, Mermaid는 텍스트 기반이므로 코드를 수정하는 것만으로 빠르게 다이어그램을 변경할 수 있습니다.
- **검색의 용이성**: 코드로 작성된 다이어그램은 텍스트 파일이기 때문에 프로젝트 내에서 검색이 가능하여, 유지보수 작업을 더 쉽게 할 수 있습니다.

## Markdown에서 직접 사용 가능

Mermaid는 **Markdown 프리뷰 플러그인**을 제공하기 때문에, Markdown 문서 내에서 다이어그램을 바로 작성하고 확인할 수 있습니다. 이는 프로젝트의 **README 파일**에 다이어그램을 포함시켜 팀원들이 쉽게 접근할 수 있도록 만들어줍니다. 문서화를 프로젝트의 중심에서 관리할 수 있어 **프로젝트의 가독성과 접근성을 크게 향상**시킬 수 있습니다.

## 다양한 다이어그램 형식 제공

Mermaid는 다양한 다이어그램 형식을 제공합니다. 몇 가지 주요 다이어그램 유형은 다음과 같습니다:

### Flowchart

프로세스 흐름을 나타내는 데 적합합니다.

```mermaid
flowchart TD
    A[Christmas] -->|Get money| B(Go shopping)
    B --> C{Let me think}
    C -->|One| D[Laptop]
    C -->|Two| E[iPhone]
    C -->|Three| F[fa:fa-car Car]
```

Mermaid 코드 (Flowchart):
```text
flowchart TD
    A[Christmas] -->|Get money| B(Go shopping)
    B --> C{Let me think}
    C -->|One| D[Laptop]
    C -->|Two| E[iPhone]
    C -->|Three| F[fa:fa-car Car]
```

### Class Diagram

객체지향 프로그램의 클래스 구조를 표현할 수 있습니다.

```mermaid
classDiagram
    Animal <|-- Duck
    Animal <|-- Fish
    Animal <|-- Zebra
    Animal : +int age
    Animal : +String gender
    Animal: +isMammal()
    Animal: +mate()
    class Duck{
      +String beakColor
      +swim()
      +quack()
    }
    class Fish{
      -int sizeInFeet
      -canEat()
    }
    class Zebra{
      +bool is_wild
      +run()
    }
```

Mermaid 코드 (Class Diagram):
```text
classDiagram
    Animal <|-- Duck
    Animal <|-- Fish
    Animal <|-- Zebra
    Animal : +int age
    Animal : +String gender
    Animal: +isMammal()
    Animal: +mate()
    class Duck{
      +String beakColor
      +swim()
      +quack()
    }
    class Fish{
      -int sizeInFeet
      -canEat()
    }
    class Zebra{
      +bool is_wild
      +run()
    }
```

### Sequence Diagram (Zenuml)

Zenuml을 사용하여 시퀀스 다이어그램을 쉽게 표현할 수 있습니다.

```mermaid
zenuml
title Order Service
@Actor Client #FFEBE6
@Boundary OrderController #0747A6
@EC2 <<BFF>> OrderService #E3FCEF
group BusinessService {
  @Lambda PurchaseService
  @AzureFunction InvoiceService
}

@Starter(Client)
// `POST /orders`
OrderController.post(payload) {
  OrderService.create(payload) {
    order = new Order(payload)
    if(order != null) {
      par {
        PurchaseService.createPO(order)
        InvoiceService.createInvoice(order)      
      }      
    }
  }
}
```

Zenuml 코드 (Sequence Diagram):
```text
title Order Service
@Actor Client #FFEBE6
@Boundary OrderController #0747A6
@EC2 <<BFF>> OrderService #E3FCEF
group BusinessService {
  @Lambda PurchaseService
  @AzureFunction InvoiceService
}

@Starter(Client)
// `POST /orders`
OrderController.post(payload) {
  OrderService.create(payload) {
    order = new Order(payload)
    if(order != null) {
      par {
        PurchaseService.createPO(order)
        InvoiceService.createInvoice(order)      
      }      
    }
  }
}
```

### XYChart

XYChart를 사용하여 매출 데이터를 시각화할 수 있습니다.

```mermaid
xychart-beta
title "Sales Revenue"
x-axis [jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec]
y-axis "Revenue (in $)" 4000 --> 11000
bar [5000, 6000, 7500, 8200, 9500, 10500, 11000, 10200, 9200, 8500, 7000, 6000]
line [5000, 6000, 7500, 8200, 9500, 10500, 11000, 10200, 9200, 8500, 7000, 6000]
```

XYChart 코드:
```text
xychart-beta
title "Sales Revenue"
x-axis [jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec]
y-axis "Revenue (in $)" 4000 --> 11000
bar [5000, 6000, 7500, 8200, 9500, 10500, 11000, 10200, 9200, 8500, 7000, 6000]
line [5000, 6000, 7500, 8200, 9500, 10500, 11000, 10200, 9200, 8500, 7000, 6000]
```

### Block Diagram

여러 블록을 사용하여 시스템의 구성 요소를 표현할 수 있습니다.

```mermaid
block-beta
    columns 3
    doc>"Document"]:3
    space down1<[" "]>(down) space

    block:e:3
        l["left"]
        m("A wide one in the middle")
        r["right"]
    end
    space down2<[" "]>(down) space
    db[("DB")]:3
    space:3
    D space C
    db --> D
    C --> db
    D --> C
    style m fill:#d6d,stroke:#333,stroke-width:4px

```

Block Diagram 코드:
```text
block-beta
    columns 3
    doc>"Document"]:3
    space down1<[" "](down) space
    
    block:e:3
          l["left"]
          m("A wide one in the middle")
          r["right"]
    end
    space down2<[" "](down) space
    db[("DB")]:3
    space:3
    D space C
    db --> D
    C --> db
    D --> C
    style m fill:#d6d,stroke:#333,stroke-width:4px
```

이 외에도 **시퀀스 다이어그램**, **상태도**, **피에조 차트** 등 다양한 다이어그램을 제공하여 개발뿐 아니라 프로젝트 관리, 사용자 경험 디자인까지 다양한 분야에서 활용할 수 있습니다.

## Mermaid Live: 실시간 다이어그램 작성

Mermaid는 **Mermaid Live Editor**라는 웹 기반 도구를 제공하여 실시간으로 다이어그램을 작성하고 미리 볼 수 있습니다. 이 도구를 사용하면 빠르게 다이어그램을 시각화할 수 있으며, 다양한 **템플릿**도 제공되어 누구나 쉽게 시작할 수 있습니다. 복잡한 다이어그램도 빠르게 그릴 수 있기 때문에 협업 시에 유용하게 활용될 수 있습니다.

## 마무리

Mermaid는 코드 기반의 다이어그램 도구로서, 유지보수성, 수정의 용이성, 그리고 프로젝트 문서화의 접근성을 크게 향상시킵니다. Markdown과 통합하여 프로젝트 문서에 쉽게 다이어그램을 포함시킬 수 있으며, 다양한 다이어그램 형식을 통해 개발 과정에서 발생하는 다양한 시각적 요구를 충족시킬 수 있습니다. Mermaid Live를 통해 실시간으로 다이어그램을 작성하고 협업할 수 있는 기능도 매우 유용합니다.

프로젝트 문서화 작업에서 Mermaid를 활용해 보세요. 코드와 문서의 통합을 통해 더 나은 협업과 유지보수를 경험할 수 있을 것입니다.

## 출처

* [Mermaid Live Editor]\([https://mermaid.live/edit](https://mermaid.live/edit))
