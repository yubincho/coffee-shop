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

export default function () {
  // 상품 목록 조회 (커서 없이 첫 페이지, size=10)
  const listRes = http.get(`${BASE_URL}/api/v1/products/all?size=10`);
  check(listRes, {
    'list status 200': (r) => r.status === 200,
    'list under 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
