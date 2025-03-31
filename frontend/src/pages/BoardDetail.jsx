import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import { fetchBoardDetail, deleteBoard } from "../services/boardService";
import { decodeToken, redirectToLogin } from "../utils/auth";
import { formatDateTime, formatDateKorean } from "../utils/dateUtils";
import {
  Container,
  Typography,
  Box,
  Paper,
  Divider,
  Button,
  Chip,
  Grid,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Snackbar,
  Alert,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import VisibilityIcon from "@mui/icons-material/Visibility";

const BoardDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, getAccessToken, loginUser } = useLogin();

  const [board, setBoard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAuthor, setIsAuthor] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });

  useEffect(() => {
    const checkAuthAndLoadData = async () => {
      try {
        const token = getAccessToken();
        if (!token || !isLoggedIn) {
          console.log("인증되지 않은 상태입니다.");
          setIsAuthenticated(false);
          redirectToLogin(navigate, `/boards/${id}`);
          return;
        }

        // 토큰 유효성 검증
        const tokenData = decodeToken(token);
        if (!tokenData) {
          console.log("토큰이 유효하지 않습니다.");
          setIsAuthenticated(false);
          redirectToLogin(navigate, `/boards/${id}`);
          return;
        }

        setIsAuthenticated(true);
        await loadBoardData();
      } catch (error) {
        console.error("인증 확인 중 오류 발생:", error);
        setError("게시글을 불러오는데 실패했습니다.");
        setLoading(false);
      }
    };

    checkAuthAndLoadData();
  }, [id, isLoggedIn, navigate, getAccessToken]);

  const loadBoardData = async () => {
    try {
      setLoading(true);
      const data = await fetchBoardDetail(id);
      setBoard(data);

      // 로그인한 사용자가 작성자인지 확인
      setIsAuthor(loginUser === data.authorName);
    } catch (error) {
      console.error(
        `게시글 상세 정보를 불러오는데 실패했습니다 (ID: ${id}):`,
        error
      );

      if (error.response?.status === 401) {
        setIsAuthenticated(false);
        redirectToLogin(navigate, `/boards/${id}`);
      } else if (error.response?.status === 404) {
        setError("게시글을 찾을 수 없습니다.");
      } else {
        setError("게시글을 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  // 게시글 삭제 확인 대화상자 표시
  const handleDeleteClick = () => {
    setConfirmDialog({
      open: true,
    });
  };

  // 게시글 삭제 처리
  const handleDeleteConfirm = async () => {
    try {
      await deleteBoard(id);

      // 성공 메시지 표시
      setSnackbar({
        open: true,
        message: "게시글이 삭제되었습니다.",
        severity: "success",
      });

      // 게시글 목록 페이지로 이동
      setTimeout(() => {
        navigate("/boards");
      }, 1500);
    } catch (error) {
      console.error("게시글 삭제 중 오류 발생:", error);

      // 에러 메시지 표시
      setSnackbar({
        open: true,
        message: "게시글 삭제에 실패했습니다.",
        severity: "error",
      });

      // 대화상자 닫기
      setConfirmDialog({
        open: false,
      });
    }
  };

  // 대화상자 닫기
  const handleDialogClose = () => {
    setConfirmDialog({
      open: false,
    });
  };

  // 스낵바 닫기
  const handleSnackbarClose = () => {
    setSnackbar({
      ...snackbar,
      open: false,
    });
  };

  // 로딩 중이면 로딩 인디케이터 표시
  if (loading) {
    return (
      <Container>
        <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  // 에러가 있으면 에러 메시지 표시
  if (error) {
    return (
      <Container>
        <Paper sx={{ p: 3, mt: 3 }}>
          <Typography color="error">{error}</Typography>
          <Button
            variant="contained"
            component={Link}
            to="/boards"
            sx={{ mt: 2 }}
            startIcon={<ArrowBackIcon />}
          >
            게시글 목록으로 돌아가기
          </Button>
        </Paper>
      </Container>
    );
  }

  return (
    <Container>
      <Paper sx={{ p: 3, mt: 4, mb: 4 }}>
        {board && (
          <>
            {/* 게시글 헤더 (제목, 작성 정보) */}
            <Box sx={{ mb: 3 }}>
              <Typography variant="h4" gutterBottom>
                {board.title}
              </Typography>
              <Grid container spacing={2}>
                <Grid item>
                  <Typography variant="subtitle1" color="text.secondary">
                    작성자: {board.authorName}
                  </Typography>
                </Grid>
                <Grid item>
                  <Typography variant="subtitle1" color="text.secondary">
                    작성일: {formatDateKorean(board.createdTime)}{" "}
                    {new Date(board.createdTime).toLocaleTimeString("ko-KR")}
                  </Typography>
                </Grid>
                <Grid item>
                  <Chip
                    icon={<VisibilityIcon fontSize="small" />}
                    label={`조회수: ${board.viewCount}`}
                    size="small"
                    variant="outlined"
                  />
                </Grid>
              </Grid>
            </Box>

            <Divider sx={{ mb: 3 }} />

            {/* 게시글 내용 */}
            <Box sx={{ mb: 4 }}>
              <Typography
                variant="body1"
                component="div"
                sx={{
                  lineHeight: 1.7,
                  whiteSpace: "pre-wrap",
                  wordBreak: "break-word",
                  minHeight: "200px",
                }}
              >
                {board.content}
              </Typography>
            </Box>

            <Divider sx={{ mb: 3 }} />

            {/* 수정일 표시 */}
            {board.updatedTime && board.updatedTime !== board.createdTime && (
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ mb: 2, display: "block" }}
              >
                마지막 수정: {formatDateTime(board.updatedTime)}
              </Typography>
            )}

            {/* 버튼 그룹 */}
            <Box
              sx={{ display: "flex", justifyContent: "space-between", mt: 2 }}
            >
              <Button
                variant="outlined"
                component={Link}
                to="/boards"
                startIcon={<ArrowBackIcon />}
              >
                목록으로
              </Button>

              <Box>
                {isAuthor && (
                  <>
                    <Button
                      variant="outlined"
                      color="primary"
                      component={Link}
                      to={`/boards/${id}/edit`}
                      startIcon={<EditIcon />}
                      sx={{ mr: 1 }}
                    >
                      수정
                    </Button>
                    <Button
                      variant="outlined"
                      color="error"
                      onClick={handleDeleteClick}
                      startIcon={<DeleteIcon />}
                    >
                      삭제
                    </Button>
                  </>
                )}
              </Box>
            </Box>
          </>
        )}
      </Paper>

      {/* 삭제 확인 대화상자 */}
      <Dialog open={confirmDialog.open} onClose={handleDialogClose}>
        <DialogTitle>게시글 삭제</DialogTitle>
        <DialogContent>
          <DialogContentText>
            정말 이 게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수
            없습니다.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDialogClose}>취소</Button>
          <Button onClick={handleDeleteConfirm} color="error" autoFocus>
            삭제
          </Button>
        </DialogActions>
      </Dialog>

      {/* 스낵바 (알림) */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          onClose={handleSnackbarClose}
          severity={snackbar.severity}
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default BoardDetail;
