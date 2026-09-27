document.addEventListener('DOMContentLoaded', () => {

  const rateRows = document.querySelectorAll('table tbody tr[data-code]');
  const currencySelect = document.getElementById('calc-currency-select');
  const rateMap = {}; // { USD: 1360.00, EUR: 1556.93, ... } 표에서 그대로 가져옴

  // select의 option 4개는 HTML에 이미 고정으로 있으니, 여기서는 option을 새로 만들지 않고
  // 표(tbody)에서 매매기준율만 뽑아 rateMap에 채움 (중복 옵션 생성 방지)
  rateRows.forEach(row => {
    const code = row.dataset.code;
    const rate = parseFloat(row.dataset.rate);
    rateMap[code] = rate;
  });

  const btnToKrw = document.getElementById('dir-to-krw');
  const btnToForeign = document.getElementById('dir-to-foreign');
  const amountInput = document.getElementById('calc-amount');
  const calcBtn = document.getElementById('calc-btn');
  const rateHint = document.getElementById('rate-hint');
  const resultPlaceholder = document.getElementById('result-placeholder');
  const resultBox = document.getElementById('result-box');
  const resultAmount = document.getElementById('result-amount');
  const resultUnit = document.getElementById('result-unit');
  const resultMeta = document.getElementById('result-meta');

  let direction = 'toKrw'; // toKrw: 외화->원화, toForeign: 원화->외화

  // 통화코드에 "(100)"처럼 괄호 숫자가 붙어있으면 그 단위 기준 환율이라는 뜻
  // (JPY(100) = 100엔당 환율. 그냥 1엔 기준으로 계산하면 100배 차이나서 틀리게 나옴)
  function getUnit(code) {
    const match = code.match(/\((\d+)\)/);
    return match ? parseInt(match[1], 10) : 1;
  }

  function setDirection(dir) {
    direction = dir;
    btnToKrw.classList.toggle('active', dir === 'toKrw');
    btnToForeign.classList.toggle('active', dir === 'toForeign');
    updateHint();
  }

  function updateHint() {
    const code = currencySelect.value;
    const rate = rateMap[code];
    if (!rate) return;
    const unit = getUnit(code);
    rateHint.textContent = `${unit} ${code} = ${rate.toLocaleString('ko-KR', { minimumFractionDigits: 2 })} KRW`;
  }

  function calculate() {
    const code = currencySelect.value;
    const rate = rateMap[code];
    const amount = parseFloat(amountInput.value);
    const unit = getUnit(code);

    if (!rate || !amount || amount <= 0) {
      alert('금액을 올바르게 입력해주세요.');
      return;
    }

    // rate는 "unit개당" 가격이라서, amount를 unit으로 나눠서 "1단위당 배수"로 바꾼 다음 계산
    const result = direction === 'toKrw'
      ? (amount / unit) * rate
      : (amount / rate) * unit;

    resultPlaceholder.classList.add('hidden');
    resultBox.classList.remove('hidden');
    resultAmount.textContent = result.toLocaleString('ko-KR', { maximumFractionDigits: 2 });
    resultUnit.textContent = direction === 'toKrw' ? '원 (KRW)' : code;
    resultMeta.textContent = direction === 'toKrw'
      ? `${amount.toLocaleString('ko-KR')} ${code} 기준`
      : `${amount.toLocaleString('ko-KR')} 원 기준`;
  }

  btnToKrw.addEventListener('click', () => setDirection('toKrw'));
  btnToForeign.addEventListener('click', () => setDirection('toForeign'));
  currencySelect.addEventListener('change', updateHint);
  calcBtn.addEventListener('click', calculate);

  setDirection('toKrw'); // 초기 상태
});