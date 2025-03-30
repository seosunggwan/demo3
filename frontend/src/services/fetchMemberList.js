import axiosInstance from "../utils/axios";
import { redirectToLogin } from "../utils/auth";

/**
 * 📌 회원 목록을 가져오는 함수 (axios 버전)
 * - GET 메서드로 요청하고 일반 사용자도 접근 가능
 * - 토큰 만료 시 자동으로 재발급 처리 (axios 인터셉터 활용)
 */
const fetchMemberList = async (navigate, location) => {
  try {
    // axios 인스턴스 사용 (인터셉터에서 토큰 만료 처리)
    const response = await axiosInstance.get("/member/list");

    return response.data;
  } catch (error) {
    console.error("회원 목록 조회 중 오류 발생:", error);

    // 인증 오류가 해결되지 않은 경우 (토큰 재발급 후에도 실패)
    if (error.response?.status === 401) {
      redirectToLogin(navigate, location.pathname);
      return null;
    }

    return [];
  }
};

export default fetchMemberList;
