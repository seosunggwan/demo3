import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Box,
  Alert,
} from "@mui/material";

/**
 * 📌 회원가입 폼 컴포넌트
 * - 사용자 입력을 받아 백엔드로 전송
 * - 회원가입 성공 시 로그인 페이지로 이동
 */
const JoinForm = () => {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅

  // 사용자 입력값을 관리하는 상태 변수
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [city, setCity] = useState("");
  const [street, setStreet] = useState("");
  const [zipcode, setZipcode] = useState("");

  /**
   * 📌 회원가입 요청 함수 (axios 버전)
   * - 사용자가 입력한 정보를 백엔드(`/join` API)로 전송
   * - 회원가입 성공 시 로그인 페이지로 이동
   */
  const fetchJoin = async (credentials) => {
    setLoading(true);
    setError("");

    try {
      const response = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/join`,
        credentials,
        {
          withCredentials: true, // 쿠키 포함하여 요청
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      // 회원가입 성공 시 로그인 페이지로 이동
      if (response.status === 200) {
        navigate("/login", { replace: true });
      }
    } catch (error) {
      console.error("회원가입 오류:", error);

      if (error.response) {
        // 서버에서 응답한 에러 메시지 사용
        const errorMessage = error.response.data || "회원가입에 실패했습니다";
        setError(errorMessage);
      } else {
        setError("회원가입 요청 중 오류가 발생했습니다");
      }
    } finally {
      setLoading(false);
    }
  };

  /**
   * 📌 회원가입 폼 제출 이벤트 핸들러
   * - 기본 동작 방지 (`e.preventDefault()`)
   * - `fetchJoin` 함수 호출하여 서버로 데이터 전송
   */
  const joinHandler = async (e) => {
    e.preventDefault();
    const credentials = {
      email,
      username,
      password,
      city,
      street,
      zipcode,
    };
    fetchJoin(credentials);
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
            회원가입
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mt: 2, mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={joinHandler} mt={3}>
            <TextField
              label="이메일"
              variant="outlined"
              fullWidth
              margin="normal"
              type="email"
              name="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="이메일 입력"
              required
            />

            <TextField
              label="사용자 이름"
              variant="outlined"
              fullWidth
              margin="normal"
              type="text"
              name="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="사용자 이름 입력"
              required
            />

            <TextField
              label="비밀번호"
              variant="outlined"
              fullWidth
              margin="normal"
              type="password"
              name="password"
              autoComplete="off"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호 입력"
              required
            />

            <TextField
              label="도시"
              variant="outlined"
              fullWidth
              margin="normal"
              type="text"
              name="city"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="도시 입력"
              required
            />

            <TextField
              label="거리"
              variant="outlined"
              fullWidth
              margin="normal"
              type="text"
              name="street"
              value={street}
              onChange={(e) => setStreet(e.target.value)}
              placeholder="거리 입력"
              required
            />

            <TextField
              label="우편번호"
              variant="outlined"
              fullWidth
              margin="normal"
              type="text"
              name="zipcode"
              value={zipcode}
              onChange={(e) => setZipcode(e.target.value)}
              placeholder="우편번호 입력"
              required
            />

            <Button
              type="submit"
              variant="contained"
              fullWidth
              sx={{ mt: 3, mb: 2, py: 1.5 }}
              disabled={loading}
            >
              {loading ? "처리 중..." : "회원가입"}
            </Button>
          </Box>

          <Box mt={3} textAlign="center">
            <Typography variant="body2">
              이미 계정이 있으신가요?{" "}
              <Button
                color="primary"
                onClick={() => navigate("/login")}
                sx={{ fontWeight: "bold", textTransform: "none" }}
              >
                로그인하기
              </Button>
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default JoinForm;
