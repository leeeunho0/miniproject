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

// 현재 region + filter 조건에 맞는 카드만 보이게 처리
function applyCardVisibility() {
  document.querySelectorAll('#card-row .hotel-card').forEach(card => {
    const regionMatch = card.dataset.region === currentRegion;
    const filterMatch = (currentFilter === '전체' || card.getAttribute('data-location') === currentFilter);
    card.style.display = (regionMatch && filterMatch) ? '' : 'none';
  });
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

document.addEventListener('DOMContentLoaded', () => {
  tagRegions();

  document.querySelectorAll('.stay-type[data-type]').forEach(btn => {
    btn.addEventListener('click', () => switchView(btn.dataset.type));
  });

  switchView('domestic'); // 첫 화면은 국내 숙소 기준으로 시작
});