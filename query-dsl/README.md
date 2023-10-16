# 사전 과제

JPA와 H2를 이용하여 주어진 객체 관계를 바탕으로 CRUD API를 설계하고 구현해주세요. 기본적으로는 REST 형식을 따르되 필요에 따라 조정해도 괜찮습니다. **전체 API 중에서 조회와 등록 API에 중점을 두어 작성해주시며, 페이징 처리 가능한 조회 API는 최소 1개 이상 포함시켜 주세요.** 모든 객체에 대해 API를 구현할 필요는 없으니, 가능한 한도 내에서 작업해주시기 바랍니다.

## 객체 관계

- **Member**:
  - 각 회원은 여러 개의 `Address`를 가질 수 있습니다.
  - 각 회원은 하나의 `Team`에 속할 수 있습니다.

- **Team**:
  - 하나의 팀은 여러 `Member`들을 포함할 수 있습니다.

- **Address**:
  - 각 주소는 하나의 `Member`에 연결됩니다.

## 도식:

```
Team <----------------- Member
     |                   |
     | 1               0..n
     |                   |
     ------------------> Address
     | 1               0..n
```

- **Team** <-> **Member**:
  - `Team`과 `Member`는 일대다 관계입니다.
  - 하나의 `Team`은 여러 `Member`를 포함할 수 있고, 각 `Member`는 하나의 `Team`에만 속할 수 있습니다.

- **Member** <-> **Address**:
  - `Member`와 `Address`는 일대다 관계입니다.
  - 하나의 `Member`는 여러 `Address`를 가질 수 있고, 각 `Address`는 하나의 `Member`에 연결될 수 있습니다.

### 코드

```java

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;

    private String mascot;

    private String foundedDate;
    
    private List<Member> members = new ArrayList<>();
}


@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String position;

    private String joinedDate;
    
    private Team team;

    private List<Address> addresses = new ArrayList<>();
}

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;

    private String city;

    private String state;

    private String zipCode;
    
    private Member member;
}
```

모든 필드 값은 null이 될 수 없습니다. **주어진 연관 관계를 엄격하게 따를 필요는 없으며, 필요에 따라 객체 관계를 적절히 조절하여 설계해도 괜찮습니다.**
