import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
  Button,
  Alert,
} from "@mui/material";
import {
  getPopularBoards,
  getPopularBoardsByDate,
  calculatePopularBoards,
} from "../services/popularBoardService";
import { useNavigate } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import { decodeToken, redirectToLogin } from "../utils/auth";

const PopularBoards = () => {
  const navigate = useNavigate();
  const { loginUser, isLoggedIn, getAccessToken } = useLogin();
  const [popularBoards, setPopularBoards] = useState([]);
  const [selectedDate, setSelectedDate] = useState("today");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const checkAuthAndLoadData = async () => {
      const token = getAccessToken();
      console.log("현재 로그인 상태:", isLoggedIn);
      console.log("현재 토큰:", token);
      console.log("현재 로그인 사용자:", loginUser);

      if (!token || !isLoggedIn) {
        console.log("인증되지 않은 상태입니다. 로그인 페이지로 이동합니다.");
        redirectToLogin(navigate, "/boards/popular");
        return;
      }

      try {
        const decoded = decodeToken(token);
        console.log("디코딩된 토큰 정보:", decoded);
        if (!decoded) {
          console.log("토큰이 유효하지 않습니다.");
          redirectToLogin(navigate, "/boards/popular");
          return;
        }

        setIsAdmin(decoded.role === "ADMIN");

        setLoading(true);
        setError(null);

        let response;
        if (selectedDate === "today") {
          response = await getPopularBoards();
        } else {
          response = await getPopularBoardsByDate(selectedDate);
        }

        if (response && response.boards) {
          setPopularBoards(response.boards);
        }
      } catch (error) {
        console.error("데이터 로딩 중 오류 발생:", error);
        setError("인기글을 불러오는데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    checkAuthAndLoadData();
  }, [selectedDate, isLoggedIn, navigate, getAccessToken]);

  const handleDateChange = (event, newValue) => {
    setSelectedDate(newValue);
  };

  const handleCalculate = async () => {
    if (!isLoggedIn || !isAdmin) {
      alert("관리자만 인기글 결산을 실행할 수 있습니다.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await calculatePopularBoards();
      const response = await getPopularBoards();
      if (response && response.boards) {
        setPopularBoards(response.boards);
      }
    } catch (error) {
      setError("인기글 결산에 실패했습니다.");
      console.error("Error calculating popular boards:", error);
    } finally {
      setLoading(false);
    }
  };

  if (!isLoggedIn) {
    return null;
  }

  return (
    <Box sx={{ width: "100%", maxWidth: 800, mx: "auto", p: 2 }}>
      <Typography variant="h4" gutterBottom>
        인기글
      </Typography>

      {isAdmin && (
        <Box sx={{ mb: 2 }}>
          <Button
            variant="contained"
            color="primary"
            onClick={handleCalculate}
            disabled={loading}
          >
            {loading ? "결산 중..." : "인기글 결산 실행"}
          </Button>
        </Box>
      )}

      {error && <Alert severity="error">{error}</Alert>}

      <Tabs value={selectedDate} onChange={handleDateChange} sx={{ mb: 2 }}>
        <Tab value="today" label="오늘" />
        <Tab value="7" label="7일" />
        <Tab value="30" label="30일" />
      </Tabs>

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", p: 3 }}>
          <CircularProgress />
        </Box>
      ) : popularBoards.length === 0 ? (
        <Typography variant="body1" align="center">
          인기 게시글이 없습니다.
        </Typography>
      ) : (
        <List>
          {popularBoards.map((board) => (
            <ListItem
              key={board.id}
              button
              onClick={() => navigate(`/boards/${board.id}`)}
              sx={{
                "&:hover": {
                  backgroundColor: "rgba(0, 0, 0, 0.04)",
                },
              }}
            >
              <ListItemText
                primary={
                  <Box display="flex" alignItems="center">
                    <Typography
                      variant="h6"
                      color="primary"
                      sx={{ mr: 2, minWidth: 30 }}
                    >
                      #{board.rank}
                    </Typography>
                    <Typography variant="subtitle1">{board.title}</Typography>
                  </Box>
                }
                secondary={
                  <Box display="flex" gap={2} mt={1}>
                    <Typography variant="body2" color="text.secondary">
                      조회수: {board.viewCount}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      좋아요: {board.likeCount}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      댓글: {board.commentCount}
                    </Typography>
                  </Box>
                }
              />
            </ListItem>
          ))}
        </List>
      )}
    </Box>
  );
};

export default PopularBoards;
