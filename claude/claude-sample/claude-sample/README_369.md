# 고급 369 게임 시스템

4단계로 발전하는 369 게임 구현 프로젝트입니다.

## 📋 단계별 구현 내용

### 1단계: 기본 369 게임
- **인터페이스 기반 설계**: `Game369Rule` 인터페이스로 규칙 추상화
- **기본 로직**: 숫자에 3, 6, 9가 포함되면 "clap" 출력

### 2단계: 게임 세션 및 플레이어 관리
- **Player 클래스**: 정답/오답 통계, 오답률 계산
- **GameSession 클래스**: 게임 진행, 라운드 관리
- **오답률 기반 종료**: 30% 초과 시 자동 종료
- **게임 상태 추적**: READY, IN_PROGRESS, COMPLETED, FAILED

### 3단계: 지역별 규칙 추상화 및 다형성
- **Strategy 패턴**: 규칙을 교체 가능한 전략으로 구현
- **한국식 규칙**: 3, 6, 9 박수
- **중국식 규칙**: 3, 6, 9, 7 박수
- **Factory 패턴**: `Game369RuleFactory`로 규칙 생성

### 4단계: 동시성 적용
- **Kotlin Coroutines**: 비동기 게임 진행
- **GameManager**: 여러 세션 동시 관리
- **Thread-safe**: ConcurrentHashMap, AtomicInteger 사용
- **독립적 실행**: 각 세션이 병렬로 실행

## 🏗️ 아키텍처

```
game/
├── rule/                   # 3단계: 규칙 추상화
│   ├── Game369Rule.kt     # 규칙 인터페이스
│   ├── KoreanGame369Rule.kt
│   ├── ChineseGame369Rule.kt
│   └── Game369RuleFactory.kt
├── model/                  # 2단계: 도메인 모델
│   ├── Player.kt          # 플레이어 통계
│   └── RoundResult.kt     # 라운드 결과
├── session/                # 2단계: 게임 세션
│   └── GameSession.kt     # 게임 진행 관리
└── manager/                # 4단계: 동시성 관리
    └── GameManager.kt     # 여러 세션 관리
```

## 🚀 실행 방법

### 빌드 및 테스트
```bash
./gradlew build
./gradlew test
```

### 실행
```bash
./gradlew run
```

## ✅ 테스트

총 **13개의 테스트** 포함:

- **규칙 테스트** (7개): 한국식/중국식/Factory
- **모델 테스트** (2개): Player 통계 계산
- **세션 테스트** (4개): 정답/오답 처리, 종료 조건
- **매니저 테스트** (2개): 세션 생성, 동시 실행

```bash
./gradlew test --tests "com.example.claudesample.game.*"
```

## 🎯 주요 기능

### 오답률 기반 게임 종료
```kotlin
val session = GameSession(
    sessionId = "SESSION-1",
    player = Player("김철수"),
    rule = KoreanGame369Rule(),
    wrongRateThreshold = 30.0  // 30% 초과 시 종료
)
```

### 지역별 규칙 선택
```kotlin
// 한국식: 3, 6, 9
val koreanRule = Game369RuleFactory.createRule(RuleType.KOREAN)

// 중국식: 3, 6, 9, 7
val chineseRule = Game369RuleFactory.createRule(RuleType.CHINESE)
```

### 동시 다중 세션 실행
```kotlin
val sessions = listOf(
    manager.createSession("김철수", koreanRule),
    manager.createSession("왕밍", chineseRule),
    manager.createSession("이영희", koreanRule)
)

// 코루틴으로 동시 실행
manager.runMultipleSessionsAuto(sessions)
```

## 📊 실행 결과 예시

```
=== 고급 369 게임 시스템 ===

5개의 게임 세션을 동시에 실행합니다...

[SESSION-1] 시작: 김철수 (한국식 369)
[SESSION-2] 시작: 이영희 (한국식 369)
[SESSION-3] 시작: 왕밍 (중국식 369 (7 포함))
...

=== 세션 [SESSION-1] 결과 ===
규칙: 한국식 369
[김철수] 정답: 46, 오답: 4, 총 50회 (오답률: 8.0%)
상태: COMPLETED
```

## 🛠️ 기술 스택

- **언어**: Kotlin 2.2.21
- **프레임워크**: Spring Boot 4.0.2
- **동시성**: Kotlin Coroutines 1.8.0
- **테스트**: JUnit 5, Kotlin Test
- **빌드**: Gradle 9.3.0

## 🎨 설계 패턴

1. **Strategy Pattern**: 지역별 규칙 교체
2. **Factory Pattern**: 규칙 객체 생성
3. **Singleton**: GameManager, RuleFactory
4. **Immutable Data**: RoundResult (data class)

## 🔒 동시성 처리

- **ConcurrentHashMap**: 세션 저장소
- **AtomicInteger**: 플레이어 통계 카운터, 세션 ID
- **@Volatile**: 상태 플래그
- **Coroutines**: 비동기 실행 (CoroutineScope, async/await)

## 📝 라이선스

MIT License
