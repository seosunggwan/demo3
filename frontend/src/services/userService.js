import axios from "axios";
import axiosInstance from "../utils/axios";

/**
 * 사용자 프로필 정보를 가져오는 함수
 *
 * @returns {Promise} 프로필 정보 또는 에러
 */
export const fetchUserProfile = async () => {
  try {
    const token = localStorage.getItem("access_token");
    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    const response = await axiosInstance.get("/api/users/profile");

    return response.data;
  } catch (error) {
    console.error("프로필 정보 가져오기 실패:", error);

    // 토큰이 만료된 경우
    if (error.response && error.response.status === 401) {
      // 리프레시 토큰으로 재시도 로직을 여기에 추가할 수 있음
      localStorage.removeItem("access_token");
      throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
    }

    throw error;
  }
};

/**
 * 사용자 프로필 정보를 업데이트하는 함수
 *
 * @param {Object} profileData 업데이트할 프로필 정보 객체
 * @returns {Promise} 업데이트된 프로필 정보 또는 에러
 */
export const updateUserProfile = async (profileData) => {
  try {
    const token = localStorage.getItem("access_token");
    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    const response = await axiosInstance.put("/api/users/profile", profileData);

    return response.data;
  } catch (error) {
    console.error("프로필 정보 업데이트 실패:", error);

    // 토큰이 만료된 경우
    if (error.response && error.response.status === 401) {
      // 리프레시 토큰으로 재시도 로직을 여기에 추가할 수 있음
      localStorage.removeItem("access_token");
      throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
    }

    throw error;
  }
};
