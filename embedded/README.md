---
layout: post
title: JPA-Embedded
subtitle: JPA-Tip
catalog: true
header-img: https://i.imgur.com/avC1Xor.jpg
thumbnail:
date:
tags:
  -
---

## Embedded Type

**JPA에서는 새로운 값 타입을 직접 정의해서 사용 할 수 있습니다. 배송 관련 서비스에서 발송인(Sender), 수취인(Receiver)가 있을 경우 중복적으로 주소에 관련 칼럼들이 요구 됩니다. 이러한 중복적인 칼럼들을 자료형으로 규합해서 훨씬더 객체지향적으로 풀어 나갈 수 있을 거같습니다.**

## Receiver 클래스
```java
@Entity
public class Receiver {

    @Id
    @GeneratedValue
    private long id;

    @Embedded
    private Name name;

    @Embedded
    private Address address;

    @Embedded
    private PhoneNumber phoneNumber;
    ...
}
```

* `Address`, `Name` 새로운 타입을 직접 정의 했습니다.
* `@Embedded` 어노테이션을 통해서 값 타입을 사용한다고 명시 했습니다.

## Address 클래스
```java
@Embeddable
public class Address {

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "zip_code")
    private String zipCode;

}
```
* `Embeddable` 어노테이션을 통해서 값 타입을 사용한다고 명시 했습니다.
* 엔티티 객체와 거의 비슷합니다.


## Receiver 클래스
```java
@Entity
public class Sender {
    ...
    @Embedded
    private Name name;

    @Embedded
    private Address address;
    ...
}
```
* `Receiver`클래스에서 직접 정의한 `Address`를 쉽게 사용할 수 있습니다.

## Name 클래스
```java
@Embeddable
public class Name {

    @Column(name = "first_name")
    private String first;

    @Column(name = "last_name")
    private String last;

    public String getFullName() {
        return this.first + this.last;
}
```
* 클래로로 정의하면 다양한 부수적인 효과를 적용 시킬 수 있습니다.
* 정말 간단한 예로 `getFullName()` 메소드로 full name을 얻을 수 있습니다. 해당 모델에 맞는 다양한 함수를 정의 할 수 있습니다.

## @AttributeOverride 재정의

```java
@Entity
public class Sender {
    ...
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "sender_city")),
            @AttributeOverride(name = "street", column = @Column(name = "sender_street")),
            @AttributeOverride(name = "zip_code", column = @Column(name = "sender_zip_code"))
    })
    private Address address;
    ...
}
```
* 임베디드 타입에 정의한 매핑정보를 재정의 하려면 `@AttributeOverride`를 사용하면 됩니다.
* 해당 칼럼은 `sender_city`, `sender_street`, `sender_zip_code` 으로 생성됩니다.