package com.example.testcode.order;

public enum OrderStep {

  AWAITING_DEPOSITED, //  결지 미완료
  PAID, // 지불 완료
  REAMDY, // 배송 준비
  SHIPPING, // 배송중
  COMPLETED // 완료

}
