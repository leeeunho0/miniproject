// ================= 국내/해외 판별용 지역 목록 =================
// Hotel 엔티티에 country/region 필드가 없어서, HotelDummyService에 넣은
// 지역명을 기준으로 국내/해외를 구분함. 나중에 실제 필드가 생기면
// 이 하드코딩 목록 대신 서버에서 hotel.region 값을 내려받아 쓰는 게 정석.
const DOMESTIC_LOCATIONS = [
  '부산', '속초', '강릉', '남해', '통영', '서울', '대구', '경기', '거제',
  '제주', '전남', '구례', '춘천', '여수', '경주', '포항', '강원', '양평',
  '전주', '안동', '가평', '양양', '무주', '인천', '울산', '광주'
];

const OVERSEAS_LOCATIONS = [
  '도쿄', '요코하마', '오사카', '유후인', '하코네', '벳푸', '나가노', '삿포로',
  '교토', '나라', '후쿠오카', '이시가키', '가나자와', '다카야마', '니세코',
  '나리타', '고베', '아타미',
  '니스', '산토리니', '런던', '프랑크푸르트', '바덴바덴', '부다페스트',
  '인터라켄', '암스테르담', '베네치아', '파리', '바르셀로나', '프라하',
  '코츠월즈', '에든버러', '잘츠부르크', '샤모니', '취리히', '밀라노',
  '포르투', '모나코'
];

let currentRegion = 'domestic'; // 'domestic' | 'overseas'
let currentFilter = '전체';
let currentKeyword = ''; // 검색어 상태

// 각 숙소 카드/탭에 국내(domestic)·해외(overseas) 태그를 붙임
function tagRegions() {
  document.querySelectorAll('#card-row .hotel-card').forEach(card => {
    const location = card.getAttribute('data-location');
    card.dataset.region = OVERSEAS_LOCATIONS.includes(location) ? 'overseas' : 'domestic';
  });

  document.querySelectorAll('#tab-bar .tab').forEach(tab => {
    const filter = tab.getAttribute('data-filter');
    if (filter === '전체') return; // 전체 탭은 두 화면에서 공통으로 씀
    tab.dataset.region = OVERSEAS_LOCATIONS.includes(filter) ? 'overseas' : 'domestic';
  });
}

// 현재 region + filter + keyword 조건을 모두 만족하는 카드만 보이게 처리
function applyCardVisibility() {
  let visibleCount = 0;

  document.querySelectorAll('#card-row .hotel-card').forEach(card => {
    const regionMatch = card.dataset.region === currentRegion;
    const filterMatch = (currentFilter === '전체' || card.getAttribute('data-location') === currentFilter);

    // 검색어가 있으면 호텔명/설명/지역명에 포함되는지 검사 (없으면 통과)
    let keywordMatch = true;
    if (currentKeyword) {
      const name = card.querySelector('.hotel-name')?.textContent || '';
      const desc = card.querySelector('.hotel-desc')?.textContent || '';
      const location = card.getAttribute('data-location') || '';
      keywordMatch = (name + location + desc).includes(currentKeyword);
    }

    const visible = regionMatch && filterMatch && keywordMatch;
    card.style.display = visible ? '' : 'none';
    if (visible) visibleCount++;
  });

  // 검색 결과가 하나도 없을 때만 안내 문구 노출
  const emptyHint = document.getElementById('search-empty-hint');
  if (emptyHint) emptyHint.style.display = (currentKeyword && visibleCount === 0) ? '' : 'none';
}

// 현재 region에 해당하는 지역 탭만 보이게 처리 ("전체" 탭은 항상 보임)
function applyTabVisibility() {
  document.querySelectorAll('#tab-bar .tab').forEach(tab => {
    if (tab.getAttribute('data-filter') === '전체') {
      tab.style.display = '';
      return;
    }
    tab.style.display = (tab.dataset.region === currentRegion) ? '' : 'none';
  });
}

// 지역 탭 클릭 시 필터링
function filterHotels(btn) {
  currentFilter = btn.getAttribute('data-filter');

  document.querySelectorAll('#tab-bar .tab').forEach(t => t.classList.remove('active'));
  btn.classList.add('active');

  applyCardVisibility();
}

