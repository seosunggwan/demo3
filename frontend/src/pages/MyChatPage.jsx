import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import axiosInstance from "../utils/axios";
import {
  Container,
  Card,
  CardContent,
  CardHeader,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Alert,
  TextField,
  InputAdornment,
  IconButton,
  Box,
  Pagination,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";

// API 기본 URL 설정 (환경 변수가 없을 경우 폴백 URL 사용)
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

/**
 * 📌 내 채팅 목록 컴포넌트 (React 변환)
 * - Vue의 `data()` → React `useState()`로 변환
 * - Vue의 `created()` → React `useEffect()`로 변환
 * - Vuetify `v-table` → MUI `Table`로 변환
 */
const MyChatPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [chatList, setChatList] = useState([]);
  const [serverStatus, setServerStatus] = useState("offline");
  const [error, setError] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState(
    searchParams.get("keyword") || ""
  );
  const [loading, setLoading] = useState(false);
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false,
  });

  // URL에서 파라미터 가져오기
  const keyword = searchParams.get("keyword") || "";
  const page = parseInt(searchParams.get("page") || "0");
  const size = parseInt(searchParams.get("size") || "10");

  // 검색어 상태 초기화
  useEffect(() => {
    setSearchKeyword(keyword);
  }, [keyword]);

  useEffect(() => {
    checkServerStatus();
  }, []);

  useEffect(() => {
    if (serverStatus === "online") {
      loadChatList(keyword, page, size);
    }
  }, [serverStatus, keyword, page, size]);

  const checkServerStatus = async () => {
    try {
      // 서버 상태 확인은 기본 axios로 유지 (인증 필요 없음)
      const response = await axios.get(`${API_BASE_URL}/health`, {
        timeout: 5000,
      });
      if (response.status === 200) {
        setServerStatus("online");
        setError(null);
      } else {
        setServerStatus("offline");
        setError("서버가 응답했지만 상태가 정상이 아닙니다.");
      }
    } catch (error) {
      console.error("서버 상태 확인 실패:", error);
      setServerStatus("offline");
      setError("서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해 주세요.");
    }
  };

  const loadChatList = async (
    searchTerm = "",
    currentPage = 0,
    pageSize = 10
  ) => {
    try {
      setLoading(true);
      console.log(
        `채팅방 목록 로드 시작 - 검색어: '${searchTerm}', 페이지: ${currentPage}, 크기: ${pageSize}`
      );

      // 파라미터 구성
      const params = {
        page: currentPage,
        size: pageSize,
      };

      if (searchTerm && searchTerm.trim() !== "") {
        params.keyword = searchTerm.trim();
      }

      console.log("API 요청 파라미터:", params);

      // URL 쿼리 파라미터 확인을 위한 로그
      const queryString = new URLSearchParams(params).toString();
      console.log(
        "요청 URL:",
        `/chat/my/rooms${queryString ? "?" + queryString : ""}`
      );

      const response = await axiosInstance.get("/chat/my/rooms", { params });

      // 응답 상태 확인
      console.log("응답 상태 코드:", response.status);

      if (response.data && response.data.rooms) {
        setChatList(response.data.rooms);
        setPageInfo(response.data.pageInfo);
        console.log(
          `채팅방 목록 로드 완료: ${response.data.rooms.length}개 채팅방 (전체 ${response.data.pageInfo.total}개)`
        );
      } else {
        // 이전 버전 API 응답 처리 (하위 호환성)
        setChatList(Array.isArray(response.data) ? response.data : []);
        console.log(
          `채팅방 목록 로드 완료 (이전 버전 API): ${
            Array.isArray(response.data) ? response.data.length : 0
          }개 채팅방`
        );
      }
    } catch (error) {
      console.error("채팅 목록 불러오기 실패:", error);
      setError("채팅 목록을 불러오는 데 실패했습니다.");

      // 인증 오류 처리
      if (error.response?.status === 401) {
        navigate("/login", { state: "/mychatpage" });
      }
    } finally {
      setLoading(false);
    }
  };

  const enterChatRoom = (roomId) => {
    navigate(`/chatpage/${roomId}`);
  };

  const leaveChatRoom = async (roomId) => {
    try {
      // axiosInstance 사용 (인터셉터에서 토큰 관리)
      await axiosInstance.delete(`/chat/room/group/${roomId}/leave`);
      setChatList(chatList.filter((chat) => chat.roomId !== roomId));
    } catch (error) {
      console.error("채팅방 나가기 실패:", error);
      setError("채팅방 나가기에 실패했습니다.");

      // 인증 오류 처리
      if (error.response?.status === 401) {
        navigate("/login", { state: "/mychatpage" });
      }
    }
  };

  // 검색 처리
  const handleSearch = (e) => {
    e.preventDefault();
    console.log("검색 요청:", searchKeyword);

    const newParams = { page: 0, size };

    // 검색어가 있는 경우만 파라미터에 추가
    if (searchKeyword && searchKeyword.trim() !== "") {
      // 검색어를 URL 인코딩하여 파라미터 설정
      const encodedKeyword = encodeURIComponent(searchKeyword.trim());
      console.log("인코딩된 검색어:", encodedKeyword);
      newParams.keyword = searchKeyword.trim();
    }

    setSearchParams(newParams);
  };

  // 페이지 변경 처리
  const handlePageChange = (event, newPage) => {
    console.log("페이지 변경:", newPage);

    // URL 파라미터 업데이트 (0부터 시작하는 페이지 번호로 변환)
    const newParams = {
      page: newPage - 1,
      size,
    };

    // 검색어가 있으면 유지
    if (keyword) {
      newParams.keyword = keyword;
    }

    setSearchParams(newParams);
  };

  // 검색어 초기화
  const handleClearSearch = () => {
    setSearchKeyword("");
    setSearchParams({ page: 0, size });
  };

  const retryConnection = () => {
    setError(null);
    checkServerStatus();
  };

  return (
    <Container>
      <Card>
        <CardHeader title="내 채팅 목록" />
        <CardContent>
          {serverStatus === "offline" && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error || "서버에 연결할 수 없습니다."}
              <Button
                color="inherit"
                size="small"
                onClick={retryConnection}
                sx={{ ml: 2 }}
              >
                재연결 시도
              </Button>
            </Alert>
          )}

          {error && serverStatus === "online" && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {/* 검색 폼 추가 */}
          <Box sx={{ mb: 3 }}>
            <form onSubmit={handleSearch}>
              <TextField
                fullWidth
                placeholder="채팅방 이름으로 검색"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                  endAdornment: searchKeyword && (
                    <InputAdornment position="end">
                      <IconButton onClick={handleClearSearch} edge="end">
                        <ClearIcon />
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
                variant="outlined"
                size="small"
              />
            </form>
          </Box>

          {keyword && (
            <Box sx={{ mb: 2 }}>
              <Alert severity="info">
                "{keyword}" 검색 결과: {pageInfo.total}개의 채팅방
              </Alert>
            </Box>
          )}

          {loading ? (
            <Box sx={{ textAlign: "center", py: 4 }}>
              <div>로딩 중...</div>
            </Box>
          ) : (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>채팅방 이름</TableCell>
                    <TableCell>읽지 않은 메시지</TableCell>
                    <TableCell>액션</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {chatList.length > 0 ? (
                    chatList.map((chat) => (
                      <TableRow key={chat.roomId}>
                        <TableCell>{chat.roomName}</TableCell>
                        <TableCell>{chat.unReadCount}</TableCell>
                        <TableCell>
                          <Button
                            variant="contained"
                            color="primary"
                            onClick={() => enterChatRoom(chat.roomId)}
                          >
                            입장
                          </Button>
                          <Button
                            variant="contained"
                            color="secondary"
                            disabled={chat.isGroupChat === "N"}
                            onClick={() => leaveChatRoom(chat.roomId)}
                            style={{ marginLeft: "10px" }}
                          >
                            나가기
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={3} align="center">
                        {keyword
                          ? `"${keyword}" 검색 결과가 없습니다.`
                          : "참여 중인 채팅방이 없습니다."}
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {/* 페이지네이션 컴포넌트 추가 */}
          {chatList.length > 0 && pageInfo.totalPages > 0 && (
            <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
              <Pagination
                count={pageInfo.totalPages}
                page={pageInfo.page + 1}
                onChange={handlePageChange}
                color="primary"
                showFirstButton
                showLastButton
              />
            </Box>
          )}
        </CardContent>
      </Card>
    </Container>
  );
};

export default MyChatPage;
