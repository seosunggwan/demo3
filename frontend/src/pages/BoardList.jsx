import { useState, useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import {
  fetchBoardsWithPaging,
  searchBoardsByKeyword,
  searchBoardsByAuthor,
  deleteBoard,
} from "../services/boardService";
import { decodeToken, redirectToLogin } from "../utils/auth";
import {
  Container,
  Typography,
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  TextField,
  MenuItem,
  Select,
  FormControl,
  InputLabel,
  Pagination,
  CircularProgress,
  Snackbar,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Grid,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import SearchIcon from "@mui/icons-material/Search";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { formatDate, formatDateKorean } from "../utils/dateUtils";
import PopularBoardList from "../components/PopularBoardList";

const BoardList = () => {
  const [boards, setBoards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const [searchType, setSearchType] = useState("keyword");
  const [searchQuery, setSearchQuery] = useState("");
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    boardId: null,
  });

  const navigate = useNavigate();
  const { isLoggedIn, getAccessToken } = useLogin();

  // URL에서 페이지 파라미터 가져오기
  const page = parseInt(searchParams.get("page") || "0");
  const size = parseInt(searchParams.get("size") || "10");
  const keyword = searchParams.get("keyword") || "";
  const type = searchParams.get("type") || "keyword";

  // 컴포넌트 마운트 시 초기 데이터 로드
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const token = getAccessToken();
        if (!token || !isLoggedIn) {
          console.log("인증되지 않은 상태입니다.");
          setIsAuthenticated(false);
          redirectToLogin(navigate, "/boards");
          return;
        }

        // 토큰 유효성 검증
        const tokenData = decodeToken(token);
        if (!tokenData) {
          console.log("토큰이 유효하지 않습니다.");
          setIsAuthenticated(false);
          redirectToLogin(navigate, "/boards");
          return;
        }

        setIsAuthenticated(true);

        // URL에 검색 쿼리가 있으면 검색 실행, 없으면 게시글 목록 조회
        if (keyword) {
          setSearchType(type);
          setSearchQuery(keyword);
          await handleSearch(type, keyword, page, size);
        } else {
          await fetchBoards(page, size);
        }
      } catch (error) {
        console.error("초기 데이터 로드 중 오류 발생:", error);
        setError("게시글 목록을 불러오는데 실패했습니다.");
        setLoading(false);
      }
    };

    loadInitialData();
  }, [isLoggedIn, navigate, getAccessToken, page, size, keyword, type]);

  // 게시글 목록 조회
  const fetchBoards = async (page, size) => {
    try {
      setLoading(true);
      const data = await fetchBoardsWithPaging(page, size);
      setBoards(data.boards);
      setPageInfo(data.pageInfo);
    } catch (error) {
      console.error("게시글 목록을 불러오는데 실패했습니다:", error);

      if (error.response?.status === 401) {
        setIsAuthenticated(false);
        redirectToLogin(navigate, "/boards");
      } else {
        setError("게시글 목록을 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  // 검색 실행
  const handleSearch = async (type, query, page, size) => {
    try {
      setLoading(true);
      let data;

      if (type === "author") {
        data = await searchBoardsByAuthor(query, page, size);
      } else {
        data = await searchBoardsByKeyword(query, page, size);
      }

      setBoards(data.boards);
      setPageInfo(data.pageInfo);
    } catch (error) {
      console.error("게시글 검색 중 오류 발생:", error);

      if (error.response?.status === 401) {
        setIsAuthenticated(false);
        redirectToLogin(navigate, "/boards");
      } else {
        setError("게시글 검색 중 오류가 발생했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  // 검색 버튼 클릭 시 처리
  const onSearchSubmit = (e) => {
    e.preventDefault();

    const newParams = {
      page: 0,
      size,
    };

    if (searchQuery.trim()) {
      newParams.keyword = searchQuery.trim();
      newParams.type = searchType;
    }

    setSearchParams(newParams);
  };

  // 페이지 변경 시 처리
  const handlePageChange = (event, newPage) => {
    const newParams = {
      ...Object.fromEntries(searchParams),
      page: newPage - 1,
    };
    setSearchParams(newParams);
  };

  // 게시글 삭제 확인 대화상자 표시
  const handleDeleteClick = (boardId) => {
    setConfirmDialog({
      open: true,
      boardId,
    });
  };

  // 게시글 삭제 처리
  const handleDeleteConfirm = async () => {
    try {
      await deleteBoard(confirmDialog.boardId);

      // 성공 메시지 표시
      setSnackbar({
        open: true,
        message: "게시글이 삭제되었습니다.",
        severity: "success",
      });

      // 게시글 목록 다시 로드
      if (keyword) {
        await handleSearch(type, keyword, page, size);
      } else {
        await fetchBoards(page, size);
      }
    } catch (error) {
      console.error("게시글 삭제 중 오류 발생:", error);

      // 에러 메시지 표시
      setSnackbar({
        open: true,
        message: "게시글 삭제에 실패했습니다.",
        severity: "error",
      });
    } finally {
      // 대화상자 닫기
      setConfirmDialog({
        open: false,
        boardId: null,
      });
    }
  };

  // 대화상자 닫기
  const handleDialogClose = () => {
    setConfirmDialog({
      open: false,
      boardId: null,
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
            onClick={() => fetchBoards(0, 10)}
            sx={{ mt: 2 }}
          >
            다시 시도
          </Button>
        </Paper>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          게시판
        </Typography>

        {/* 인기글 목록 컴포넌트 추가 */}
        <PopularBoardList />

        {/* 검색 필터 영역 */}
        <Paper sx={{ p: 2, mb: 2 }}>
          <Paper
            component="form"
            onSubmit={onSearchSubmit}
            sx={{ p: 2, mb: 3 }}
          >
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={2}>
                <FormControl fullWidth size="small">
                  <InputLabel id="search-type-label">검색 유형</InputLabel>
                  <Select
                    labelId="search-type-label"
                    value={searchType}
                    label="검색 유형"
                    onChange={(e) => setSearchType(e.target.value)}
                  >
                    <MenuItem value="keyword">제목+내용</MenuItem>
                    <MenuItem value="author">작성자</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={8}>
                <TextField
                  fullWidth
                  size="small"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder={
                    searchType === "keyword"
                      ? "제목 또는 내용으로 검색"
                      : "작성자 이름으로 검색"
                  }
                  InputProps={{
                    endAdornment: (
                      <IconButton type="submit" sx={{ p: 0.5 }}>
                        <SearchIcon />
                      </IconButton>
                    ),
                  }}
                />
              </Grid>
              <Grid item xs={12} sm={2}>
                <Button
                  fullWidth
                  variant="contained"
                  type="submit"
                  startIcon={<SearchIcon />}
                >
                  검색
                </Button>
              </Grid>
            </Grid>
          </Paper>
        </Paper>

        {/* 글쓰기 버튼 */}
        <Box sx={{ display: "flex", justifyContent: "flex-end", mb: 2 }}>
          <Button
            component={Link}
            to="/boards/new"
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
          >
            글쓰기
          </Button>
        </Box>

        {/* 게시글 목록 */}
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }}>
            <TableHead>
              <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                <TableCell width="10%" align="center">
                  번호
                </TableCell>
                <TableCell width="45%">제목</TableCell>
                <TableCell width="15%" align="center">
                  작성자
                </TableCell>
                <TableCell width="15%" align="center">
                  작성일
                </TableCell>
                <TableCell width="7%" align="center">
                  조회수
                </TableCell>
                <TableCell width="8%" align="center">
                  관리
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {boards.length > 0 ? (
                boards.map((board) => (
                  <TableRow key={board.id} hover>
                    <TableCell align="center">{board.id}</TableCell>
                    <TableCell>
                      <Link
                        to={`/boards/${board.id}`}
                        style={{ textDecoration: "none", color: "inherit" }}
                      >
                        <Typography>{board.title}</Typography>
                      </Link>
                    </TableCell>
                    <TableCell align="center">{board.authorName}</TableCell>
                    <TableCell align="center">
                      {board.createdTime
                        ? new Date(
                            board.createdTime[0],
                            board.createdTime[1] - 1, // 월은 0부터 시작하므로 1을 빼줍니다
                            board.createdTime[2],
                            board.createdTime[3],
                            board.createdTime[4]
                          ).toLocaleDateString("ko-KR", {
                            year: "numeric",
                            month: "2-digit",
                            day: "2-digit",
                            hour: "2-digit",
                            minute: "2-digit",
                          })
                        : "날짜 없음"}
                    </TableCell>
                    <TableCell align="center">{board.viewCount}</TableCell>
                    <TableCell align="center">
                      <IconButton
                        component={Link}
                        to={`/boards/${board.id}`}
                        color="primary"
                        size="small"
                      >
                        <VisibilityIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        component={Link}
                        to={`/boards/${board.id}/edit`}
                        color="primary"
                        size="small"
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        color="error"
                        size="small"
                        onClick={() => handleDeleteClick(board.id)}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                    <Typography variant="body1">
                      {keyword
                        ? "검색 결과가 없습니다."
                        : "등록된 게시글이 없습니다."}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* 페이지네이션 */}
        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
          <Pagination
            count={pageInfo.totalPages}
            page={pageInfo.page + 1}
            onChange={handlePageChange}
            color="primary"
            showFirstButton
            showLastButton
            disabled={pageInfo.total === 0}
          />
        </Box>
      </Box>

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
          <Button onClick={handleDialogClose} color="primary">
            취소
          </Button>
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

export default BoardList;
