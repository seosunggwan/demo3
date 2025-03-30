import { useLogin } from "../contexts/AuthContext";
import { Box, Typography, Container, Paper, Avatar, Chip } from "@mui/material";
import { keyframes } from "@mui/system";
import { useLocation } from "react-router-dom";

// 간단한 팝업 애니메이션 정의
const popIn = keyframes`
  0% { opacity: 0; transform: scale(0.9); }
  70% { opacity: 1; transform: scale(1.05); }
  100% { transform: scale(1); }
`;

function MainContent() {
  const { isLoggedIn, loginUser } = useLogin();
  const location = useLocation();

  // Home 페이지이거나 로그인 상태가 아니면 null 반환 (컴포넌트 렌더링 안함)
  const isHomePage = location.pathname === "/" || location.pathname === "/home";
  if (!isLoggedIn || isHomePage) return null;

  // 로그인된 사용자의 이니셜 얻기
  const getInitials = (name) => {
    return name && name.charAt(0).toUpperCase();
  };

  return (
    <Box
      sx={{
        position: "fixed",
        top: 70,
        right: 24,
        zIndex: 1000,
        maxWidth: "300px",
        padding: 0,
        pointerEvents: "none", // 아래 요소 클릭 허용
      }}
    >
      <Paper
        elevation={2}
        sx={{
          p: 1.5,
          borderRadius: 3,
          display: "flex",
          alignItems: "center",
          background: "linear-gradient(145deg, #ffffff, #f9faff)",
          overflow: "hidden",
          position: "relative",
          boxShadow: "0 4px 15px rgba(0,0,0,0.03)",
          animation: `${popIn} 0.4s ease-out`,
          pointerEvents: "auto", // 버튼 클릭 허용
        }}
      >
        {/* 상단 장식 요소 */}
        <Box
          sx={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            height: "3px",
            background: "linear-gradient(90deg, #4F46E5, #7C74FF)",
          }}
        />

        <Avatar
          sx={{
            width: 36,
            height: 36,
            mr: 1.5,
            bgcolor: "primary.main",
            boxShadow: "0 2px 8px rgba(79, 70, 229, 0.2)",
            fontSize: "1rem",
          }}
        >
          {getInitials(loginUser)}
        </Avatar>

        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          }}
        >
          <Typography
            variant="subtitle1"
            sx={{
              fontWeight: 600,
              color: "primary.main",
              lineHeight: 1.2,
              fontSize: "0.95rem",
            }}
          >
            {loginUser}
          </Typography>
          <Chip
            label="로그인됨"
            size="small"
            color="success"
            variant="outlined"
            sx={{ height: 20, fontSize: "0.7rem", mt: 0.5 }}
          />
        </Box>
      </Paper>
    </Box>
  );
}

export default MainContent;
