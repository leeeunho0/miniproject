// HTML의 숨겨진 input에서 Controller가 넘긴 값 꺼내기
const hotelId = document.getElementById("hotelId").value;
const nights = document.getElementById("nights").value;
const clientKey = document.getElementById("clientKey").value;

// 토스 SDK 초기화 → 결제창 인스턴스 생성 (비회원 결제)
const tossPayments = TossPayments(clientKey);
const payment = tossPayments.payment({ customerKey: TossPayments.ANONYMOUS });

// 결제 준비 API 호출: 서버가 DB 1박 가격 × 박수로 금액을 정하고 READY로 저장
// 실패하면 서버가 보낸 ErrorResponse({ status, message })의 message로 에러를 만든다
function preparePayment() {
  return fetch(`/payments/prepare/${hotelId}?nights=${nights}`, { method: "POST" })
    .then(res => {
      if (!res.ok) {
        // 404, 400, 500이면 응답 JSON에서 message를 꺼내 catch로 넘김
        return res.json().then(err => {
          throw new Error(err.message);
        });
      }
      return res.json();
    });
}

// [결제하기] 토스 결제창으로 실제 결제 흐름 진행
document.getElementById("payBtn").onclick = function () {

  // 결제 준비가 끝나면 주문번호를 담아둠 → 결제창 오류 시 어떤 주문을 취소할지 알 수 있음
  let orderId = null;

  preparePayment()
    .then(data => {
      orderId = data.orderId;

      // 서버가 준 주문 정보를 화면에 표시
      document.getElementById("orderName").innerText = data.orderName;
      document.getElementById("orderId").innerText = data.orderId;
      document.getElementById("amount").innerText = data.amount.toLocaleString() + "원";

      // 토스 결제창 열기 (금액은 서버가 준 값, 토스는 정수만 받으므로 Math.round)
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

      // orderId가 있으면 결제 준비는 성공 → 결제창을 닫았거나 결제창 오류
      // 실패 주소로 이동해서 READY 주문을 CANCELED로 정리하고 실패 화면을 보여줌
      if (orderId) {
        location.href = `/payment/fail?orderId=${orderId}&message=결제가 취소되었습니다.`;
        return;
      }

      // orderId가 없으면 결제 준비 단계(서버)에서 실패 → 서버 메시지 표시
      alert(err.message);
    });
};

// [테스트 결제] 토스 결제창 없이 서버 금액 검증 후 바로 완료
document.getElementById("testPayBtn").onclick = function () {

  preparePayment()
    .then(data => {
      // 토스 결제창 대신 테스트 완료 주소로 바로 이동
      location.href = `/payment/test-success?orderId=${data.orderId}&amount=${Math.round(data.amount)}`;
    })
    .catch(err => {
      console.error(err);
      alert(err.message);
    });
};