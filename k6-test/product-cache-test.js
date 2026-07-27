// 상품 - 상세조회 테스트 - 레디스 캐시 적용

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 300 },   // 1분간 300명까지 증가
    { duration: '3m', target: 300 },   // 3분간 300명 유지
    { duration: '1m', target: 0 },     // 1분간 서서히 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95%가 500ms 안에 응답
    http_req_failed: ['rate<0.01'],    // 실패율 1% 미만
  },
};

const BASE_URL = 'http://localhost:8080';
const PRODUCT_ID = 12;

export default function () {
  // 상품 목록 조회
  const listRes = http.get(`${BASE_URL}/api/v1/products/all`);
  check(listRes, {
    'list status 200': (r) => r.status === 200,
    'list under 500ms': (r) => r.timings.duration < 500,
  });

  // 상품 상세 조회 (캐시 대상)
  const detailRes = http.get(`${BASE_URL}/api/v1/products/product/${PRODUCT_ID}/product`);
  check(detailRes, {
    'detail status 200': (r) => r.status === 200,
    'detail under 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}


