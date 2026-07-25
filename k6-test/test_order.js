import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 지표: 실패율과 주문 API 응답 시간을 따로 추적
const errorRate = new Rate('errors');
const orderDuration = new Trend('order_duration');

export let options = {
    // 유저 수를 계단식으로 올려가며 임계점을 찾는다
    stages: [
        { duration: '30s', target: 10 },   // 30초 동안 10명까지 워밍업
        { duration: '1m',  target: 50 },    // 1분 동안 50명까지 증가
        { duration: '1m',  target: 100 },   // 1분 동안 100명까지 증가
        { duration: '1m',  target: 200 },   // 1분 동안 200명까지 증가
        { duration: '30s', target: 0 },     // 30초 동안 부하 제거(쿨다운)
    ],
    // 임계 기준: 이 선을 넘으면 테스트가 '실패'로 표시된다
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 미만이어야 함
        errors: ['rate<0.01'],             // 에러율 1% 미만이어야 함
    },
};

const BASE_URL = 'http://localhost:8080/api/v1';

export default function () {
    // 상품 주문 POST 요청 (임계점 관찰의 핵심 대상)
    let resPost = http.post(`${BASE_URL}/orders/user/place-order?userId=1`);

    // 주문 응답 시간을 커스텀 지표에 기록
    orderDuration.add(resPost.timings.duration);

    // 성공 여부를 체크하고, 실패하면 errorRate에 반영
    const success = check(resPost, {
        'order status was 200': (r) => r.status === 200,
        'order under 500ms': (r) => r.timings.duration < 500,
    });
    errorRate.add(!success);

    sleep(1);
}
