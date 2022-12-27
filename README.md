# Throttling Consumer 구현

## 요구사항

- 이벤트 메시지를 1초 내 5개까지만 소비해야 하는 제한조건이 있는 이벤트 처리기를 구현합니다.
- 서버는 퍼포먼스 및 가용성을 위해 Sacle-out되어있습니다. (2대 이상)

## 고민거리

- Active MQ에 지연발송 기능이 있으나 loop문에서 지연발송해도 결국 한꺼번에 소비가 되므로 지연발송 기능은 사용할 수 없음
- Consumer에서 wait를 걸어서 처리하는 것 또한 Sacle-out되어 있는 서버 특성 상 각 서버마다 wait 해서 처리하므로 서버 대수만큼 한번에 처리하는 이슈가 있음

## 해결

- [bucket4j](https://github.com/bucket4j/bucket4j)를 이용하여 Consumer를 클러스터링 후 Queueing된 메시지를 순차적으로 지연 처리함 

## 필요조건

- DB 및 MQ 실행

  ```
  docker-compose up
  ```

- DDL
  ```sql
  CREATE TABLE bucket(id BIGINT PRIMARY KEY, state BYTEA);
  ```

## 참고

- https://activemq.apache.org/delay-and-schedule-message-delivery
- https://github.com/bucket4j/bucket4j
