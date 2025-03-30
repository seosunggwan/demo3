import axiosInstance from "../utils/axios";
import { redirectToLogin, decodeToken } from "../utils/auth";

/**
 * 채팅 기록 조회 서비스 (axios 버전)
 * @param {string} roomId - 채팅방 ID
 * @param {Function} navigate - 라우팅 네비게이션 함수 (선택적)
 * @param {Object} location - 현재 위치 객체 (선택적)
 * @returns {Promise<Array>} - 채팅 메시지 배열
 */
const fetchChatHistory = async (roomId, navigate, location) => {
  try {
    console.log(`📥 채팅방 ${roomId} 기록 조회 시작`);
    const access_token = localStorage.getItem("access_token");

    if (!access_token) {
      console.error("🚨 토큰이 없습니다. 로그인 후 이용해주세요.");
      if (navigate && location) {
        redirectToLogin(navigate, location.pathname);
      }
      return [];
    }

    // 토큰 만료 확인 - 사전 체크 (불필요한 요청 방지)
    try {
      const tokenData = decodeToken(access_token);
      if (tokenData) {
        console.log(
          "🔍 토큰 만료 시간:",
          new Date(tokenData.exp * 1000).toLocaleString()
        );
      }
    } catch (error) {
      console.error("❌ 토큰 디코딩 중 오류 발생:", error);
      // 토큰 디코딩 실패해도 계속 진행 (백엔드에서 검증)
    }

    // 채팅 기록 요청 - axios 인스턴스 사용 (인터셉터에서 토큰 만료 처리)
    console.log(`📤 채팅방 ${roomId} 기록 요청 보내는 중...`);
    const response = await axiosInstance.get(`/chat/history/${roomId}`);

    const data = response.data;
    console.log(`✅ 채팅방 ${roomId} 기록 조회 성공: ${data.length}개 메시지`);
    return data;
  } catch (error) {
    console.error("❌ 채팅 기록 조회 중 오류 발생:", error);

    // 인증 오류가 해결되지 않은 경우 (토큰 재발급 후에도 실패)
    if (error.response?.status === 401) {
      console.error("❌ 인증 실패 (토큰 재발급 후에도 실패)");
      if (navigate && location) {
        redirectToLogin(navigate, location.pathname);
      }
    }

    return [];
  }
};

export default fetchChatHistory;
