import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  Chip,
  Divider,
  IconButton,
  Box,
  Card,
  CardContent,
  CardActions,
  Button,
  Tabs,
  Tab,
  CircularProgress,
  Alert,
} from "@mui/material";
import { ThumbUp, Comment, Visibility, EmojiEvents } from "@mui/icons-material";
import {
  fetchTodayPopularBoards,
  fetchRecentPopularBoards,
} from "../services/boardService";
import { formatDateKorean, formatRelativeDate } from "../utils/dateUtils";

// 인기글 목록 컴포넌트
const PopularBoardList = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [popularBoards, setPopularBoards] = useState([]);
  const [tabValue, setTabValue] = useState(0);
  const [dateRange, setDateRange] = useState({
    days: 7,
    startDate: "",
    endDate: "",
  });
  const navigate = useNavigate();

  // 탭 변경 처리
  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);

    // 탭에 따라 다른 일수로 인기글 조회
    const days = newValue === 0 ? 1 : newValue === 1 ? 7 : 30;
    loadPopularBoards(days);
  };

  // 인기글 상세 페이지로 이동
  const handleBoardClick = (boardId) => {
    navigate(`/boards/${boardId}`);
  };

  // 인기글 목록 로드
  const loadPopularBoards = async (days) => {
    setIsLoading(true);
    setError(null);

    try {
      let response;

      if (days === 1) {
        response = await fetchTodayPopularBoards();
      } else {
        response = await fetchRecentPopularBoards(days);
      }

      setPopularBoards(response.boards);

      if (days > 1) {
        setDateRange({
          days,
          startDate: response.startDate,
          endDate: response.endDate,
        });
      }
    } catch (error) {
      console.error("인기글 목록을 불러오는데 실패했습니다:", error);
      setError("인기글 목록을 불러오는데 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  // 초기 로딩
  useEffect(() => {
    loadPopularBoards(1); // 기본적으로 오늘의 인기글 로드
  }, []);

  // 로딩 중 표시
  if (isLoading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  // 에러 표시
  if (error) {
    return (
      <Alert severity="error" sx={{ my: 2 }}>
        {error}
      </Alert>
    );
  }

  // 데이터가 없을 때 표시
  if (popularBoards.length === 0) {
    return (
      <Paper sx={{ p: 3, my: 2 }}>
        <Typography variant="subtitle1" align="center">
          {tabValue === 0
            ? "오늘의 인기글이 없습니다."
            : tabValue === 1
            ? "최근 7일간의 인기글이 없습니다."
            : "최근 30일간의 인기글이 없습니다."}
        </Typography>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, my: 2 }}>
      <Box sx={{ mb: 2 }}>
        <Typography
          variant="h6"
          sx={{
            mb: 1,
            fontWeight: "bold",
            display: "flex",
            alignItems: "center",
          }}
        >
          <EmojiEvents sx={{ mr: 1, color: "gold" }} />
          인기글
        </Typography>

        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          variant="fullWidth"
        >
          <Tab label="오늘" />
          <Tab label="최근 7일" />
          <Tab label="최근 30일" />
        </Tabs>
      </Box>

      <List sx={{ width: "100%" }}>
        {popularBoards.map((board, index) => (
          <React.Fragment key={`${board.selectionDate}-${board.boardId}`}>
            {index > 0 && <Divider component="li" />}
            <ListItem
              alignItems="flex-start"
              sx={{
                cursor: "pointer",
                "&:hover": { backgroundColor: "rgba(0, 0, 0, 0.04)" },
              }}
              onClick={() => handleBoardClick(board.boardId)}
            >
              <Box
                sx={{
                  mr: 2,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  width: 30,
                }}
              >
                <Chip
                  label={board.rank}
                  color={board.rank <= 3 ? "primary" : "default"}
                  size="small"
                  sx={{
                    fontWeight: "bold",
                    backgroundColor:
                      board.rank === 1
                        ? "gold"
                        : board.rank === 2
                        ? "silver"
                        : board.rank === 3
                        ? "#cd7f32"
                        : "default",
                  }}
                />
              </Box>

              <ListItemText
                primary={
                  <Box sx={{ display: "flex", alignItems: "center" }}>
                    <Typography
                      component="span"
                      variant="subtitle1"
                      sx={{
                        fontWeight: "medium",
                        mr: 1,
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {board.title}
                    </Typography>

                    {tabValue > 0 && (
                      <Chip
                        label={formatRelativeDate(board.selectionDate)}
                        size="small"
                        sx={{ ml: 1 }}
                        variant="outlined"
                      />
                    )}
                  </Box>
                }
                secondary={
                  <Box sx={{ display: "flex", alignItems: "center", mt: 0.5 }}>
                    <Typography
                      component="span"
                      variant="body2"
                      color="text.primary"
                      sx={{ mr: 2 }}
                    >
                      {board.authorName}
                    </Typography>

                    <Box
                      sx={{ display: "flex", alignItems: "center", gap: 1.5 }}
                    >
                      <Box sx={{ display: "flex", alignItems: "center" }}>
                        <Visibility
                          fontSize="small"
                          sx={{
                            mr: 0.5,
                            color: "text.secondary",
                            fontSize: "0.875rem",
                          }}
                        />
                        <Typography variant="body2" color="text.secondary">
                          {board.viewCount}
                        </Typography>
                      </Box>

                      <Box sx={{ display: "flex", alignItems: "center" }}>
                        <ThumbUp
                          fontSize="small"
                          sx={{
                            mr: 0.5,
                            color: "text.secondary",
                            fontSize: "0.875rem",
                          }}
                        />
                        <Typography variant="body2" color="text.secondary">
                          {board.likeCount}
                        </Typography>
                      </Box>

                      <Box sx={{ display: "flex", alignItems: "center" }}>
                        <Comment
                          fontSize="small"
                          sx={{
                            mr: 0.5,
                            color: "text.secondary",
                            fontSize: "0.875rem",
                          }}
                        />
                        <Typography variant="body2" color="text.secondary">
                          {board.commentCount}
                        </Typography>
                      </Box>
                    </Box>
                  </Box>
                }
              />
            </ListItem>
          </React.Fragment>
        ))}
      </List>

      {tabValue > 0 && (
        <Box
          sx={{
            mt: 2,
            p: 1,
            backgroundColor: "rgba(0, 0, 0, 0.04)",
            borderRadius: 1,
          }}
        >
          <Typography variant="caption" color="text.secondary">
            {`${formatDateKorean(dateRange.startDate)} ~ ${formatDateKorean(
              dateRange.endDate
            )} 기간의 인기글입니다.`}
          </Typography>
        </Box>
      )}

      <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
        <Button component={Link} to="/boards" color="primary" size="small">
          전체 게시글 보기
        </Button>
      </Box>
    </Paper>
  );
};

export default PopularBoardList;
