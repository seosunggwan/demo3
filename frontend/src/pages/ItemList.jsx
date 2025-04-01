import React, { useState, useEffect, Suspense } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import axiosInstance from "../utils/axios";
import { decodeToken, redirectToLogin } from "../utils/auth";
import {
  Container,
  Typography,
  Box,
  TextField,
  Button,
  InputAdornment,
  Paper,
  IconButton,
  Fade,
  Grid,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Divider,
  CircularProgress,
  Pagination as MuiPagination,
  Alert,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import NoPhotographyIcon from "@mui/icons-material/NoPhotography";

const ItemList = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchKeyword, setSearchKeyword] = useState(
    searchParams.get("keyword") || ""
  );
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 8,
    total: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  const navigate = useNavigate();
  const { isLoggedIn, getAccessToken } = useLogin();

  // URL에서 파라미터 가져오기
  const page = parseInt(searchParams.get("page") || "0");
  const size = parseInt(searchParams.get("size") || "8");
  const keyword = searchParams.get("keyword") || "";

  // 검색어 상태 초기화
  useEffect(() => {
    setSearchKeyword(keyword);
  }, [keyword]);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const token = getAccessToken();
        if (!token || !isLoggedIn) {
          console.log("인증되지 않은 상태입니다.");
          setIsAuthenticated(false);
          redirectToLogin(navigate, "/items");
          return;
        }

        // 토큰 유효성 검증
        const tokenData = decodeToken(token);
        if (!tokenData) {
          console.log("토큰이 유효하지 않습니다.");
          setIsAuthenticated(false);
          redirectToLogin(navigate, "/items");
          return;
        }

        setIsAuthenticated(true);

        if (keyword) {
          // 검색어가 있으면 검색 API 호출
          await searchItemsWithPaging(keyword, page, size);
        } else {
          // 검색어가 없으면 일반 목록 조회
          await fetchItemsWithPaging(page, size);
        }
      } catch (error) {
        console.error("인증 확인 중 오류 발생:", error);
        setIsAuthenticated(false);
        redirectToLogin(navigate, "/items");
      }
    };

    checkAuth();
  }, [isLoggedIn, navigate, getAccessToken, page, size, keyword]);

  // 페이지네이션 적용 상품 목록 조회
  const fetchItemsWithPaging = async (page, size) => {
    try {
      setLoading(true);
      const response = await axiosInstance.get(
        `/api/items/page?page=${page}&size=${size}`
      );

      if (response.data && response.data.items) {
        setItems(response.data.items);
        setPageInfo(response.data.pageInfo);
      } else {
        setItems([]);
        console.error("API 응답 형식이 올바르지 않습니다:", response.data);
      }
    } catch (error) {
      console.error("상품 목록을 불러오는데 실패했습니다:", error);

      if (error.response?.status === 401) {
        setIsAuthenticated(false);
        redirectToLogin(navigate, "/items");
      } else {
        setError("상품 목록을 불러오는데 실패했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  // 검색 API를 사용한 상품 목록 조회
  const searchItemsWithPaging = async (keyword, page, size) => {
    try {
      setLoading(true);
      console.log(
        `상품 검색 시작: 키워드='${keyword}', 페이지=${page}, 사이즈=${size}`
      );

      const response = await axiosInstance.get(
        `/api/items/search?keyword=${encodeURIComponent(
          keyword
        )}&page=${page}&size=${size}`
      );

      if (response.data && response.data.items) {
        setItems(response.data.items);
        setPageInfo(response.data.pageInfo);
        console.log(
          `검색 결과: ${response.data.items.length}개 상품 (총 ${response.data.pageInfo.total}개)`
        );
      } else {
        setItems([]);
        console.error("API 응답 형식이 올바르지 않습니다:", response.data);
      }
    } catch (error) {
      console.error("상품 검색 중 오류 발생:", error);

      if (error.response?.status === 401) {
        setIsAuthenticated(false);
        redirectToLogin(navigate, "/items");
      } else {
        setError("상품 검색 중 오류가 발생했습니다.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    // URL 파라미터 업데이트
    const newParams = {
      page: newPage,
      size,
    };

    // 검색어가 있으면 추가
    if (keyword) {
      newParams.keyword = keyword;
    }

    setSearchParams(newParams);
  };

  // 검색 처리
  const handleSearch = (e) => {
    e.preventDefault();
    console.log("검색 요청:", searchKeyword);

    const newParams = {
      page: 0,
      size,
    };

    // 검색어가 있는 경우만 파라미터에 추가
    if (searchKeyword && searchKeyword.trim() !== "") {
      newParams.keyword = searchKeyword.trim();
    }

    setSearchParams(newParams);
  };

  // 검색어 초기화
  const handleClearSearch = () => {
    setSearchKeyword("");
    setSearchParams({ page: 0, size });
  };

  const handleDelete = async (id) => {
    if (!isAuthenticated) {
      redirectToLogin(navigate, "/items");
      return;
    }

    if (window.confirm("정말 삭제하시겠습니까?")) {
      try {
        console.log(`상품 ID ${id} 삭제 요청 시작`);

        // axios 인스턴스 사용 (인터셉터에서 토큰 만료 처리)
        const response = await axiosInstance.delete(`/api/items/${id}`);

        if (response.status === 200) {
          console.log(`상품 ID ${id} 삭제 성공`);
          setItems(items.filter((item) => item.id !== id));
        }
      } catch (error) {
        console.error("상품 삭제에 실패했습니다:", error);
        console.error("에러 상세:", {
          status: error.response?.status,
          data: error.response?.data,
          message: error.message,
        });

        if (error.response?.status === 401) {
          setIsAuthenticated(false);
          redirectToLogin(navigate, "/items");
        } else if (error.response?.status === 403) {
          alert("상품을 삭제할 권한이 없습니다.");
        } else if (error.response?.status === 404) {
          alert("상품을 찾을 수 없습니다.");
          setItems(items.filter((item) => item.id !== id));
        } else if (
          error.response?.data?.message?.includes("이미 주문에 사용된 상품")
        ) {
          alert("이미 주문에 사용된 상품은 삭제할 수 없습니다.");
        } else {
          alert(
            "상품 삭제에 실패했습니다: " +
              (error.response?.data?.message || error.message)
          );
        }
      }
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading) {
    return (
      <Container sx={{ py: 8, textAlign: "center" }}>
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ mt: 3 }}>
          상품 목록을 불러오는 중...
        </Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container sx={{ py: 8 }}>
        <Paper
          elevation={2}
          sx={{
            p: 4,
            textAlign: "center",
            borderRadius: 2,
            backgroundColor: "error.lighter",
          }}
        >
          <Typography variant="h6" color="error" gutterBottom>
            {error}
          </Typography>
          <Button
            variant="contained"
            color="primary"
            onClick={() => window.location.reload()}
            sx={{ mt: 2 }}
          >
            다시 시도
          </Button>
        </Paper>
      </Container>
    );
  }

  return (
    <Suspense
      fallback={
        <Container sx={{ py: 8, textAlign: "center" }}>
          <CircularProgress size={60} />
        </Container>
      }
    >
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            mb: 4,
          }}
        >
          <Typography variant="h4" component="h1" fontWeight="bold">
            상품 목록
          </Typography>
          <Button
            component={Link}
            to="/items/new"
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            sx={{ borderRadius: 1.5, textTransform: "none" }}
          >
            상품 등록
          </Button>
        </Box>

        {/* 검색 폼 추가 */}
        <Paper
          elevation={1}
          sx={{
            p: 2,
            mb: 4,
            borderRadius: 2,
            transition: "all 0.3s ease",
            "&:hover": {
              boxShadow: 3,
            },
          }}
        >
          <form onSubmit={handleSearch}>
            <TextField
              fullWidth
              variant="outlined"
              size="medium"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder="상품명을 입력하세요"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon color="action" />
                  </InputAdornment>
                ),
                endAdornment: searchKeyword && (
                  <InputAdornment position="end">
                    <Fade in={Boolean(searchKeyword)}>
                      <IconButton
                        aria-label="clear search"
                        onClick={() => setSearchKeyword("")}
                        edge="end"
                        size="small"
                      >
                        <ClearIcon fontSize="small" />
                      </IconButton>
                    </Fade>
                  </InputAdornment>
                ),
              }}
              sx={{
                "& .MuiOutlinedInput-root": {
                  borderRadius: 2,
                  transition: "all 0.3s ease",
                },
                "& .MuiOutlinedInput-root.Mui-focused": {
                  borderColor: "primary.main",
                },
              }}
            />
            <Box
              sx={{
                display: "flex",
                gap: 1,
                mt: 1.5,
                justifyContent: "flex-end",
              }}
            >
              <Button
                type="submit"
                variant="contained"
                color="primary"
                startIcon={<SearchIcon />}
                sx={{
                  minWidth: "100px",
                  borderRadius: 1.5,
                  textTransform: "none",
                }}
              >
                검색
              </Button>
              {keyword && (
                <Button
                  onClick={handleClearSearch}
                  variant="outlined"
                  color="secondary"
                  sx={{
                    minWidth: "100px",
                    borderRadius: 1.5,
                    textTransform: "none",
                  }}
                >
                  검색 초기화
                </Button>
              )}
            </Box>
          </form>
        </Paper>

        {keyword && (
          <Box sx={{ mb: 3 }}>
            <Alert
              severity="info"
              sx={{ borderRadius: 2 }}
              action={
                <Button
                  color="inherit"
                  size="small"
                  onClick={handleClearSearch}
                >
                  모든 상품 보기
                </Button>
              }
            >
              <Typography variant="body2">
                '{keyword}' 검색 결과: 총 {pageInfo.total}개의 상품
              </Typography>
            </Alert>
          </Box>
        )}

        {/* 상품 목록 */}
        <Grid container spacing={3}>
          {Array.isArray(items) && items.length > 0 ? (
            items.map((item) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={item.id}>
                <Card
                  sx={{
                    height: "100%",
                    display: "flex",
                    flexDirection: "column",
                    transition: "transform 0.2s, box-shadow 0.2s",
                    "&:hover": {
                      transform: "translateY(-4px)",
                      boxShadow: 4,
                    },
                    borderRadius: 2,
                    overflow: "hidden",
                  }}
                >
                  <CardMedia
                    component="div"
                    sx={{
                      pt: "75%", // 4:3 비율
                      position: "relative",
                      backgroundColor: "grey.100",
                    }}
                  >
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.name}
                        style={{
                          position: "absolute",
                          top: 0,
                          left: 0,
                          width: "100%",
                          height: "100%",
                          objectFit: "cover",
                        }}
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src =
                            "https://via.placeholder.com/150?text=No+Image";
                        }}
                      />
                    ) : (
                      <Box
                        sx={{
                          position: "absolute",
                          top: 0,
                          left: 0,
                          width: "100%",
                          height: "100%",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          color: "text.secondary",
                        }}
                      >
                        <NoPhotographyIcon sx={{ fontSize: 40 }} />
                      </Box>
                    )}
                  </CardMedia>

                  <CardContent sx={{ flexGrow: 1 }}>
                    <Typography
                      variant="h6"
                      component="h2"
                      noWrap
                      gutterBottom
                      fontWeight="bold"
                    >
                      {item.name}
                    </Typography>
                    <Typography
                      variant="body2"
                      color="text.primary"
                      gutterBottom
                    >
                      가격: {item.price.toLocaleString()}원
                    </Typography>
                    <Typography
                      variant="body2"
                      color={item.stockQuantity > 0 ? "text.primary" : "error"}
                    >
                      재고: {item.stockQuantity}개
                      {item.stockQuantity === 0 && " (품절)"}
                    </Typography>
                  </CardContent>

                  <Divider />

                  <CardActions sx={{ p: 1.5, justifyContent: "space-between" }}>
                    <Button
                      component={Link}
                      to={`/items/${item.id}/edit`}
                      size="small"
                      startIcon={<EditIcon />}
                      sx={{ textTransform: "none" }}
                    >
                      수정
                    </Button>
                    <Button
                      size="small"
                      color="error"
                      startIcon={<DeleteIcon />}
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        handleDelete(item.id);
                      }}
                      sx={{ textTransform: "none" }}
                    >
                      삭제
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))
          ) : (
            <Grid item xs={12}>
              <Paper
                sx={{
                  p: 4,
                  textAlign: "center",
                  borderRadius: 2,
                  backgroundColor: "grey.50",
                }}
              >
                <Typography variant="body1" color="text.secondary" gutterBottom>
                  등록된 상품이 없습니다.
                </Typography>
                <Button
                  component={Link}
                  to="/items/new"
                  variant="contained"
                  color="primary"
                  startIcon={<AddIcon />}
                  sx={{ mt: 2, borderRadius: 1.5, textTransform: "none" }}
                >
                  상품 등록하기
                </Button>
              </Paper>
            </Grid>
          )}
        </Grid>

        {/* 페이지네이션 */}
        {pageInfo.totalPages > 1 && (
          <Box sx={{ display: "flex", justifyContent: "center", mt: 4, mb: 2 }}>
            <MuiPagination
              count={pageInfo.totalPages}
              page={pageInfo.page + 1}
              onChange={(event, page) => handlePageChange(page - 1)}
              color="primary"
              showFirstButton
              showLastButton
              siblingCount={1}
            />
          </Box>
        )}
      </Container>
    </Suspense>
  );
};

export default ItemList;
