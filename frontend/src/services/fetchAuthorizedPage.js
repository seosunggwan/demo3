import axiosInstance from "../utils/axios";

/**
 * 📌 인증이 필요한 페이지에 접근할 때 사용
 * - `access token`을 헤더에 추가하여 백엔드 API 요청
 * - 만료된 토큰이면 자동으로 토큰 재발급 후 재시도 (axios 인터셉터 이용)
 * - 토큰 재발급 실패 시 로그인 페이지로 리디렉트
 */
const fetchAuthorizedPage = async (
  url,
  navigate,
  location,
  customErrorMsg = "인증에 실패했습니다. 다시 로그인해주세요."
) => {
  try {
    // axios 인스턴스 사용 (인터셉터에서 토큰 만료 처리)
    const response = await axiosInstance.post(url);

    // 요청 성공 시 응답 데이터 반환
    return response.data;
  } catch (error) {
    console.error("인증 요청 실패:", error);

    // 인증 오류가 해결되지 않은 경우 (토큰 재발급 후에도 실패한 경우)
    if (error.response?.status === 401) {
      alert(customErrorMsg);
      navigate("/login", { state: location.pathname }); // 로그인 후 원래 페이지로 돌아갈 수 있도록 `state`에 저장
    }
    return null;
  }
};

export default fetchAuthorizedPage;
