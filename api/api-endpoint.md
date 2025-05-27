### Riot Account API

GET /api/v1/riot/accounts/{gameName}/{tagLine}

- 설명: 게임 이름과 태그로 라이엇 계정 조회
- 응답:
  ```json
  {
    "success": true,
    "message": "계정 조회 성공",
    "data": {
      "puuid": "RGAPI-12345678-1234-1234-1234-123456789012",
      "gameName": "Hide on bush",
      "tagLine": "KR1"
    }
  }
  ```

GET /api/v1/riot/accounts/renew/{gameName}/{tagLine}

- 설명: 라이엇 계정 정보 갱신
- 응답: 위와 동일

### TFT League Entry API

GET /api/v1/tft/entries/{encodedFullName}

- 설명: TFT 랭크 정보 조회 (encodedFullName = gameName#tagLine)
- 응답:
  ```json
  {
    "success": true,
    "message": "TFT 랭크 정보 조회 성공",
    "data": {
      "tier": "CHALLENGER",
      "rank": "I",
      "leaguePoints": 1247,
      "wins": 89,
      "losses": 45,
      "hotStreak": true
    }
  }
  ```

### Rivalry API

POST /api/v1/rivalries

- 설명: 새로운 라이벌리 생성
- 요청:
  ```json
  {
    "participants": [
      {
        "id": 123456,
        "side": "LEFT"
      },
      {
        "id": 789012,
        "side": "RIGHT"
      }
    ]
  }
  ```
- 응답:
  ```json
  {
    "success": true,
    "message": "라이벌리 생성 성공",
    "data": {
      "rivalryId": 987654
    }
  }
  ```

GET /api/v1/rivalries/{rivalryId}

- 설명: 라이벌리 상세 정보 조회
- 응답:
  ```json
  {
    "success": true,
    "message": "라이벌리 조회 성공",
    "data": {
      "rivalryId": 987654,
      "leftStats": [
        {
          "id": 123456,
          "fullName": "Hide on bush#KR1",
          "statistics": {
            "tier": "CHALLENGER",
            "rank": "I",
            "leaguePoints": 1247,
            "wins": 89,
            "losses": 45,
            "hotStreak": true
          }
        }
      ],
      "rightStats": [
        {
          "id": 789012,
          "fullName": "Faker#KR1",
          "statistics": {
            "tier": "DIAMOND",
            "rank": "II",
            "leaguePoints": 75,
            "wins": 45,
            "losses": 32,
            "hotStreak": false
          }
        }
      ],
      "createdAt": "2024-03-19T15:30:00Z"
    }
  }
  ```

### 에러 응답 예시

```json
{
  "success": false,
  "message": "소환사를 찾을 수 없습니다",
  "data": null
}
```

모든 API는 HTTP 상태 코드와 함께 위와 같은 BaseResponseDto 형식으로 응답합니다:

- 200: 성공
- 400: 잘못된 요청
- 404: 리소스를 찾을 수 없음
- 500: 서버 에러
