import { Cookies } from "react-cookie";
import axios from "axios";

/**
 * 📌 토큰 재발급 요청 함수 (axios 버전)
 * - 백엔드에 `refresh token`을 사용하여 `access token`을 재발급 요청
 * - 성공 시 새 `access token`을 `localStorage`에 저장
 * - 실패 시 재시도 로직 포함
 * @param {number} retryCount - 재시도 횟수 (기본값: 0)
 * @returns {Promise<boolean>} - 재발급 성공 여부
 */
const fetchReissue = async (retryCount = 0) => {
  const MAX_RETRIES = 2; // 최대 재시도 횟수
  const cookies = new Cookies();
  const refreshToken = cookies.get("refresh_token");

  try {
    console.log(
      `🔄 토큰 재발급 요청 시작 (시도 ${retryCount + 1}/${MAX_RETRIES + 1})`
    );

    if (!refreshToken) {
      console.error("❌ Refresh 토큰이 없습니다.");
      return false;
    }

    // axios로 토큰 재발급 요청
    const response = await axios({
      method: "POST",
      url: `${import.meta.env.VITE_API_BASE_URL}/reissue`,
      headers: {
        Authorization: `Bearer ${refreshToken}`,
        "Content-Type": "application/json",
      },
      withCredentials: true, // 쿠키 포함
    });

    // 요청 성공
    if (response.status === 200) {
      const newToken = response.headers["access_token"];
      console.log("✅ 토큰 재발급 성공");

      if (newToken) {
        window.localStorage.setItem("access_token", newToken);

        try {
          const tokenData = JSON.parse(atob(newToken.split(".")[1]));
          console.log("✅ 새 토큰 데이터:", tokenData);

          // 이메일 정보 업데이트
          const existingEmail = localStorage.getItem("email");
          if (!existingEmail && tokenData.email) {
            localStorage.setItem("email", tokenData.email);
          }
        } catch (error) {
          console.error("❌ 토큰 디코딩 실패:", error);
        }
      }

      return true;
    }
  } catch (error) {
    console.error("❌ 토큰 재발급 요청 오류:", error);

    // 서버 응답이 있는 경우 (HTTP 에러)
    if (error.response) {
      console.error(
        `❌ 토큰 재발급 실패 (상태 코드: ${error.response.status})`
      );
    }

    // 재시도 로직
    if (retryCount < MAX_RETRIES) {
      console.log(`🔄 ${retryCount + 1}초 후 재시도...`);
      await new Promise((resolve) =>
        setTimeout(resolve, (retryCount + 1) * 1000)
      );
      return fetchReissue(retryCount + 1);
    } else {
      // 최대 재시도 횟수 초과 시에만 로그아웃 처리
      console.error("❌ 최대 재시도 횟수 초과");
      window.localStorage.removeItem("access_token");
      cookies.set("refresh_token", null, {
        maxAge: 0,
        path: "/",
        secure: true,
        sameSite: "strict",
      });
      return false;
    }
  }

  return false;
};

export default fetchReissue;
