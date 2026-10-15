// HTML의 숨겨진 input에서 Controller가 넘긴 값 꺼내기
const hotelId = document.getElementById("hotelId").value;
const nights = document.getElementById("nights").value;
const clientKey = document.getElementById("clientKey").value;

// 토스 SDK 초기화 → 결제창 인스턴스 생성 (비회원 결제)
const tossPayments = TossPayments(clientKey);
const payment = tossPayments.payment({ customerKey: TossPayments.ANONYMOUS });

// [결제하기] 토스 결제창으로 실제 결제 흐름 진행
document.getElementById("payBtn").onclick = function () {

  // ① 결제 준비 API 호출: 서버가 DB 1박 가격 × 박수로 금액을 정하고 READY로 저장
  fetch(`/payments/prepare/${hotelId}?nights=${nights}`, { method: "POST" })
    .then(res => {
      if (!res.ok) throw new Error(res.status); // 500, 404면 에러로
      return res.json();
    })
    .then(data => {
      // 서버가 준 주문 정보를 화면에 표시
      document.getElementById("orderName").innerText = data.orderName;
      document.getElementById("orderId").innerText = data.orderId;
      document.getElementById("amount").innerText = data.amount.toLocaleString() + "원";

      // ② 토스 결제창 열기 (금액은 서버가 준 값, 토스는 정수만 받으므로 Math.round)
      return payment.requestPayment({
        method: "CARD",
        amount: { currency: "KRW", value: Math.round(data.amount) },
        orderId: data.orderId,
        orderName: data.orderName,
        successUrl: window.location.origin + "/payment/success",
        failUrl: window.location.origin + "/payment/fail"
      });
    })
    .catch(err => {
      console.error(err);
      alert("결제를 진행할 수 없습니다.");
    });
};

// [테스트 결제] 토스 결제창 없이 서버 금액 검증 후 바로 완료
document.getElementById("testPayBtn").onclick = function () {

  // ① 결제 준비 API 호출 (실제 결제와 똑같이 서버가 금액을 정하고 READY로 저장)
  fetch(`/payments/prepare/${hotelId}?nights=${nights}`, { method: "POST" })
    .then(res => {
      if (!res.ok) throw new Error(res.status);
      return res.json();
    })
    .then(data => {
      // ② 토스 결제창 대신 테스트 완료 주소로 바로 이동
      location.href = `/payment/test-success?orderId=${data.orderId}&amount=${Math.round(data.amount)}`;
    })
    .catch(err => {
      console.error(err);
      alert("테스트 결제를 진행할 수 없습니다.");
    });
};