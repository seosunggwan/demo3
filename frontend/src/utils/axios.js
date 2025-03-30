import axios from "axios";

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
 * 응답 인터셉터 - 401 에러 처리
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

    // 401 에러이고 재시도하지 않은 요청인 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      console.log("토큰 갱신 시도 중...");

      try {
        // 로컬 저장소에서 토큰 제거 후 로그인 페이지로 리다이렉트
        localStorage.removeItem("access_token");
        localStorage.removeItem("refresh_token");

        if (window.location.pathname !== "/login") {
          // 현재 페이지 경로를 저장하고 로그인 페이지로 리다이렉트
          const returnTo = window.location.pathname;
          window.location.href = `/login?returnTo=${encodeURIComponent(
            returnTo
          )}`;
        }

        return Promise.reject(
          new Error("인증이 만료되었습니다. 다시 로그인해주세요.")
        );
      } catch (refreshError) {
        console.error("인증 처리 중 오류 발생:", refreshError);
        return Promise.reject(new Error("로그인이 필요합니다"));
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
