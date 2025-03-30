import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
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
} from "@mui/material";

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
  const [chatList, setChatList] = useState([]);
  const [serverStatus, setServerStatus] = useState("offline");
  const [error, setError] = useState(null);

  useEffect(() => {
    checkServerStatus();
  }, []);

  useEffect(() => {
    if (serverStatus === "online") {
      loadChatList();
    }
  }, [serverStatus]);

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

  const loadChatList = async () => {
    try {
      // axiosInstance 사용 (인터셉터에서 토큰 관리)
      const response = await axiosInstance.get("/chat/my/rooms");
      setChatList(response.data);
    } catch (error) {
      console.error("채팅 목록 불러오기 실패:", error);
      setError("채팅 목록을 불러오는 데 실패했습니다.");

      // 인증 오류 처리
      if (error.response?.status === 401) {
        navigate("/login", { state: "/mychatpage" });
      }
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
                      참여 중인 채팅방이 없습니다.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Container>
  );
};

export default MyChatPage;
