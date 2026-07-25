import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 500 },   // 2분에 걸쳐 500명까지 선형 증가
    { duration: '3m', target: 1000 },  // 3분에 걸쳐 1000명까지 선형 증가
    { duration: '2m', target: 1000 },  // 2분간 1000명 유지
    { duration: '1m', target: 0 },     // 1분간 서서히 줄이기
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95%가 500ms 안에 응답
    http_req_failed: ['rate<0.01'],    // 실패율 1% 미만
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // 상품 목록 조회
  const listRes = http.get(`${BASE_URL}/api/v1/products/all`);
  check(listRes, {
    'list status 200': (r) => r.status === 200,
    'list under 500ms': (r) => r.timings.duration < 500,
  });

  // 상품 상세 조회 (id=12)
  const detailRes = http.get(`${BASE_URL}/api/v1/products/product/12/product`);
  check(detailRes, {
    'detail status 200': (r) => r.status === 200,
  });

  sleep(1);  // 실제 유저처럼 1초 쉬기
}
