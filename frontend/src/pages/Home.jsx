import { useLogin } from "../contexts/AuthContext";
import { Box, Typography, Container, Paper, Avatar } from "@mui/material";
import { keyframes } from "@mui/system";

// 간단한 팝업 애니메이션 정의
const popIn = keyframes`
  0% { opacity: 0; transform: scale(0.9); }
  70% { opacity: 1; transform: scale(1.05); }
  100% { transform: scale(1); }
`;

/**
 * 📌 홈 페이지 컴포넌트
 * - 로그인 상태에 따라 사용자 맞춤 환영 메시지 표시
 */
const Home = () => {
  const { isLoggedIn, loginUser } = useLogin(); // ✅ 로그인 상태 및 사용자 정보 가져오기

  // 로그인된 사용자의 이니셜 얻기
  const getInitials = (name) => {
    return name && name.charAt(0).toUpperCase();
  };

  return (
    <Container
      maxWidth="sm"
      sx={{
        my: 4,
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "calc(100vh - 200px)", // 네비게이션바 고려한 높이 조정
      }}
    >
      <Paper
        elevation={3}
        sx={{
          p: 4,
          borderRadius: 4,
          textAlign: "center",
          background: "linear-gradient(145deg, #ffffff, #f5f7ff)",
          overflow: "hidden",
          position: "relative",
          width: "100%",
          boxShadow: "0 10px 25px rgba(0,0,0,0.05)",
          animation: `${popIn} 0.6s ease-out`,
        }}
      >
        {/* 상단 장식 요소 */}
        <Box
          sx={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            height: "6px",
            background: "linear-gradient(90deg, #4F46E5, #7C74FF)",
          }}
        />

        <Box sx={{ py: 2 }}>
          {isLoggedIn && (
            <Avatar
              sx={{
                width: 64,
                height: 64,
                mx: "auto",
                mb: 2,
                bgcolor: "primary.main",
                boxShadow: "0 4px 12px rgba(79, 70, 229, 0.3)",
                fontSize: "1.5rem",
              }}
            >
              {getInitials(loginUser)}
            </Avatar>
          )}

          {isLoggedIn ? (
            <>
              <Typography
                variant="h4"
                component="h1"
                sx={{
                  fontWeight: 600,
                  color: "#4F46E5",
                  lineHeight: 1.2,
                  mb: 1,
                }}
              >
                {loginUser}님, 환영합니다!
              </Typography>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  mb: 1.5,
                }}
              >
                <span
                  role="img"
                  aria-label="celebrate"
                  style={{ fontSize: "1.5rem", marginRight: "4px" }}
                >
                  🎉
                </span>
              </Box>
              <Typography
                variant="body1"
                sx={{
                  color: "#6B7280",
                  fontSize: "1.2rem",
                }}
              >
                즐거운 하루 보내세요!
              </Typography>
            </>
          ) : (
            <>
              <Typography
                variant="h4"
                component="h1"
                sx={{
                  fontWeight: 600,
                  color: "#4F46E5",
                  mb: 1,
                }}
              >
                안녕하세요!
              </Typography>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  mb: 1.5,
                }}
              >
                <span
                  role="img"
                  aria-label="wave"
                  style={{ fontSize: "1.5rem" }}
                >
                  👋
                </span>
              </Box>
              <Typography
                variant="body1"
                sx={{
                  color: "#6B7280",
                  fontSize: "1.2rem",
                }}
              >
                로그인하시면 더 많은 기능을 이용하실 수 있습니다.
              </Typography>
            </>
          )}
        </Box>
      </Paper>
    </Container>
  );
};

export default Home;