// 상단 인기 여행지 칩 클릭 -> 같은 이름의 탭이 있으면 그걸로 필터링 후 스크롤 이동
function jumpToLocation(chip) {
  const target = chip.getAttribute('data-filter');
  const matchedTab = Array.from(document.querySelectorAll('#tab-bar .tab'))
    .find(t => t.getAttribute('data-filter') === target && t.style.display !== 'none');

  const fallbackTab = document.querySelector('#tab-bar .tab[data-filter="전체"]');
  filterHotels(matchedTab || fallbackTab);

  document.getElementById('hotel-section').scrollIntoView({ behavior: 'smooth' });
}

// 국내 숙소 / 해외 숙소 토글
function switchView(region) {
  currentRegion = region;
  currentFilter = '전체';
  currentKeyword = ''; // 지역 토글 시 검색어도 같이 초기화
  document.getElementById('keyword-input').value = '';

  // 토글 버튼 활성 표시
  document.querySelectorAll('.stay-type').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.type === region);
  });

  // 인기 여행지 섹션 전환
  document.getElementById('domestic-destinations').classList.toggle('hidden', region !== 'domestic');
  document.getElementById('overseas-destinations').classList.toggle('hidden', region !== 'overseas');

  // 숙소 추천 섹션 제목 전환
  document.getElementById('hotel-section-title').textContent =
    region === 'domestic' ? '인기 추천 숙소' : '지금 가장 핫한 숙소';

  applyTabVisibility();

  // 전체 탭을 다시 활성화 상태로
  document.querySelectorAll('#tab-bar .tab').forEach(t => t.classList.remove('active'));
  document.querySelector('#tab-bar .tab[data-filter="전체"]').classList.add('active');

  applyCardVisibility();
}

// ================= 환율정보 모달 =================
// iframe으로 /exchange를 그대로 불러옴 (innerHTML로 넣으면 안의 <script>가 실행이 안 돼서 iframe 사용)
function openExchangeModal() {
  const overlay = document.getElementById('exchange-modal-overlay');
  const iframe = document.getElementById('exchange-modal-iframe');
  iframe.src = '/exchange'; // 열 때마다 새로 불러와서 최신 정보로 갱신
  overlay.classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeExchangeModal() {
  document.getElementById('exchange-modal-overlay').classList.add('hidden');
  document.getElementById('exchange-modal-iframe').src = ''; // 백그라운드 정리
  document.body.style.overflow = '';
}

// ================= 일정 선택 달력 =================
const WEEKDAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'];

let calendarBaseMonth = new Date(); // 왼쪽에 보여줄 기준 달 (오늘이 속한 달부터 시작)
calendarBaseMonth.setDate(1);
let selectedStart = null; // 체크인 Date
let selectedEnd = null;   // 체크아웃 Date

function toggleDateCalendar() {
  const popup = document.getElementById('date-calendar-popup');
  if (popup.classList.contains('hidden')) {
    renderCalendar();
    popup.classList.remove('hidden');
  } else {
    popup.classList.add('hidden');
  }
}

function moveCalendarMonth(diff) {
  calendarBaseMonth.setMonth(calendarBaseMonth.getMonth() + diff);
  renderCalendar();
}

// 왼쪽 달, 오른쪽 달(왼쪽 달 + 1개월) 두 개를 각각 그림
function renderCalendar() {
  renderMonth(document.getElementById('calendar-month-0'), calendarBaseMonth.getFullYear(), calendarBaseMonth.getMonth());

  const next = new Date(calendarBaseMonth);
  next.setMonth(next.getMonth() + 1);
  renderMonth(document.getElementById('calendar-month-1'), next.getFullYear(), next.getMonth());
}

// 한 달치 달력을 <table>로 만들어서 컨테이너에 넣음
function renderMonth(container, year, month) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const firstDay = new Date(year, month, 1);
  const lastDate = new Date(year, month + 1, 0).getDate(); // 그 달의 마지막 날짜
  const startWeekday = firstDay.getDay(); // 1일이 무슨 요일인지 (0=일 ~ 6=토)

  let html = `<div class="calendar-title">${year}년 ${month + 1}월</div>`;
  html += '<table class="calendar-table"><thead><tr>';
  WEEKDAY_LABELS.forEach(w => html += `<th>${w}</th>`);
  html += '</tr></thead><tbody><tr>';

  // 1일 요일 전까지는 빈 칸으로 채워서 요일 줄을 맞춤
  for (let i = 0; i < startWeekday; i++) html += '<td></td>';

  let weekday = startWeekday;
  for (let date = 1; date <= lastDate; date++) {
    const current = new Date(year, month, date);
    const isPast = current < today; // 오늘 이전 날짜는 선택 불가

    const classes = ['cal-day'];
    if (weekday === 0) classes.push('sunday');
    if (isPast) classes.push('disabled');
    if (selectedStart && isSameDate(current, selectedStart)) classes.push('selected');
    if (selectedEnd && isSameDate(current, selectedEnd)) classes.push('selected');
    if (selectedStart && selectedEnd && current > selectedStart && current < selectedEnd) classes.push('in-range');

    // event 인자 추가: 클릭 시 이벤트 버블링을 막기 위해 onDayClick에 넘겨줌
    const clickAttr = isPast ? '' : `onclick="onDayClick(${year}, ${month}, ${date}, event)"`;
    html += `<td><span class="${classes.join(' ')}" ${clickAttr}>${date}</span></td>`;

    weekday++;
    if (weekday === 7) { html += '</tr><tr>'; weekday = 0; }
  }
  html += '</tr></tbody></table>';

  container.innerHTML = html;
}

