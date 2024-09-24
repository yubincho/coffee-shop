import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 시나리오 설정
export let options = {
    vus: 10,  // 가상 유저 수 (동시 접속 유저)
    duration: '30s',  // 테스트 지속 시간
};

export default function () {
    // get order by orderId
    let res = http.get('http://localhost:8080/api/v1/orders/3/order');

    // 응답 상태 코드가 200인지 확인
    check(res, {
        'status was 200': (r) => r.status === 200,
        'response time under 500ms': (r) => r.timings.duration < 500,
    });

    // 상품 주문 POST 요청 예제 (URL 파라미터 사용)
    let resPost = http.post('http://localhost:8080/api/v1/orders/user/place-order?userId=1');

    // POST 요청 후 응답 상태와 시간이 적절한지 확인
    check(resPost, {
        'status was 200': (r) => r.status === 200,
        'response time under 500ms': (r) => r.timings.duration < 500,
    });

    // get order by userId
    let res2 = http.get('http://localhost:8080/api/v1/orders/user/1/order');

    // POST 요청 후 응답 상태와 시간이 적절한지 확인
    check(resPost, {
        'status was 200': (r) => r.status === 200,
        'response time under 500ms': (r) => r.timings.duration < 500,
    });

    // 잠시 대기 (1초)
    sleep(1);
}
