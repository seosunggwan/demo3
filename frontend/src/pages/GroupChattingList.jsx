import { useEffect, useState } from "react";
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
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
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  CircularProgress,
  Pagination,
  Box,
  InputAdornment,
  IconButton,
  Grid,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";
import fetchGroupChatRooms from "../services/fetchGroupChatRooms";
import createGroupChatRoom from "../services/createGroupChatRoom";
import joinGroupChatRoom from "../services/joinGroupChatRoom";

const GroupChattingList = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [chatRoomList, setChatRoomList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateRoomModal, setShowCreateRoomModal] = useState(false);
  const [newRoomTitle, setNewRoomTitle] = useState("");
  const [searchKeyword, setSearchKeyword] = useState(
    searchParams.get("keyword") || ""
  );
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 10,
    total: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  // URL에서 페이지 파라미터 가져오기
  const page = parseInt(searchParams.get("page") || "0");
  const size = parseInt(searchParams.get("size") || "10");
  const keyword = searchParams.get("keyword") || "";

  // 검색어 상태 업데이트
  useEffect(() => {
    setSearchKeyword(keyword);
  }, [keyword]);

  // ✅ 채팅방 목록 불러오기
  useEffect(() => {
    console.log(
      "채팅방 목록 로드: page =",
      page,
      "size =",
      size,
      "keyword =",
      keyword
    );
    loadChatRoom(page, size, keyword);
  }, [navigate, location, page, size, keyword]);

  const loadChatRoom = async (page, size, keyword) => {
    try {
      setLoading(true);
      // 새로 만든 fetchGroupChatRooms 서비스 사용 (페이지네이션 파라미터 추가)
      const data = await fetchGroupChatRooms(
        page,
        size,
        navigate,
        location,
        keyword
      );

      if (data && data.rooms && Array.isArray(data.rooms)) {
        setChatRoomList(data.rooms);
        setPageInfo(data.pageInfo);
        console.log("채팅방 목록 로드 성공:", data);
      } else {
        console.error("채팅방 목록 데이터가 잘못되었습니다.");
        setChatRoomList([]);
      }
    } catch (error) {
      console.error("채팅방 목록을 불러오는 중 오류 발생:", error);
      setChatRoomList([]);
    } finally {
      setLoading(false);
    }
  };

  // 페이지 변경 시 처리
  const handlePageChange = (event, newPage) => {
    const newParams = {
      page: newPage - 1,
      size,
      ...(keyword && { keyword }),
    };
    setSearchParams(newParams);
  };

  // 검색 처리
  const handleSearch = (e) => {
    e.preventDefault();
    console.log("검색 폼 제출: 검색어 =", searchKeyword);

    // 검색어가 없을 경우에도 명시적으로 빈 문자열 처리
    const newParams = {
      page: 0,
      size,
    };

    // 검색어가 있을 경우에만 keyword 파라미터 추가
    if (searchKeyword && searchKeyword.trim() !== "") {
      newParams.keyword = searchKeyword.trim();
    }

    console.log("설정할 URL 파라미터:", newParams);
    setSearchParams(newParams);
  };

  // 검색어 초기화
  const handleClearSearch = () => {
    console.log("검색어 초기화");
    setSearchKeyword("");
    const newParams = {
      page: 0,
      size,
    };
    setSearchParams(newParams);
  };

  // ✅ 채팅방 참여하기
  const handleJoinChatRoom = async (roomId) => {
    try {
      // 새로 만든 joinGroupChatRoom 서비스 사용
      const success = await joinGroupChatRoom(roomId, navigate, location);

      if (success) {
        navigate(`/chatpage/${roomId}`);
      } else {
        alert("채팅방 참여에 실패했습니다.");
      }
    } catch (error) {
      console.error("채팅방 참여 중 오류 발생:", error);
    }
  };

  // ✅ 채팅방 생성하기
  const handleCreateChatRoom = async () => {
    if (!newRoomTitle.trim()) {
      alert("채팅방 이름을 입력해주세요.");
      return;
    }

    try {
      // 새로 만든 createGroupChatRoom 서비스 사용
      await createGroupChatRoom(newRoomTitle, navigate, location);

      // 모달 닫고 목록 새로고침
      setNewRoomTitle("");
      setShowCreateRoomModal(false);
      loadChatRoom(page, size, keyword);
    } catch (error) {
      console.error("채팅방 생성 중 오류 발생:", error);
    }
  };

  return (
    <Container>
      <Card>
        <CardHeader title="채팅방 목록" />
        <CardContent>
          <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
            <Grid item xs={12} sm={6} md={8}>
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
            </Grid>
            <Grid
              item
              xs={12}
              sm={6}
              md={4}
              sx={{ textAlign: { xs: "left", sm: "right" } }}
            >
              <Button
                variant="contained"
                color="secondary"
                onClick={() => setShowCreateRoomModal(true)}
              >
                채팅방 생성
              </Button>
            </Grid>
          </Grid>

          <TableContainer>
            {loading ? (
              <div
                style={{
                  display: "flex",
                  justifyContent: "center",
                  padding: "20px",
                }}
              >
                <CircularProgress />
              </div>
            ) : (
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>방번호</TableCell>
                    <TableCell>방제목</TableCell>
                    <TableCell>채팅</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {chatRoomList.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={3} align="center">
                        {keyword
                          ? `'${keyword}' 검색 결과가 없습니다.`
                          : "채팅방이 없습니다."}
                      </TableCell>
                    </TableRow>
                  ) : (
                    chatRoomList.map((chat) => (
                      <TableRow key={chat.roomId}>
                        <TableCell>{chat.roomId}</TableCell>
                        <TableCell>{chat.roomName}</TableCell>
                        <TableCell>
                          <Button
                            variant="contained"
                            color="primary"
                            onClick={() => handleJoinChatRoom(chat.roomId)}
                          >
                            참여하기
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            )}
          </TableContainer>

          {/* 페이지네이션 추가 */}
          {!loading && chatRoomList.length > 0 && (
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

      {/* 채팅방 생성 모달 */}
      <Dialog
        open={showCreateRoomModal}
        onClose={() => setShowCreateRoomModal(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>채팅방 생성</DialogTitle>
        <DialogContent>
          <TextField
            label="방제목"
            fullWidth
            value={newRoomTitle}
            onChange={(e) => setNewRoomTitle(e.target.value)}
            margin="dense"
            autoFocus
          />
        </DialogContent>
        <DialogActions>
          <Button color="inherit" onClick={() => setShowCreateRoomModal(false)}>
            취소
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleCreateChatRoom}
          >
            생성
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default GroupChattingList;