function isSameDate(a, b) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

// 체크인/체크아웃 두 번 클릭으로 기간을 정하는 로직
// 1) 아무것도 선택 안 됐거나, 이미 둘 다 선택된 상태 -> 새로 체크인부터 다시 시작
// 2) 체크인만 있는 상태에서 그보다 나중 날짜 클릭 -> 체크아웃으로 확정
// 3) 체크인보다 이전 날짜를 클릭 -> 체크인을 그 날짜로 다시 잡음
function onDayClick(year, month, date, event) {
  event.stopPropagation(); // 달력 다시 그릴 때 클릭한 엘리먼트가 detach되면서
                            // "바깥 클릭"으로 오인돼 팝업이 닫히는 것 방지

  const clicked = new Date(year, month, date);

  if (!selectedStart || (selectedStart && selectedEnd)) {
    selectedStart = clicked;
    selectedEnd = null;
  } else if (clicked > selectedStart) {
    selectedEnd = clicked;
  } else {
    selectedStart = clicked;
  }

  renderCalendar();
  updateDateSummary();

  // 체크인 + 체크아웃이 둘 다 정해졌을 때(=마지막 날짜 클릭)만 팝업 자동으로 닫기
  if (selectedStart && selectedEnd) {
    document.getElementById('date-calendar-popup').classList.add('hidden');
  }
}

function resetDateSelection() {
  selectedStart = null;
  selectedEnd = null;
  renderCalendar();
  updateDateSummary();
}

// 입력창 + 팝업 하단 요약을 "9.26 토 - 9.27 일 (1박)" 형태로 갱신
function updateDateSummary() {
  const summaryEl = document.getElementById('calendar-summary');
  const displayInput = document.getElementById('date-display');

  if (!selectedStart) {
    summaryEl.textContent = '체크인 - 체크아웃 날짜를 선택하세요';
    displayInput.value = '';
    return;
  }
  if (!selectedEnd) {
    summaryEl.textContent = `${formatDateShort(selectedStart)} 체크인 (체크아웃 날짜를 선택하세요)`;
    displayInput.value = formatDateShort(selectedStart);
    return;
  }

  const nights = Math.round((selectedEnd - selectedStart) / (1000 * 60 * 60 * 24));
  const text = `${formatDateShort(selectedStart)} - ${formatDateShort(selectedEnd)} (${nights}박)`;
  summaryEl.textContent = text;
  displayInput.value = text;
}

function formatDateShort(date) {
  return `${date.getMonth() + 1}.${date.getDate()} ${WEEKDAY_LABELS[date.getDay()]}`;
}

// ================= 인원 선택 =================
let guestCount = 2;
const GUEST_MIN = 1;
const GUEST_MAX = 20; // 최대 인원 임의로 지정

function toggleGuestPopup() {
  document.getElementById('guest-popup').classList.toggle('hidden');
}

function changeGuestCount(diff) {
  guestCount = Math.min(GUEST_MAX, Math.max(GUEST_MIN, guestCount + diff));
  updateGuestDisplay();
}

// 숫자/입력창 텍스트 갱신 + 최소/최대 도달 시 버튼 비활성화
function updateGuestDisplay() {
  document.getElementById('guest-count').textContent = guestCount;
  document.getElementById('guest-display').value = `인원 ${guestCount}`;
  document.querySelector('.guest-btn.minus').disabled = (guestCount <= GUEST_MIN);
  document.querySelector('.guest-btn.plus').disabled = (guestCount >= GUEST_MAX);
}

