import { useNavigate } from "react-router-dom";
import axiosInstance from "./axios";

/**
 * 📌 인증이 필요한 페이지 접근 함수 (axios 버전)
 * @param {string} url - 요청 URL
 * @param {function} navigate - 라우팅 함수
 * @param {object} location - 현재 위치 정보
 * @param {string} [errorMsg] - 사용자 정의 에러 메시지
 * @returns {Promise<string>} - 서버 응답 데이터
 */
export const fetchAuthorizedPage = async (
  url,
  navigate,
  location,
  errorMsg = "인증에 실패했습니다. 다시 로그인해주세요."
) => {
  try {
    const response = await axiosInstance.post(url);
    return response.data;
  } catch (error) {
    console.error("인증 요청 실패:", error);

    // 인증 오류가 해결되지 않은 경우
    if (error.response?.status === 401) {
      alert(errorMsg);
      navigate("/login", { state: location.pathname });
    }
    return null;
  }
};

/**
 * 📌 로그인 페이지로 리디렉션하는 함수
 * @param {string} currentPath - 현재 경로
 * @param {string} [message] - 알림 메시지
 */
export const redirectToLogin = (
  navigate,
  currentPath,
  message = "로그인이 필요합니다."
) => {
  alert(message);
  navigate("/login", { state: currentPath });
};

/**
 * 📌 토큰 디코딩 함수
 * @param {string} token - JWT 토큰
 * @returns {object|null} 디코딩된 토큰 데이터
 */
export const decodeToken = (token) => {
  try {
    if (!token) return null;
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(base64));
  } catch (error) {
    console.error("토큰 디코딩 실패:", error);
    return null;
  }
};
