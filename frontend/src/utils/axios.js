import axios from "axios";
import fetchReissue from "../services/fetchReissue";

/**
 * 기본 axios 인스턴스 설정
 */
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

/**
 * 요청 인터셉터 - 모든 요청에 토큰 추가
 */
axiosInstance.interceptors.request.use(
  (config) => {
    // FormData 요청인 경우 Content-Type 헤더를 제거
    // 브라우저가 자동으로 boundary와 함께 설정함
    if (config.data instanceof FormData) {
      delete config.headers["Content-Type"];
    } else if (!config.headers["Content-Type"]) {
      config.headers["Content-Type"] = "application/json";
    }

    const token = localStorage.getItem("access_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`요청 인터셉터: ${config.url}에 토큰 추가됨`);
    } else {
      console.log(`요청 인터셉터: ${config.url}에 토큰 없음`);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * 응답 인터셉터 - 401 에러 처리 및 토큰 자동 갱신
 */
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    console.log(
      `응답 에러: ${originalRequest?.url}, 상태: ${error.response?.status}`
    );

    if (error.response) {
      console.log("에러 응답 데이터:", error.response.data);
    }

    // 401 에러이고 재시도하지 않은 요청인 경우 토큰 갱신 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      console.log("🔄 401 에러 발생, 토큰 갱신 시도 중...");

      try {
        // 토큰 갱신 시도
        const refreshResult = await fetchReissue();

        if (refreshResult) {
          console.log("✅ 토큰 갱신 성공, 요청 재시도");

          // 새 토큰으로 헤더 업데이트
          const newToken = localStorage.getItem("access_token");
          originalRequest.headers.Authorization = `Bearer ${newToken}`;

          // 원래 요청 재시도
          return axios(originalRequest);
        } else {
          console.log("❌ 토큰 갱신 실패");
          return Promise.reject(
            new Error("인증이 만료되었습니다. 다시 로그인이 필요합니다.")
          );
        }
      } catch (refreshError) {
        console.error("❌ 토큰 갱신 중 오류 발생:", refreshError);
        return Promise.reject(
          new Error("인증이 만료되었습니다. 다시 로그인이 필요합니다.")
        );
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
