import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    status_check: {
      executor: 'constant-arrival-rate',
      rate: 100,
      timeUnit: '1s',
      duration: '3m',
      preAllocatedVUs: 50,
      maxVUs: 200,
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const MIN_ID = 3;
const MAX_ID = 52; // 50개 정도로 좁혀서 조회 집중 → hit율 상승

export default function () {
  const orderId = Math.floor(Math.random() * (MAX_ID - MIN_ID + 1)) + MIN_ID;
  const res = http.get(`http://localhost:8080/api/v1/orders/${orderId}/status`);
  check(res, {
    'status ok': (r) => r.status === 200,
  });
}
