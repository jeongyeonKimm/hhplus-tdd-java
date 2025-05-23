## STEP01 - 기본 과제

### 책임과 역할

- 단순히 기능을 구현하는 것에서 벗어나 각 계층과 도메인이 가지는 책임과 역할에 대해 고민했습니다.
- PointService가 너무 많은 책임을 갖지 않도록 포인트 충전/사용 시 충전/사용 amount에 대한 검증과 포인트 업데이트는 UserPoint 내에서 수행하도록 하였습니다.

### 확장성 고려

- 데이터 저장소를 추상화하기 위해 Repository 계층을 생성하였습니다.
- Repository 계층을 생성하면서 어느 정도까지 확장성을 생각해야 하는지에 대한 고민이 많았는데 멘토링을 통해 너무 먼 미래까지 고려하지 않아도 된다는 피드백을 받았습니다.
- Layered Architecture의 구성을 갖추도록 Repository 계층을 생성해두고, 나중에 추가적으로 확장이 필요한 요구사항이 생기면 리팩토링 진행을 할 수 있도록 했습니다.

### 테스트 가능한 코드

- 의존성을 최소화, 모듈화에 대한 의미를 생각했습니다.
- 테스트가 불가한 private 메서드를 작성하지 않으려 노력했습니다.
- 테스트 코드도 지속적인 리팩토링이 필요한 코드라고 생각하여 테스트 코드의 질을 높이기 위해 리팩토링을 진행하였습니다.

---

## STEP02 - 심화 과제

### 동시성 문제 해결 방법

#### 1. synchronized

- synchronized는 가장 쉽게 접근할 수 있는 동시성 제어 방법이지만 무한 대기 문제와 공정성 문제가 발생합니다.
- BLOCKED 상태로 무한 대기를 하게되면 interrupt가 발생하여도 빠져나오지 못하기 때문에 성능이 저하됩니다.
- 대기 중인 스레드 중 어떤 스레드가 락을 얻게 될 지 알지 못해 특정 스레드가 오래 기다리게 되는 문제 또한 발생 가능합니다.

#### 2. ReentrantLock + ConcurrentHashMap

- ReentrantLock은 WAITING, TIMED_WAITING 상태로 대기하기 때문에 interrupt로 빠져나오는 것이 가능하여 synchronized의 무한 대기 문제 해결이 가능합니다.
- 공정 모드와 비공정 모드를 제공해 공정성 문제 또한 해결 가능합니다.
  - 공정 모드: 대기 순서대로 락을 제공. 성능 저하될 수 있음.
  - 비공정 모드: 성능을 중시. 기아 현상 발생 가능성 있음.
- ConcurrentHashMap은 읽기 작업에 대해서는 동시에 읽는 것이 가능하지만 쓰기 작업에 대해서는 특정 세크먼트나 버킷에 대한 락을 가져야 가능합니다.
- userId를 key로 하여 userId 별로 독립적인 락이 걸리도록 하여 동일 사용자에 대한 요청은 직렬 처리, 다른 사용자에 대한 요청은 병렬 처리가 가능합니다.


| 항목 | `synchronized` | `ConcurrentHashMap + ReentrantLock` |
|------|--------------|--------------------------------------|
| **락 단위** | 객체 전체 or 메서드 단위 | 특정 키 (예: userId) 단위로 제어 가능 |
| **제어 범위** | 고정 (블록/메서드) | 유연 (락 범위 자유롭게 지정 가능) |
| **병렬성** | 낮음 (모든 요청이 직렬화) | 높음 (사용자별 동시 처리 가능) |
| **사용자 단위 락** | 불가능 | 가능 (사용자 ID 기반) |
| **유연성** | 낮음 | 높음 (tryLock, 타임아웃 등 가능) |
| **락 해제 책임** | 개발자 책임 (`try-finally` 필수) | 동일 |
| **실제 서비스 적합도** | 단순한 경우만 | 사용자 단위 동시성 제어에 적합 |


