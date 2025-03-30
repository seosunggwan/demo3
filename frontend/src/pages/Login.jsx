import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import axios from "axios";
import {
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Box,
  Grid,
  Divider,
  Alert,
} from "@mui/material";

/**
 * 📌 로그인 폼 컴포넌트
 * - 이메일과 비밀번호를 입력받아 백엔드로 로그인 요청
 * - 로그인 성공 시 `localStorage`에 저장 후 상태 업데이트 및 `/home` 또는 이전 페이지로 이동
 */
const LoginForm = () => {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅
  const location = useLocation(); // 이전 페이지 URL 정보 가져오기
  const { login } = useLogin(); // 전역 로그인 상태 관리 훅

  const prevUrl = location.state || "/home"; // ✅ 기본 이동 경로 "/home"

  // ✅ 로그인 입력값 상태 관리
  const [email, setEmail] = useState(""); // 이메일 입력
  const [password, setPassword] = useState(""); // 비밀번호 입력
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  /**
   * 📌 로그인 요청 함수 (axios 버전)
   * - 입력받은 이메일과 비밀번호를 백엔드(`/login` API)로 전송
   */
  const fetchLogin = async (credentials) => {
    setLoading(true);
    setError("");

    try {
      // URLSearchParams 형식으로 데이터 전송
      const params = new URLSearchParams();
      params.append("email", credentials.email);
      params.append("password", credentials.password);

      const response = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/login`,
        params,
        {
          withCredentials: true, // 쿠키 포함하여 요청
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      );

      console.log("🔍 [서버 응답 상태]:", response.status);
      console.log("📩 [서버 응답 헤더]:", response.headers);
      console.log("📦 [서버 응답 데이터]:", response.data);

      // 응답 데이터에서 필요한 정보 추출
      const data = response.data;

      // JWT 토큰, 이메일, 역할 저장
      const access_token = data.access_token;
      const refresh_token = data.refresh_token; // 리프레시 토큰 추출
      const email = data.email;
      const role = data.role;

      if (access_token) {
        // AuthContext의 login 함수 사용 (refresh_token 추가)
        login(access_token, email, role, refresh_token);

        console.log("🔐 [저장 완료] JWT 토큰:", access_token);
        console.log("🔐 [저장 완료] 리프레시 토큰:", refresh_token);
        console.log("🔐 [저장 완료] 사용자 이메일:", email);
        console.log("🔐 [저장 완료] 사용자 역할:", role);

        // 로그인 완료 후, 이전 페이지가 있으면 이동, 없으면 "/home"으로 이동
        console.log(`🚀 [페이지 이동] ${prevUrl}`);
        navigate(prevUrl, { replace: true });
      } else {
        console.error("❌ [오류] 서버에서 토큰을 반환하지 않음");
        setError("서버에서 인증 토큰을 받지 못했습니다");
      }
    } catch (error) {
      console.error("🚨 [요청 오류] 로그인 요청 중 오류 발생:", error);

      if (error.response) {
        // 서버에서 응답한 에러 메시지 사용
        const errorMessage = error.response.data || "로그인에 실패했습니다";
        setError(errorMessage);
      } else {
        setError("로그인 요청 중 오류가 발생했습니다");
      }
    } finally {
      setLoading(false);
    }
  };

  /**
   * 📌 로그인 폼 제출 이벤트 핸들러
   * - 기본 동작 방지 (`e.preventDefault()`)
   * - `fetchLogin` 함수 호출하여 서버로 데이터 전송
   */
  const loginHandler = async (e) => {
    e.preventDefault();
    console.log("📥 [사용자 입력] 이메일:", email);
    console.log("📥 [사용자 입력] 비밀번호:", "********"); // 비밀번호는 로그에 노출되지 않도록 처리

    const credentials = { email, password }; // email, password만 포함
    fetchLogin(credentials);
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 8 }}>
      <Paper elevation={3} sx={{ borderRadius: 3, overflow: "hidden" }}>
        <Box p={4}>
          <Typography
            variant="h4"
            align="center"
            gutterBottom
            fontWeight="bold"
          >
            로그인
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mt: 2, mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={loginHandler} mt={3}>
            <TextField
              label="이메일"
              variant="outlined"
              fullWidth
              margin="normal"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="이메일 입력"
              required
            />

            <TextField
              label="비밀번호"
              variant="outlined"
              fullWidth
              margin="normal"
              type="password"
              autoComplete="off"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호 입력"
              required
            />

            <Button
              type="submit"
              variant="contained"
              fullWidth
              sx={{ mt: 3, mb: 2, py: 1.5 }}
              disabled={loading}
            >
              {loading ? "로그인 중..." : "로그인"}
            </Button>
          </Box>

          <Divider sx={{ my: 3 }}>또는</Divider>

          <Typography variant="h6" align="center" gutterBottom>
            소셜 로그인
          </Typography>

          <Grid container spacing={2} justifyContent="center" sx={{ mt: 2 }}>
            <Grid item>
              <a
                href={`${
                  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
                }/oauth2/authorization/naver`}
              >
                <img
                  src="naver_icon.png"
                  alt="naver"
                  style={{ width: 40, height: 40, borderRadius: "50%" }}
                />
              </a>
            </Grid>
            <Grid item>
              <a
                href={`${
                  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
                }/oauth2/authorization/google`}
              >
                <img
                  src="google_icon.png"
                  alt="google"
                  style={{ width: 40, height: 40, borderRadius: "50%" }}
                />
              </a>
            </Grid>
            <Grid item>
              <a
                href={`${
                  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
                }/oauth2/authorization/github`}
              >
                <img
                  src="github_icon.png"
                  alt="github"
                  style={{ width: 40, height: 40, borderRadius: "50%" }}
                />
              </a>
            </Grid>
          </Grid>

          <Box mt={3} textAlign="center">
            <Typography variant="body2">
              계정이 없으신가요?{" "}
              <Button
                color="primary"
                onClick={() => navigate("/join")}
                sx={{ fontWeight: "bold", textTransform: "none" }}
              >
                회원가입
              </Button>
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default LoginForm;
