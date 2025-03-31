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
} from "@mui/material";
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

  // ✅ 채팅방 목록 불러오기
  useEffect(() => {
    loadChatRoom(page, size);
  }, [navigate, location, page, size]);

  const loadChatRoom = async (page, size) => {
    try {
      setLoading(true);
      // 새로 만든 fetchGroupChatRooms 서비스 사용 (페이지네이션 파라미터 추가)
      const data = await fetchGroupChatRooms(page, size, navigate, location);

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
      loadChatRoom(page, size);
    } catch (error) {
      console.error("채팅방 생성 중 오류 발생:", error);
    }
  };

  return (
    <Container>
      <Card>
        <CardHeader title="채팅방 목록" />
        <CardContent>
          <Button
            variant="contained"
            color="secondary"
            onClick={() => setShowCreateRoomModal(true)}
            sx={{ float: "right", mb: 2 }}
          >
            채팅방 생성
          </Button>

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
                        채팅방이 없습니다.
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
