import { useNavigate } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import { useEffect } from "react";

/**
 * 📌 로그아웃 컴포넌트
 * - 백엔드에 로그아웃 요청을 보내고, 클라이언트의 로그인 상태를 초기화
 * - 로그아웃 성공 시 `localStorage`에서 토큰 제거 후 홈(`/`)으로 리디렉트
 */
const Logout = () => {
  const navigate = useNavigate();
  const { logout } = useLogin();

  /**
   * 📌 로그아웃 요청 함수
   * - 백엔드로 POST 요청을 보내서 로그아웃 처리
   * - refresh token 삭제 및 무효화
   */
  const fetchLogout = async () => {
    try {
      console.log("로그아웃 요청 시작");

      const response = await fetch(
        `${
          import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
        }/auth/logout`,
        {
          method: "POST",
          credentials: "include",
        }
      );

      console.log("로그아웃 응답 상태:", response.status);

      if (response.ok) {
        // AuthContext의 logout 함수 사용
        logout();
        console.log("로그아웃 성공");
        navigate("/", { replace: true });
      } else {
        console.error("로그아웃 실패:", response.status);
        // 실패해도 클라이언트 측에서 로그아웃 처리
        logout();
        navigate("/", { replace: true });
      }
    } catch (error) {
      console.error("로그아웃 오류:", error);
      // 오류가 발생해도 클라이언트 측에서 로그아웃 처리
      logout();
      navigate("/", { replace: true });
    }
  };

  useEffect(() => {
    fetchLogout();
  }, []);

  return null;
};

export default Logout;
