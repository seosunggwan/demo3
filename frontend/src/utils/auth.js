import { useNavigate } from "react-router-dom";
import axiosInstance from "./axios";

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
    if (error.response?.status === 401) {
      alert(errorMsg);
      navigate("/login", { state: location.pathname });
    }
    return null;
  }
};

export const redirectToLogin = (navigate, currentPath, message = "로그인이 필요합니다.") => {
  alert(message);
  navigate("/login", { state: currentPath });
};

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
