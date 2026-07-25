import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 100 },   // 1분간 100명까지 증가
    { duration: '2m', target: 500 },   // 2분간 500명까지 증가
    { duration: '3m', target: 500 },   // 3분간 500명 유지 (여기서 락 경합 관찰)
    { duration: '1m', target: 0 },     // 1분간 0으로 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95%가 2초 이내면 통과
    http_req_failed: ['rate<0.05'],     // 실패율 5% 미만이면 통과
  },
};

const BASE_URL = 'http://localhost:8080';
const PRODUCT_ID = 12;   // 모든 VU가 같은 상품을 노림 (락 경합 유도)

export default function () {
  // 유저 id 3~32 중 랜덤 선택 (30명 분산)
  const userId = Math.floor(Math.random() * 30) + 3;

  // 1) 장바구니 담기
  const addRes = http.post(
    `${BASE_URL}/api/v1/cartItems/item/add?productId=${PRODUCT_ID}&quantity=1&userId=${userId}`
  );
  check(addRes, {
    'cart add success': (r) => r.status === 200 || r.status === 201,
  });

  // 2) 주문하기 (비관적 락이 걸리는 지점)
  const orderRes = http.post(
    `${BASE_URL}/api/v1/orders/user/place-order?userId=${userId}`
  );
  check(orderRes, {
    'order success': (r) => r.status === 200,
  });

  sleep(1);
}
