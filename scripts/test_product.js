import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 시나리오 설정
export let options = {
    vus: 100,  // 가상 유저 수 (동시 접속 유저)
    duration: '30s',  // 테스트 지속 시간
};

export default function () {
    // 상품 목록을 조회하는 GET 요청 예제
    let res = http.get('http://localhost:8080/api/v1/products/all');

    // 응답 상태 코드가 200인지 확인
    check(res, {
        'status was 200': (r) => r.status === 200,
        'response time under 500ms': (r) => r.timings.duration < 500,
    });

    // 상품을 장바구니에 추가하는 POST 요청 예제 (URL 파라미터 사용)
    // let resPost = http.post('http://localhost:8080/api/v1/cartItems/item/add?productId=3&quantity=1&userId=1');
    //
    // // POST 요청 후 응답 상태와 시간이 적절한지 확인
    // check(resPost, {
    //     'status was 201': (r) => r.status === 201,
    //     'response time under 500ms': (r) => r.timings.duration < 500,
    // });

    // 잠시 대기 (1초)
    sleep(1);
}
