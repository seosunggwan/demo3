import axios from "axios";
import fetchReissue from "../services/fetchReissue";

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

axiosInstance.interceptors.request.use(
  (config) => {
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
    if (config.params) {
      console.log(`요청 인터셉터: ${config.url}에 파라미터:`, config.params);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    console.log(`응답 에러: ${originalRequest?.url}, 상태: ${error.response?.status}`);
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      console.log("🔄 401 에러 발생, 토큰 갱신 시도 중...");
      try {
        const refreshResult = await fetchReissue();
        if (refreshResult) {
          console.log("✅ 토큰 갱신 성공, 요청 재시도");
          const newToken = localStorage.getItem("access_token");
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return axiosInstance(originalRequest);
        } else {
          console.log("❌ 토큰 갱신 실패");
          return Promise.reject(new Error("인증이 만료되었습니다. 다시 로그인이 필요합니다."));
        }
      } catch (refreshError) {
        console.error("❌ 토큰 갱신 중 오류 발생:", refreshError);
        return Promise.reject(new Error("인증이 만료되었습니다. 다시 로그인이 필요합니다."));
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
