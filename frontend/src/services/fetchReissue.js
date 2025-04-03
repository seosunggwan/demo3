import { Cookies } from "react-cookie";
// fetchReissue.js (수정된 예시)
import axios from "axios";

const fetchReissue = async (retryCount = 0) => {
  const MAX_RETRIES = 2;
  try {
    console.log(`🔄 토큰 재발급 요청 시작 (시도 ${retryCount + 1}/${MAX_RETRIES + 1})`);

    // Refresh Token은 HttpOnly 쿠키로 전송되므로, 별도의 헤더 설정 없이 호출
    const response = await axios({
      method: "POST",
      url: `${import.meta.env.VITE_API_BASE_URL}/reissue`,
      withCredentials: true, // 쿠키가 자동으로 전송됨
    });

    if (response.status === 200) {
      // 새로운 Access Token은 응답 헤더에 포함되어 있습니다.
      const newToken = response.headers["access_token"];
      if (newToken) {
        window.localStorage.setItem("access_token", newToken);
        console.log("✅ 토큰 재발급 성공");
      }
      return true;
    }
  } catch (error) {
    console.error("❌ 토큰 재발급 요청 오류:", error);
    if (retryCount < MAX_RETRIES) {
      console.log(`🔄 ${retryCount + 1}초 후 재시도...`);
      await new Promise((resolve) => setTimeout(resolve, (retryCount + 1) * 1000));
      return fetchReissue(retryCount + 1);
    } else {
      console.error("❌ 최대 재시도 횟수 초과");
      window.localStorage.removeItem("access_token");
      return false;
    }
  }
  return false;
};

export default fetchReissue;