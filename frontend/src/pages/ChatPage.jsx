import { useEffect, useRef, useState } from "react";
import {
  TextField,
  Button,
  Card,
  CardContent,
  Typography,
  Container,
  Box,
  Paper,
  Avatar,
  CircularProgress,
  Divider,
  Alert,
  IconButton,
  Grid,
} from "@mui/material";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import SendIcon from "@mui/icons-material/Send";
import RefreshIcon from "@mui/icons-material/Refresh";

export default function ChatPage() {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [status, setStatus] = useState("connecting"); // 연결 상태: connecting, connected, error
  const ws = useRef(null);
  const chatBoxRef = useRef(null);
  const currentUser = localStorage.getItem("email") || "게스트";

  useEffect(() => {
    connectWebSocket();

    return () => {
      if (ws.current) {
        ws.current.disconnect(() => console.log("🔴 웹소켓 연결 종료"));
      }
    };
  }, []);

  const connectWebSocket = () => {
    setStatus("connecting");

    const socket = new SockJS("http://localhost:8080/connect");
    const client = Stomp.over(socket);

    // 디버그 로그 비활성화
    client.debug = null;

    client.connect(
      {
        Authorization: `Bearer ${localStorage.getItem("access_token")}`,
      },
      () => {
        console.log("🟢 STOMP 웹소켓 연결 성공");
        setStatus("connected");
        client.subscribe("/topic/1", (message) => {
          const receivedMessage = JSON.parse(message.body);
          console.log("받은 메시지 전체 데이터:", receivedMessage);
          console.log("메시지 시간:", receivedMessage.updateTime);
          setMessages((prev) => [...prev, receivedMessage]);
          scrollToBottom();
        });
      },
      (error) => {
        console.error("🔴 웹소켓 연결 에러:", error);
        setStatus("error");
      }
    );

    ws.current = client;
  };

  const sendMessage = () => {
    if (!newMessage.trim() || status !== "connected") return;

    if (ws.current) {
      const chatMessage = {
        roomId: 1,
        message: newMessage,
        senderEmail: currentUser,
      };
      ws.current.send("/publish/1", {}, JSON.stringify(chatMessage));
      setNewMessage("");
    }
  };

  const scrollToBottom = () => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleReconnect = () => {
    connectWebSocket();
  };

  const renderMessageItem = (msg, idx) => {
    const isCurrentUser = msg.senderEmail === currentUser;
    console.log(`메시지 ${idx} 데이터:`, {
      content: msg.message,
      sender: msg.senderEmail,
      time: msg.updateTime,
    });
    const messageTime = msg.updateTime
      ? new Date(msg.updateTime).toLocaleTimeString("ko-KR", {
          hour: "2-digit",
          minute: "2-digit",
        })
      : "";

    return (
      <Box
        key={idx}
        sx={{
          display: "flex",
          flexDirection: isCurrentUser ? "row-reverse" : "row",
          mb: 2,
          alignItems: "flex-end",
        }}
      >
        {!isCurrentUser && (
          <Avatar
            sx={{
              mr: 1,
              bgcolor: isCurrentUser ? "primary.main" : "secondary.main",
              width: 35,
              height: 35,
            }}
          >
            {msg.senderEmail.charAt(0).toUpperCase()}
          </Avatar>
        )}

        <Box sx={{ maxWidth: "70%" }}>
          {!isCurrentUser && (
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{ ml: 1, mb: 0.5 }}
            >
              {msg.senderEmail}
            </Typography>
          )}

          <Paper
            sx={{
              p: 1.5,
              borderRadius: 2,
              bgcolor: isCurrentUser ? "primary.main" : "grey.100",
              color: isCurrentUser ? "white" : "text.primary",
            }}
          >
            <Box sx={{ display: "flex", alignItems: "flex-end", gap: 1 }}>
              <Typography variant="body1" sx={{ whiteSpace: "pre-wrap" }}>
                {msg.message}
              </Typography>
              <Typography
                variant="caption"
                sx={{
                  color: isCurrentUser
                    ? "rgba(255, 255, 255, 0.7)"
                    : "text.secondary",
                  whiteSpace: "nowrap",
                }}
              >
                {msg.updateTime
                  ? new Date(msg.updateTime).toLocaleTimeString("ko-KR", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })
                  : ""}
              </Typography>
            </Box>
          </Paper>
        </Box>
      </Box>
    );
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Card sx={{ height: "80vh", display: "flex", flexDirection: "column" }}>
        <CardContent
          sx={{
            p: 3,
            flexShrink: 0,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <Typography variant="h5" fontWeight="medium">
            📌 STOMP 채팅방
          </Typography>

          {status === "error" && (
            <Button color="primary" onClick={handleReconnect}>
              재연결
            </Button>
          )}
        </CardContent>

        {status !== "connected" && status !== "connecting" && (
          <Alert
            severity="error"
            action={
              <IconButton
                color="inherit"
                size="small"
                onClick={handleReconnect}
              >
                <RefreshIcon />
              </IconButton>
            }
            sx={{ mb: 2 }}
          >
            서버 연결이 끊겼습니다. 재연결을 시도하세요.
          </Alert>
        )}

        <Divider />

        <Box
          sx={{
            flex: 1,
            overflow: "hidden",
            display: "flex",
            flexDirection: "column",
          }}
        >
          {status === "connecting" ? (
            <Box
              sx={{
                flex: 1,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <CircularProgress />
              <Typography variant="body1" sx={{ ml: 2 }}>
                채팅방에 연결 중...
              </Typography>
            </Box>
          ) : (
            <Box
              ref={chatBoxRef}
              sx={{
                flex: 1,
                overflowY: "auto",
                p: 3,
                bgcolor: "background.default",
              }}
            >
              {messages.length === 0 ? (
                <Box
                  sx={{
                    height: "100%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  <Typography variant="body1" color="textSecondary">
                    아직 메시지가 없습니다. 첫 메시지를 보내보세요!
                  </Typography>
                </Box>
              ) : (
                messages.map((msg, idx) => renderMessageItem(msg, idx))
              )}
            </Box>
          )}

          <Divider />

          <Box sx={{ p: 2, bgcolor: "background.paper" }}>
            <Grid container spacing={1} alignItems="center">
              <Grid item xs={10}>
                <TextField
                  fullWidth
                  variant="outlined"
                  size="small"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="메시지를 입력하세요"
                  onKeyPress={handleKeyPress}
                  disabled={status !== "connected"}
                />
              </Grid>
              <Grid item xs={2}>
                <Button
                  fullWidth
                  variant="contained"
                  color="primary"
                  onClick={sendMessage}
                  disabled={status !== "connected" || !newMessage.trim()}
                  endIcon={<SendIcon />}
                >
                  전송
                </Button>
              </Grid>
            </Grid>
          </Box>
        </Box>
      </Card>
    </Container>
  );
}
