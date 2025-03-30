import { useNavigate } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import { useEffect } from "react";

/**
 * 📌 로그아웃 컴포넌트
 * - 백엔드에 로그아웃 요청을 보내고, 클라이언트의 로그인 상태를 초기화
 * - 로그아웃 성공 시 `localStorage`에서 토큰 제거 후 홈(`/`)으로 리디렉트
 */
const Logout = () => {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  const { logout } = useLogin(); // 전역 로그인 상태 관리 훅

  // 쿠키를 완전히 삭제하는 함수 - 더 적극적인 방법
  const deleteCookie = (name) => {
    // 다양한 경로와 도메인 조합으로 시도
    const domains = [
      "", // 현재 도메인
      "localhost",
      ".localhost",
      window.location.hostname,
      `.${window.location.hostname}`,
    ];

    const paths = ["/", "", "/api", "/login", "/logout", "/api/auth"];

    // 일반적인 JavaScript 방식으로 삭제 시도
    domains.forEach((domain) => {
      paths.forEach((path) => {
        const domainStr = domain ? `domain=${domain};` : "";
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; ${domainStr} path=${path}; secure; samesite=strict;`;
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; ${domainStr} path=${path};`;
        console.log(
          `쿠키 삭제 시도: ${name} - 도메인: ${domain || "현재"}, 경로: ${path}`
        );
      });
    });

    // 직접 document.cookie 값 확인 및 변경 시도
    try {
      // 쿠키 값 직접 확인
      console.log("현재 모든 쿠키:", document.cookie);

      // 브라우저 로컬 스토리지 및 세션 스토리지 정리
      localStorage.clear();
      sessionStorage.clear();

      // 실험적: 내장 쿠키 스토어 접근 시도 (일부 브라우저에서만 작동)
      if (window.cookieStore) {
        window.cookieStore.delete(name).then(() => {
          console.log(`${name} 쿠키가 cookieStore API로 삭제되었습니다.`);
        });
      }
    } catch (e) {
      console.error("쿠키 삭제 중 오류:", e);
    }
  };

  /**
   * 📌 로그아웃 요청 함수
   * - 백엔드로 `DELETE` 요청을 보내서 로그아웃 처리
   * - `refresh token` 삭제 및 무효화
   */
  const fetchLogout = async () => {
    try {
      console.log("로그아웃 전 쿠키:", document.cookie);

      // 올바른 로그아웃 엔드포인트로 수정: /auth/logout
      const response = await fetch(
        `${
          import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
        }/auth/logout`,
        {
          method: "POST", // DELETE에서 POST로 변경 (Spring Security 기본 설정)
          credentials: "include", // 쿠키 포함하여 요청 (Refresh Token 삭제)
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("access_token")}`,
          },
        }
      );

      console.log("로그아웃 응답 상태:", response.status);
      const responseText = await response.text();
      console.log("로그아웃 응답 내용:", responseText);

      // 스스로 쿠키 삭제를 시도 (여러 방법 시도)
      deleteCookie("refresh_token");

      console.log("로그아웃 후 쿠키:", document.cookie);

      // ✅ AuthContext의 logout 함수 사용
      logout();

      console.log("로컬스토리지 토큰 삭제됨");

      // 완전한 세션 정리를 위한 추가 조치
      localStorage.clear();
      sessionStorage.clear();

      // 브라우저 측 캐시 문제일 수 있으므로 항상 새로고침
      alert("완전히 로그아웃하기 위해 페이지를 새로고침합니다.");
      window.location.href = "/";
      return;

      // ✅ 로그아웃 후 홈 페이지(`/`)로 이동
      // navigate("/", { replace: true });
    } catch (error) {
      console.error("로그아웃 오류:", error);

      // 오류가 발생해도 클라이언트 측에서 로그아웃 처리
      deleteCookie("refresh_token");
      logout();
      navigate("/", { replace: true });

      alert("로그아웃 중 오류가 발생했지만, 로컬 로그아웃은 처리되었습니다.");
    }
  };

  // 컴포넌트 마운트 시 한 번만 실행
  useEffect(() => {
    fetchLogout();
  }, []);

  return null;
};

export default Logout;
