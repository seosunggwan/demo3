# Fetch API에서 Axios로의 마이그레이션 계획

## 1. 마이그레이션 개요

현재 프로젝트에서는 다양한 서비스 함수에서 네이티브 Fetch API를 사용하여 백엔드와 통신하고 있습니다. 이 계획은 모든 Fetch API 호출을 Axios로 마이그레이션하는 과정을 설명합니다.

## 2. 완료된 작업

- [x] `utils/axios.js`: Axios 인스턴스 및 인터셉터 설정 파일 생성
- [x] `utils/auth.js`: 인증 관련 유틸리티 함수 생성
- [x] `services/fetchAuthorizedPage.js`: Axios로 변환 완료
- [x] `services/fetchReissue.js`: Axios로 변환 완료
- [x] `services/fetchMemberList.js`: Axios로 변환 완료
- [x] `services/fetchGroupChatRooms.js`: Axios로 변환 완료
- [x] `services/fetchChatHistory.js`: Axios로 변환 완료
- [x] `services/createGroupChatRoom.js`: Axios로 변환 완료
- [x] `services/createChatRoom.js`: Axios로 변환 완료
- [x] `services/markChatAsRead.js`: Axios로 변환 완료
- [x] `services/joinGroupChatRoom.js`: Axios로 변환 완료
- [x] `services/Oauth2Redirect.js`: Axios로 변환 완료
- [x] 페이지 컴포넌트 내 직접 Fetch/Axios 호출 부분 확인 및 변환:
  - [x] `pages/Join.jsx`: Axios로 변환 완료
  - [x] `pages/Login.jsx`: Axios로 변환 완료
  - [x] `pages/ItemList.jsx`: Axios 인스턴스로 리팩토링 완료
  - [x] `pages/MyChatPage.jsx`: Axios 인스턴스로 리팩토링 완료

## 3. 남은 변환 작업

- [ ] 테스트 및 검증 (모든 API 호출이 정상적으로 작동하는지 확인)
- [ ] 추가 오류 발견 시 수정

## 4. 변환 패턴

### 4.1. Fetch 호출 → Axios 변환 패턴

```javascript
// 변환 전: Fetch
const response = await fetch(url, {
  method: "GET",
  credentials: "include",
  headers: {
    Authorization: `Bearer ${localStorage.getItem("access_token")}`,
  },
});

if (response.ok) {
  const data = await response.json();
  return data;
}
```

```javascript
// 변환 후: Axios
const response = await axiosInstance.get(url);
return response.data;
```

### 4.2. 토큰 재발급 및 재시도 로직

```javascript
// 변환 전: 수동 토큰 재발급 요청 및 재시도
if (response.status === 401) {
  const reissueSuccess = await fetchReissue();
  if (reissueSuccess) {
    // 재요청 로직
  } else {
    // 로그인 페이지 리디렉션
  }
}
```

```javascript
// 변환 후: Axios 인터셉터 활용
// utils/axios.js의 인터셉터에서 자동으로 처리
// 서비스 함수에서는 401 에러 처리만 남김
catch (error) {
  if (error.response?.status === 401) {
    redirectToLogin(navigate, location.pathname);
  }
}
```

## 5. 테스트 시 확인사항

1. 인증 요청 정상 작동 여부
2. 토큰 만료 시 자동 갱신 기능
3. 네트워크 오류 처리
4. 헤더와 파라미터 전달 방식
5. 요청/응답 데이터 타입 처리

## 6. 주의사항

- Axios 응답의 데이터는 `response.data`로 접근
- Axios는 자동으로 JSON 변환 (`.json()` 호출 필요 없음)
- Axios 에러 객체 구조 이해 필요 (`error.response`, `error.request` 등)
- URL 경로를 상대 경로로 변경 (baseURL 설정 활용)

## 7. 마이그레이션 이점

- 보일러플레이트 코드 감소
- 자동 토큰 갱신 및 재시도 로직 중앙화
- 에러 처리 일관성 향상
- 요청 인터셉트를 통한 확장성 개선
- 환경별 URL 설정 용이

## 8. 발견된 문제 및 수정 사항

- `MyChatPage.jsx`: 기본 axios 사용으로 인한 토큰 갱신 인터셉터 미적용 → axiosInstance로 변경
- URL 경로 중복 → baseURL 활용하여 상대 경로로 변경
