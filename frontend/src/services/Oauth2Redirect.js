import { useNavigate, useSearchParams } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import axios from "axios";
import { decodeToken } from "../utils/auth";

/**
 * 📌 OAuth2 로그인 후 JWT 저장 및 사용자 상태 업데이트 (axios 버전)
 * - 백엔드(`/oauth2-jwt-header`)로 요청하여 `access token`을 받아옴
 * - 받은 `access token`을 `localStorage`에 저장하여 이후 API 요청에서 사용
 * - 로그인 성공 시 사용자 상태를 업데이트하고 홈(`/`)으로 리디렉트
 */
const OAuth2Redirect = () => {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  const { setIsLoggedIn, setLoginUser, login } = useLogin(); // 전역 로그인 상태 관리 훅
  const [queryParams] = useSearchParams(); // URL에서 `name` 파라미터 가져오기

  /**
   * 📌 백엔드에서 `httpOnly 쿠키`로 전달받은 JWT를 `localStorage`에 저장
   * - `access token`을 `localStorage`에 저장하여 인증에 사용
   * - 백엔드에서 전달받은 `name`을 `localStorage`에 저장하여 UI에서 활용
   */
  const OAuth2JwtHeaderFetch = async () => {
    try {
      // axios 요청 설정
      const response = await axios.post(
        "http://localhost:8080/oauth2-jwt-header",
        {},
        {
          withCredentials: true, // 쿠키 기반 인증 포함
        }
      );

      // access_token 헤더 확인
      const token = response.headers["access_token"];
      const name = queryParams.get("name");
      // URL에서 이메일 파라미터 가져오기
      const email = queryParams.get("email");

      if (token) {
        console.log("✅ 토큰 받음:", token.substring(0, 20) + "...");

        // JWT 토큰 디코딩하여 사용자 정보 추출
        try {
          const tokenData = decodeToken(token);
          console.log("✅ 토큰 데이터:", tokenData);

          // URL에서 받은 이메일 사용 (백엔드에서 전달한 실제 이메일)
          const userEmail =
            email ||
            tokenData.email ||
            `${tokenData.username.replace(" ", ".")}@oauth.user`;

          // AuthContext의 login 함수 사용
          login(token, userEmail, tokenData.role || "USER");

          console.log("✅ 로그인 성공 - 이름:", name);
          console.log("✅ 로그인 성공 - 이메일:", userEmail);
        } catch (error) {
          console.error("❌ 토큰 디코딩 실패:", error);

          // 기본 방식으로 저장 - URL에서 받은 이메일 사용
          window.localStorage.setItem("access_token", token);
          window.localStorage.setItem("name", name);

          // URL에서 전달받은 이메일 사용 (없는 경우에만 가상 이메일 생성)
          if (email) {
            window.localStorage.setItem("email", email);
          } else {
            window.localStorage.setItem(
              "email",
              `${name.replace(" ", ".")}@oauth.user`
            );
          }

          // 전역 로그인 상태 업데이트
          setIsLoggedIn(true);
          setLoginUser(name);
        }
      } else {
        console.error("❌ 서버에서 토큰을 반환하지 않음");
        alert("로그인 처리 중 오류가 발생했습니다.");
      }

      // 로그인 성공 후 홈(`/`)으로 이동
      navigate("/", { replace: true });
    } catch (error) {
      console.error("❌ 요청 오류:", error);
      alert("로그인 처리 중 오류가 발생했습니다.");
      navigate("/login", { replace: true });
    }
  };

  // OAuth2 로그인 후 `access token` 요청 및 저장
  OAuth2JwtHeaderFetch();

  return null; // 컴포넌트 UI가 필요 없으므로 `null` 반환
};

export default OAuth2Redirect;
