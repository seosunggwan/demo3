import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import {
  fetchBoardDetail,
  createBoard,
  updateBoard,
} from "../services/boardService";
import { decodeToken, redirectToLogin } from "../utils/auth";
import {
  Container,
  Typography,
  Box,
  Paper,
  TextField,
  Button,
  Divider,
  CircularProgress,
  Snackbar,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import CloseIcon from "@mui/icons-material/Close";

const BoardForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, getAccessToken } = useLogin();

  const [isEdit, setIsEdit] = useState(!!id);
  const [board, setBoard] = useState({
    title: "",
    content: "",
  });
  const [validation, setValidation] = useState({
    title: { error: false, message: "" },
    content: { error: false, message: "" },
  });
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(isEdit);
  const [error, setError] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    action: null,
  });

  useEffect(() => {
    const checkAuthAndLoadData = async () => {
      try {
        const token = getAccessToken();
        // 로그인 상태가 아닌 경우에만 리다이렉트
        if (!isLoggedIn) {
          console.log("로그인되지 않은 상태입니다.");
          setIsAuthenticated(false);
          redirectToLogin(
            navigate,
            isEdit ? `/boards/${id}/edit` : "/boards/new"
          );
          return;
        }

        // 토큰이 없는 경우에만 오류 처리 (토큰 유효성은 axiosInstance에서 처리)
        if (!token) {
          console.log("인증 토큰이 없습니다.");
          setError("로그인이 필요합니다. 인증 토큰이 없습니다.");
          return;
        }

        // 토큰 유효성 검증 및 만료 확인
        try {
          const tokenData = decodeToken(token);
          if (tokenData) {
            console.log(
              "🔍 토큰 만료 시간:",
              new Date(tokenData.exp * 1000).toLocaleString()
            );
            const now = new Date().getTime();
            const expTime = tokenData.exp * 1000;

            // 이미 만료된 경우
            if (expTime <= now) {
              console.log("⚠️ 토큰이 이미 만료되었습니다.");
              // 리프레시 토큰으로 갱신 시도
              const refreshed = await import("../services/fetchReissue").then(
                (module) => module.default()
              );
              if (!refreshed) {
                console.log("❌ 토큰 갱신 실패");
                setIsAuthenticated(false);
                redirectToLogin(
                  navigate,
                  isEdit ? `/boards/${id}/edit` : "/boards/new",
                  "인증이 만료되었습니다. 다시 로그인해주세요."
                );
                return;
              }
              console.log("✅ 토큰 갱신 성공");
            }
          }
        } catch (tokenError) {
          console.error("❌ 토큰 검증 중 오류 발생:", tokenError);
        }

        setIsAuthenticated(true);

        // 수정 모드인 경우 기존 게시글 데이터 로드
        if (isEdit) {
          await loadBoardData();
        } else {
          setInitialLoading(false);
        }
      } catch (error) {
        console.error("인증 확인 중 오류 발생:", error);
        setError("페이지를 불러오는데 실패했습니다.");
        setInitialLoading(false);
      }
    };

    checkAuthAndLoadData();
  }, [id, isEdit, isLoggedIn, navigate, getAccessToken]);

  // 기존 게시글 데이터 로드 (수정 모드)
  const loadBoardData = async () => {
    try {
      setInitialLoading(true);
      const data = await fetchBoardDetail(id, navigate, window.location);

      // data가 null이면 인증 오류가 발생하여 이미 리다이렉트된 상태
      if (data === null) {
        return;
      }

      setBoard({
        title: data.title,
        content: data.content,
      });
    } catch (error) {
      console.error(
        `게시글 상세 정보를 불러오는데 실패했습니다 (ID: ${id}):`,
        error
      );

      // 401 오류가 발생해도 바로 리다이렉트하지 않고, 에러 메시지만 표시
      if (error.response?.status === 401) {
        console.log(
          "인증 오류가 발생했지만, 로그아웃하지 않고 메시지만 표시합니다."
        );
        setError("인증이 만료되었습니다. 로그인 상태를 확인해주세요.");
      } else if (error.response?.status === 404) {
        setError("게시글을 찾을 수 없습니다.");
      } else {
        setError("게시글을 불러오는데 실패했습니다.");
      }
    } finally {
      setInitialLoading(false);
    }
  };

  // 입력 필드 변경 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;

    setBoard({
      ...board,
      [name]: value,
    });

    // 유효성 검증 상태 초기화
    if (validation[name]?.error) {
      setValidation({
        ...validation,
        [name]: { error: false, message: "" },
      });
    }
  };

  // 폼 검증
  const validateForm = () => {
    const newValidation = { ...validation };
    let isValid = true;

    if (!board.title.trim()) {
      newValidation.title = { error: true, message: "제목을 입력해주세요." };
      isValid = false;
    } else if (board.title.trim().length < 2) {
      newValidation.title = {
        error: true,
        message: "제목은 최소 2자 이상이어야 합니다.",
      };
      isValid = false;
    } else if (board.title.trim().length > 100) {
      newValidation.title = {
        error: true,
        message: "제목은 100자를 초과할 수 없습니다.",
      };
      isValid = false;
    }

    if (!board.content.trim()) {
      newValidation.content = { error: true, message: "내용을 입력해주세요." };
      isValid = false;
    }

    setValidation(newValidation);
    return isValid;
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setLoading(true);

      // 폼 제출 전 토큰 유효성 검증
      const token = getAccessToken();
      if (!token) {
        setSnackbar({
          open: true,
          message: "인증 토큰이 없습니다. 다시 로그인해주세요.",
          severity: "error",
        });
        setTimeout(() => {
          navigate("/login", {
            state: isEdit ? `/boards/${id}/edit` : "/boards/new",
          });
        }, 1500);
        return;
      }

      // 토큰 유효성 검증 (만료 여부 확인)
      try {
        const tokenData = decodeToken(token);
        if (tokenData) {
          const now = new Date().getTime();
          const expTime = tokenData.exp * 1000;

          // 토큰이 만료되었거나 10초 이내에 만료될 예정인 경우 갱신 시도
          if (expTime - now < 10000) {
            console.log(
              "⚠️ 토큰이 곧 만료되거나 이미 만료되었습니다. 갱신 시도 중..."
            );
            const refreshed = await import("../services/fetchReissue").then(
              (module) => module.default()
            );
            if (!refreshed) {
              setSnackbar({
                open: true,
                message: "인증이 만료되었습니다. 다시 로그인해주세요.",
                severity: "error",
              });
              setTimeout(() => {
                navigate("/login", {
                  state: isEdit ? `/boards/${id}/edit` : "/boards/new",
                });
              }, 1500);
              return;
            }
            console.log("✅ 토큰 갱신 성공, 폼 제출 계속");
          }
        }
      } catch (tokenError) {
        console.error("❌ 토큰 검증 중 오류 발생:", tokenError);
        // 오류 발생해도 계속 진행 (axiosInstance에서 처리)
      }

      if (isEdit) {
        // 게시글 수정
        await updateBoard(id, board, navigate, window.location);

        setSnackbar({
          open: true,
          message: "게시글이 수정되었습니다.",
          severity: "success",
        });

        // 수정된 게시글 페이지로 이동
        setTimeout(() => {
          navigate(`/boards/${id}`);
        }, 1500);
      } else {
        // 게시글 생성
        const response = await createBoard(board, navigate, window.location);

        // 응답이 null이면 인증 오류가 발생하여 이미 리다이렉트된 상태
        if (response === null) {
          return;
        }

        setSnackbar({
          open: true,
          message: "게시글이 작성되었습니다.",
          severity: "success",
        });

        // 작성된 게시글 페이지로 이동
        setTimeout(() => {
          navigate(`/boards/${response.id}`);
        }, 1500);
      }
    } catch (error) {
      console.error(
        isEdit ? "게시글 수정 중 오류 발생:" : "게시글 작성 중 오류 발생:",
        error
      );

      // 에러 메시지 표시
      let errorMessage = isEdit
        ? "게시글 수정에 실패했습니다."
        : "게시글 작성에 실패했습니다.";

      // 401 오류가 발생해도 바로 리다이렉트하지 않고, 메시지만 표시
      if (error.response?.status === 401) {
        errorMessage = "인증이 만료되었습니다. 로그인 상태를 확인해주세요.";
        console.log(
          "인증 오류가 발생했지만, 로그아웃하지 않고 메시지만 표시합니다."
        );
        // 여기서 리다이렉트하지 않음
      }

      setSnackbar({
        open: true,
        message: errorMessage,
        severity: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  // 취소 확인 대화상자 표시
  const handleCancelClick = () => {
    // 입력된 내용이 있으면 확인 대화상자 표시
    if (board.title.trim() || board.content.trim()) {
      setConfirmDialog({
        open: true,
        action: () => navigate(isEdit ? `/boards/${id}` : "/boards"),
      });
    } else {
      // 입력된 내용이 없으면 바로 이동
      navigate(isEdit ? `/boards/${id}` : "/boards");
    }
  };

  // 대화상자 확인 버튼 핸들러
  const handleDialogConfirm = () => {
    if (confirmDialog.action) {
      confirmDialog.action();
    }

    setConfirmDialog({
      open: false,
      action: null,
    });
  };

  // 대화상자 닫기
  const handleDialogClose = () => {
    setConfirmDialog({
      open: false,
      action: null,
    });
  };

  // 스낵바 닫기
  const handleSnackbarClose = () => {
    setSnackbar({
      ...snackbar,
      open: false,
    });
  };

  // 초기 로딩 중이면 로딩 인디케이터 표시
  if (initialLoading) {
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
        <Typography variant="h4" gutterBottom>
          {isEdit ? "게시글 수정" : "게시글 작성"}
        </Typography>

        <Divider sx={{ mb: 3 }} />

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <TextField
            name="title"
            label="제목"
            fullWidth
            margin="normal"
            value={board.title}
            onChange={handleChange}
            error={validation.title.error}
            helperText={validation.title.error ? validation.title.message : ""}
            disabled={loading}
            required
          />

          <TextField
            name="content"
            label="내용"
            fullWidth
            margin="normal"
            value={board.content}
            onChange={handleChange}
            error={validation.content.error}
            helperText={
              validation.content.error ? validation.content.message : ""
            }
            multiline
            rows={15}
            disabled={loading}
            required
          />

          <Box sx={{ display: "flex", justifyContent: "space-between", mt: 3 }}>
            <Button
              variant="outlined"
              color="secondary"
              onClick={handleCancelClick}
              startIcon={<CloseIcon />}
              disabled={loading}
            >
              취소
            </Button>

            <Button
              type="submit"
              variant="contained"
              color="primary"
              startIcon={<SaveIcon />}
              disabled={loading}
            >
              {loading ? (
                <CircularProgress size={24} />
              ) : isEdit ? (
                "수정하기"
              ) : (
                "등록하기"
              )}
            </Button>
          </Box>
        </Box>
      </Paper>

      {/* 취소 확인 대화상자 */}
      <Dialog open={confirmDialog.open} onClose={handleDialogClose}>
        <DialogTitle>작성 취소</DialogTitle>
        <DialogContent>
          <DialogContentText>
            작성 중인 내용이 있습니다. 정말 취소하시겠습니까?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDialogClose} color="primary">
            계속 작성
          </Button>
          <Button onClick={handleDialogConfirm} color="error" autoFocus>
            취소
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

export default BoardForm;
