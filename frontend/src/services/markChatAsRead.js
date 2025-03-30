import axiosInstance from "../utils/axios";

/**
 * 채팅방 읽음 표시 처리 서비스 (axios 버전)
 * @param {string} roomId - 채팅방 ID
 * @returns {Promise<boolean>} - 처리 성공 여부
 */
const markChatAsRead = async (roomId) => {
  try {
    const token = localStorage.getItem("access_token");
    if (!token) {
      console.error("🚨 토큰이 없습니다. 로그인 후 이용해주세요.");
      return false;
    }

    console.log(`📤 채팅방 ${roomId} 읽음 표시 요청 시작`);

    // axios 인스턴스 사용 (인터셉터에서 토큰 만료 처리)
    await axiosInstance.post(`/chat/room/${roomId}/read`);

    console.log(`✅ 채팅방 ${roomId} 읽음 표시 성공`);
    return true;
  } catch (error) {
    console.error("❌ 채팅방 읽음 표시 처리 중 오류 발생:", error);

    if (error.response) {
      console.error(`❌ 상태 코드: ${error.response.status}`);
    }

    return false;
  }
};

export default markChatAsRead;
