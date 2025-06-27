import http from "k6/http";
import { check, sleep, group } from "k6";
import { SharedArray } from "k6/data";
import papaparse from "https://jslib.k6.io/papaparse/5.1.1/index.js";
import { scenario, exec } from "k6/execution";

// --- Riot API 속도 제한 준수를 위한 계산 ---
// 우리 백엔드 서비스가 외부 Riot API를 호출하는 구조는 다음과 같습니다.
// 1. 계정 정보(Account): 1회
// 2. 리그 정보(Entry/Status): 1회
// 3. 매치 ID 목록(Match Ids): 1회
// 4. 각 매치 상세 정보(Matches): 최대 20회
// => 총합: 1 + 1 + 1 + 20 = 최대 23회의 외부 Riot API 호출이 발생합니다.
//
// Riot API 속도 제한:
// - 10초당 20개 요청 (2 req/s)
// - 120초당 100개 요청 (0.83 req/s) -> 더 엄격한 제한
//
// 우리 테스트가 이 제한을 넘지 않으려면:
// (테스트 실행률) * 23 API 호출 < 0.83 req/s
// 테스트 실행률 < 0.036 실행/s  (1 / 0.036 ≈ 27.7초)
// 따라서, 30초에 1번씩 테스트를 실행하여 속도 제한을 안전하게 준수합니다.

// --- 테스트 데이터 로드 ---
// CSV 파일을 읽어서 모든 가상 사용자(VU)가 공유하는 메모리에 올립니다.
const summoners = new SharedArray("summoner list", function () {
  // `papaparse` 라이브러리를 사용하여 CSV 파일 내용을 파싱합니다.
  // header: true 옵션으로 첫 줄을 헤더(key)로 사용합니다.
  return papaparse.parse(open("./summoner-list.csv"), { header: true }).data;
});

// --- 테스트 설정 ---
// 이 옵션은 부하 테스트의 시나리오를 정의합니다.
// 여기서는 30초에 걸쳐 가상 사용자(VUS)를 20명까지 늘리고,
// 1분간 20명을 유지한 뒤, 10초에 걸쳐 사용자를 0으로 줄입니다.
export const options = {
  scenarios: {
    // `constant-arrival-rate` 실행기를 사용하여 요청률을 정밀하게 제어합니다.
    baseline_scenario: {
      executor: "constant-arrival-rate",

      // 속도: 30초당 1번의 테스트를 실행합니다.
      // 계산: (1 실행 / 30초) * 23 API 호출 ≈ 0.77 req/s
      // 이 속도는 Riot API의 제한(0.83 req/s)을 넘지 않습니다.
      rate: 1,
      timeUnit: "30s",

      // 테스트 지속 시간: 4번의 반복을 보장하기 위해 넉넉하게 설정합니다.
      // 실제 종료는 스크립트 내에서 제어합니다.
      duration: "2m30s",

      // 이 테스트를 실행할 가상 사용자를 미리 할당합니다.
      // 동시성을 확인하기 위해 여전히 여러 사용자가 필요합니다.
      preAllocatedVUs: 5,
      maxVUs: 10,
    },
  },
  thresholds: {
    // 테스트가 성공으로 간주되기 위한 조건입니다.
    // http_req_failed의 비율이 1% 미만이어야 하고,
    // 95%의 요청이 1.5초(1500ms) 안에 처리되어야 합니다.
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1500"],
  },
};

// --- 메인 테스트 로직 ---
export default function () {
  // 총 4번의 반복(iteration 0, 1, 2, 3)만 실행하고 테스트를 중단합니다.
  if (scenario.iterationInTest >= 4) {
    exec.test.abort("Completed 4 iterations.");
    return;
  }

  // CSV 데이터가 정상적으로 로드되었는지 첫 반복에서만 확인
  if (scenario.iterationInTest === 0) {
    console.log(`Loaded ${summoners.length} summoners from CSV.`);
  }

  // 매 반복마다 CSV 파일에서 다른 소환사 정보를 가져옵니다.
  // __ITER는 특정 실행기에서 문제가 있을 수 있으므로 scenario.iterationInTest를 사용합니다.
  const currentUser = summoners[scenario.iterationInTest % 4];
  const testSummonerName = currentUser.gameName;
  const testSummonerTag = currentUser.tagLine;

  // 디버깅을 위한 로그 추가
  console.log(
    `[Iteration ${scenario.iterationInTest}] Using summoner: ${testSummonerName}#${testSummonerTag}`
  );

  // URL 인코딩은 여전히 필수입니다.
  const encodedSummonerName = encodeURIComponent(testSummonerName);
  const encodedSummonerTag = encodeURIComponent(testSummonerTag);

  group(
    `Summoner Page Load for ${testSummonerName}#${testSummonerTag}`,
    function () {
      // 1. 계정 정보 조회
      const accountRes = http.get(
        `http://localhost:8080/api/v1/riot/accounts/${encodedSummonerName}/${encodedSummonerTag}`
      );
      check(accountRes, {
        "[Step 1] Account Info - status was 200": (r) => r.status === 200,
      });

      // 2. TFT 상태 및 매치 정보 병렬 조회
      const tftResponses = http.batch([
        [
          "GET",
          `http://localhost:8080/api/v1/tft/entries/${encodedSummonerName}/${encodedSummonerTag}`,
        ],
        [
          "GET",
          `http://localhost:8080/api/v1/tft/matches/${encodedSummonerName}/${encodedSummonerTag}`,
        ],
      ]);
      check(tftResponses[0], {
        "[Step 2] TFT Status - status was 200": (r) => r.status === 200,
      });
      check(tftResponses[1], {
        "[Step 2] TFT Matches - status was 200": (r) => r.status === 200,
      });

      // 3. 배지 정보 조회
      const tftBadgesRes = http.get(
        `http://localhost:8080/api/v1/tft/badges/${encodedSummonerName}/${encodedSummonerTag}`
      );
      check(tftBadgesRes, {
        "[Step 3] TFT Badges - status was 200": (r) => r.status === 200,
      });
    }
  );

  sleep(1);
}
