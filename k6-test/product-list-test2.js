// 레디스 캐시 적용
// 실제 패턴으로 테스트 하기

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 300 },
    { duration: '3m', target: 300 },
    { duration: '1m', target: 0 }
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01']
  }
};

const BASE_URL = 'http://localhost:8080';

// 더미 데이터에 실제로 매칭되는 키워드로 구성
const keywords = [
  '상품',      // name/description에 광범위하게 매칭 (많은 행 반환)
  '스타벅스',  // brand 매칭
  '메가커피',  // brand 매칭
  '설명',      // description 매칭
  '', '', '', '', '', ''  // 검색 안 함(그냥 스크롤)을 다수로
];

export default function () {
  // 1) 커서를 랜덤하게 이동 (무한 스크롤로 여러 페이지를 넘기는 상황 흉내)
  //    100,003건 안에서 랜덤 위치를 커서로 사용
  const randomCursor = Math.floor(Math.random() * 100000);
  const keyword = keywords[Math.floor(Math.random() * keywords.length)];

  const url = `${BASE_URL}/api/v1/products/all?cursor=${randomCursor}&size=10&keyword=${encodeURIComponent(keyword)}`;

  const res = http.get(url);
  check(res, {
    'status 200': r => r.status === 200,
    'under 500ms': r => r.timings.duration < 500
  });
  sleep(1);
}
