//상품 상세 조회 테스트 - 캐시 적용 전

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 300 },
    { duration: '3m', target: 300 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = 'http://localhost:8080';
const PRODUCT_ID = 12;

export default function () {
  // 상품 상세 조회만 테스트 (캐시 대상)
  const detailRes = http.get(`${BASE_URL}/api/v1/products/product/${PRODUCT_ID}/product`);
  check(detailRes, {
    'detail status 200': (r) => r.status === 200,
    'detail under 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
