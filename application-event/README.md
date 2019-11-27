# Application Event

스프링에서는 Application Event를 재공해주고 있습니다. `Application Event Publisher`, `Application Event Subscriber` 방식으로 결합도를 낮춰 느슨한 결합 관계를 갖을 수 있습니다.

## 시나리오
![](https://github.com/cheese10yun/TIL/blob/master/assets/event-part-1.png?raw=true)

주문 단계에 (주문, 결제)가 완료되면 주문 완료 이후 로직들이 동작해야 합니다. 대표적으로 

* 장바구니 제거 (장바구니에 등록된 상품 주문시)
* 제품 재고 변경
* 주문 완료 알람 (시스템, 유저 등등)

주문 완료 이후 매우 다양한 작업들이 있고 해당 로직들이 `OrderService`에 모두 구현하는 경우 **강한 결합 관계**를 갖게 되고 변경 및 유지보수에 악영향이 발생하게 됩니다.

```kotlin
class OrderService(
        ... // 필요한 의존성 주입, 강한 관계를 갖는 경우 필요한 의존성 들이 많아진다.
) {

    fun order(): Order {
        orderRepository.save(order) // 주문 저장
        stockService.adjust(order) // 재고 조정
        cartService.remove(order) // 장바구니 제거 (장바구니 상품중 주문 완료된 상품 제거)
        return order
    }
}
```
`OrderService` 객체에 의존성 주입받아야 하는 항목들이 많아지는 것이 대표적인 외부와 결합관계를 갖게 되는 대표적인 신호입니다. 주문, 주문 완료 이후 로직이 추가될 때마다 `OrderService`의 변경이 야기됩니다. 이런 문제를 `Application Event`으로 쉽게 해결 할 수 있습니다.


![](https://github.com/cheese10yun/TIL/blob/master/assets/event-part-2.png?raw=true)

`주문-결제` 이후 결제 완료 이벤트를 발생시키고 주문 완료 이후 로직들은 `Application Event Subscriber`에서 구현하게 작성하는 구조를 갖을 수 있습니다. 그렇게 되면 `OrderService`에 필요 의존성이 줄게 되고 자연스럽게 해당 서비스의 책임이 작아지게 되며 이벤트에 의해 느슨한 결합관계를 갖게 됩니다.

## 구현

주문 완료 이후 구매한 제품중 장바구니에 있는 제품을 제거 하는 로직을 구현 했습니다.

```kotlin
@Service
class OrderService(
        private val orderRepository: OrderRepository,
        private val itemRepository: ItemRepository,
        private val eventPublisher: ApplicationEventPublisher
) {

    fun order(itemCode: List<String>): Order {
        val items = itemRepository.findByCodeIn(itemCode) // 상품(Item) 조회
        val orderItems: MutableList<OrderItem> = mutableListOf()
        items.mapTo(orderItems) { OrderItem(it.code, it.price, it.name) }
        val order = orderRepository.save(Order(orderItems))
        eventPublisher.publishEvent(OrderCompletedEvent(itemCode)) // 주문 완료 이벤트 발생, 주문한 아이템 code 전송
        return order
    }
}
```
요청 받은 상품(Item)를 주문 하고, 주문 완료 event를 발생시키게 됩니다. 장바구니 제거 관련 코드는 존재하지 않고 주문 완료 이벤트만 발생시킵니다.

```kotlin
@Component
class OrderEventHandler(
        private val cartRepository: CartRepository
) {

    @EventListener
    fun orderEventHandler(event: OrderCompletedEvent) {
        cartRepository.deleteAllByCodes(codes = event.itemCodes)
    }

}
```
이벤트 리스너를 담당합니다. 주문 완료 이벤트가 발생하면 해당 리스너가 동작하게 되고 장바구니에서 주문 상품들을 제거하는 코드입니다.





* https://engkimbs.tistory.com/718
* https://javacan.tistory.com/entry/Handle-DomainEvent-with-Spring-ApplicationEventPublisher-EventListener-TransactionalEventListener
* https://medium.com/@SlackBeck/spring-framework%EC%9D%98-applicationevent-%ED%99%9C%EC%9A%A9%EA%B8%B0-845fd2d29f32