// ================= 호텔 상세 모달 =================
// 상단에서 실제로 선택한 일정/인원이 있으면 그대로 가져다 씀 (없으면 오늘+7일, 2박, 1명 기본값)
function openHotelModal(card) {
  const id = card.dataset.id;
  const name = card.dataset.name;
  const price = Number(card.dataset.price);

  document.getElementById('hotel-modal-name').textContent = name;

  const image = document.getElementById('hotel-modal-image');
  image.src = `/images/hotels/hotel${id}.png`;
  image.alt = name;

  let checkin, checkout;
  if (selectedStart && selectedEnd) {
    checkin = selectedStart;
    checkout = selectedEnd;
  } else {
    checkin = new Date();
    checkin.setDate(checkin.getDate() + 7);
    checkout = new Date(checkin);
    checkout.setDate(checkout.getDate() + 2);
  }

  // 실제 선택한 체크인/체크아웃 날짜 차이로 박수를 계산해서 "2박" 고정값 대신 씀 (최소 1박)
  const nights = Math.max(1, Math.round((checkout - checkin) / (1000 * 60 * 60 * 24)));

  document.getElementById('hotel-modal-nights').textContent = `${nights}박`;
  document.getElementById('hotel-modal-multiplier').textContent = `X ${nights}`;
  document.getElementById('hotel-modal-unit-price').textContent = '₩' + price.toLocaleString();
  document.getElementById('hotel-modal-total-price').textContent = '₩' + (price * nights).toLocaleString();

  const formatFull = d => `${d.getFullYear()}. ${d.getMonth() + 1}. ${d.getDate()}.`;
  document.getElementById('hotel-modal-checkin').textContent = formatFull(checkin);
  document.getElementById('hotel-modal-checkout').textContent = formatFull(checkout);

  // 무료 취소 기한 = 체크인 30일 전
  const cancelDeadline = new Date(checkin);
  cancelDeadline.setDate(cancelDeadline.getDate() - 30);
  document.querySelector('.hotel-modal-cancel-note').textContent =
    `${cancelDeadline.getMonth() + 1}월 ${cancelDeadline.getDate()}일 전까지 무료 취소 가능`;

  document.getElementById('hotel-modal-guest-count').textContent = `게스트 ${guestCount}명`;

  document.getElementById('hotel-modal-overlay').classList.remove('hidden');
  document.body.style.overflow = 'hidden';
}

function closeHotelModal() {
  document.getElementById('hotel-modal-overlay').classList.add('hidden');
  document.body.style.overflow = '';
}

// 예약하기 클릭 시 완료 메시지만 띄움 (실제 DB 저장 로직은 없음)
function confirmReservation() {
  alert('예약되었습니다!');
}

document.addEventListener('DOMContentLoaded', () => {
  tagRegions();

  document.querySelectorAll('.stay-type[data-type]').forEach(btn => {
    btn.addEventListener('click', () => switchView(btn.dataset.type));
  });

  // 검색 폼 제출 시 서버로 안 보내고 화면에서 바로 필터링
  document.getElementById('search-form').addEventListener('submit', (e) => {
    e.preventDefault();
    currentKeyword = document.getElementById('keyword-input').value.trim();
    applyCardVisibility();
    document.getElementById('hotel-section').scrollIntoView({ behavior: 'smooth' });
  });

  // 달력 / 인원 팝업 바깥 클릭하면 각각 닫기
  document.addEventListener('click', (e) => {
    const dateField = document.getElementById('date-field');
    const datePopup = document.getElementById('date-calendar-popup');
    if (!dateField.contains(e.target)) datePopup.classList.add('hidden');

    const guestField = document.getElementById('guest-field');
    const guestPopup = document.getElementById('guest-popup');
    if (!guestField.contains(e.target)) guestPopup.classList.add('hidden');
  });

  // 호텔 상세 모달 닫기 이벤트
  document.getElementById('hotel-modal-close').addEventListener('click', closeHotelModal);
  document.getElementById('hotel-modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'hotel-modal-overlay') closeHotelModal();
  });

  // 환율정보 모달 닫기 이벤트
  document.getElementById('exchange-modal-close').addEventListener('click', closeExchangeModal);
  document.getElementById('exchange-modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'exchange-modal-overlay') closeExchangeModal();
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeHotelModal();
      closeExchangeModal();
    }
  });

  updateGuestDisplay(); // 처음 로드될 때 버튼 disabled 상태 맞춰줌

  switchView('domestic'); // 첫 화면은 국내 숙소 기준으로 시작
});