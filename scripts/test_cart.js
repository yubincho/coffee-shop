import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 시나리오 설정
export let options = {
    vus: 10,  // 가상 유저 수 (동시 접속 유저)
    duration: '30s',  // 테스트 지속 시간
};

export default function () {
    // get cart (userId)
    let res = http.get('http://localhost:8080/api/v1/carts/user/1/my-cart');

    // 응답 상태 코드가 200인지 확인
    check(res, {
        'status was 200': (r) => r.status === 200,
        'response time under 500ms': (r) => r.timings.duration < 500,
    });



    // 잠시 대기 (1초)
    sleep(1);
}
