import axiosInstance from "../utils/axios";
import { redirectToLogin } from "../utils/auth";

/**
 * 📌 개인 채팅방 생성 함수 (axios 버전)
 * - 사용자 ID를 받아 개인 채팅방 생성
 * - 토큰 만료 시 자동으로 재발급 처리 (axios 인터셉터 활용)
 */
const createChatRoom = async (otherMemberId, navigate, location) => {
  try {
    // axios 인스턴스 사용 (인터셉터에서 토큰 만료 처리)
    const response = await axiosInstance.post(
      `/chat/room/private/create`,
      null,
      {
        params: { otherMemberId },
      }
    );

    return response.data;
  } catch (error) {
    console.error("채팅방 생성 중 오류 발생:", error);

    // 인증 오류가 해결되지 않은 경우 (토큰 재발급 후에도 실패)
    if (error.response?.status === 401) {
      redirectToLogin(navigate, location.pathname);
    }

    return null;
  }
};

export default createChatRoom;
