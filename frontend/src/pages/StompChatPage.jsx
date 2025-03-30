import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

import {
  Container,
  Card,
  CardContent,
  CardHeader,
  TextField,
  Button,
  CircularProgress,
  Alert,
  Box,
} from "@mui/material";
import fetchChatHistory from "../services/fetchChatHistory";
import markChatAsRead from "../services/markChatAsRead";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function StompChatPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  // 상태 변수
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [connected, setConnected] = useState(false);
  const [connectionFailed, setConnectionFailed] = useState(false);

  const [serverStatus, setServerStatus] = useState("checking");
  const [reconnectAttempt, setReconnectAttempt] = useState(0);
  const maxReconnectAttempts = 5;

  // 참조
  const senderEmail = localStorage.getItem("email");
  const stompClient = useRef(null);
  const chatBoxRef = useRef(null);

  /**
   * 컴포넌트 마운트 시점에 서버 상태 확인
   */
  useEffect(() => {
    console.log(`🔄 채팅 페이지 마운트됨, roomId: ${roomId}`);
    setReconnectAttempt(0);
    checkServerStatus();

    return () => {
      disconnectWebSocket();
    };
  }, [roomId, navigate, location]);

  /**
   * 서버 상태가 "online"이면 채팅 기록 + WebSocket 연결
   */
  useEffect(() => {
    if (serverStatus === "online") {
      loadChatHistory();
      connectWebSocket();
    }
  }, [serverStatus]);

  /**
   * 서버 상태 확인
   */
  const checkServerStatus = async () => {
    try {
      setServerStatus("checking");
      setLoading(true);

      const token = localStorage.getItem("access_token");
      if (!token) {
        console.error("🚨 토큰이 없습니다.");
        navigate("/login", { state: location.pathname });
        return;
      }

      const response = await fetch(`${API_BASE_URL}/health`, {
        method: "GET",
        signal: AbortSignal.timeout(5000),
        cache: "no-cache",
        headers: {
          Authorization: `Bearer ${token}`,
          "Cache-Control": "no-cache",
          Pragma: "no-cache",
        },
      });

      if (response.ok) {
        console.log("✅ 서버 온라인 상태 확인");
        setServerStatus("online");
      } else {
        console.error("❌ 서버 응답 오류:", response.status);
        setServerStatus("offline");
        setConnectionFailed(true);
      }
    } catch (error) {
      console.error("❌ 서버 상태 확인 실패:", error);
      setServerStatus("offline");
      setConnectionFailed(true);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 채팅 기록 불러오기
   */
  const loadChatHistory = async () => {
    if (serverStatus !== "online") {
      console.error("❌ 서버가 오프라인 상태입니다. 채팅 기록 불가");
      return;
    }

    try {
      setLoading(true);
      console.log("📥 채팅 기록 불러오는 중...");
      const data = await fetchChatHistory(roomId, navigate, location);

      if (data && Array.isArray(data)) {
        console.log(`📥 불러온 채팅 기록: ${data.length}개 메시지`);
        setMessages(data);
      } else {
        console.error("❌ 채팅 기록 데이터가 올바르지 않습니다.");
        setMessages([]);
      }
    } catch (error) {
      console.error("❌ 채팅 기록 불러오기 실패:", error);
      setMessages([]);
    } finally {
      setLoading(false);
    }
  };

  /**
   * WebSocket 연결 - CONNECT 시점에만 JWT 전송
   */
  const connectWebSocket = () => {
    if (serverStatus !== "online") {
      console.error("❌ 서버 오프라인, 연결 시도 중단");
      setConnectionFailed(true);
      return;
    }

    if (stompClient.current && stompClient.current.active) {
      console.log("⚡ 이미 WebSocket 연결됨.");
      setConnected(true);
      return;
    }

    if (reconnectAttempt >= maxReconnectAttempts) {
      console.error(`❌ 최대 재연결 시도(${maxReconnectAttempts}회) 초과`);
      alert("서버 연결에 문제가 있습니다. 새로고침해 주세요.");
      setConnectionFailed(true);
      return;
    }

    console.log(
      `🔌 WebSocket 연결 시도... (시도 ${
        reconnectAttempt + 1
      }/${maxReconnectAttempts})`
    );

    const token = localStorage.getItem("access_token");
    if (!token) {
      console.error("🚨 토큰이 없습니다. 로그인 후 이용해주세요.");
      navigate("/login", { state: location.pathname });
      return;
    }

    resetStompClient();

    stompClient.current = new Client({
      webSocketFactory: () =>
        new SockJS(`${API_BASE_URL}/connect`, null, {
          transports: ["websocket"],
          headers: { Authorization: `Bearer ${token}` },
        }),
      // CONNECT 헤더에만 토큰 전달
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        "accept-version": "1.1,1.2",
      },
      debug: (msg) => console.log("🔍 STOMP 디버그:", msg),

      onConnect: (frame) => {
        console.log("✅ WebSocket 연결 성공!", frame);
        setConnected(true);
        setConnectionFailed(false);
        setReconnectAttempt(0);

        // 이제 SUBSCRIBE는 헤더 없이
        stompClient.current.subscribe(
          `/topic/${roomId}`,
          (message) => {
            console.log("📩 받은 메시지:", message.body);
            const parsedMessage = JSON.parse(message.body);
            setMessages((prev) => [...prev, parsedMessage]);
            scrollToBottom();
          },
          { Authorization: `Bearer ${token}` }
        );

        console.log(`📌 구독한 경로: /topic/${roomId}`);
      },

      onStompError: (frame) => {
        console.error("❌ STOMP 에러:", frame.headers["message"]);
        console.error("📄 상세:", frame.body);
        setConnected(false);
        setConnectionFailed(true);
      },

      onWebSocketClose: (evt) => {
        console.error("❌ 소켓 연결 끊김:", evt);
        setConnected(false);
        setConnectionFailed(true);

        const nextAttempt = reconnectAttempt + 1;
        setReconnectAttempt(nextAttempt);
        if (nextAttempt < maxReconnectAttempts) {
          const backoffTime = Math.min(
            1000 * Math.pow(2, reconnectAttempt),
            10000
          );
          console.log(`🔄 ${nextAttempt}번째 재연결 시도 예정...`);
          setTimeout(() => connectWebSocket(), backoffTime);
        }
      },
    });

    stompClient.current.activate();
  };

  /**
   * 메시지 전송
   */
  const sendMessage = () => {
    if (!newMessage.trim()) return;

    if (!stompClient.current || !stompClient.current.active) {
      console.warn("❌ WebSocket이 연결되지 않음. 재연결 시도...");
      connectWebSocket();
      return;
    }

    const token = localStorage.getItem("access_token");

    // 원본 이메일 사용 (변환하지 않음)
    // 서버 측에서 정규화된 형식으로 처리할 것임
    const messageBody = {
      senderEmail: senderEmail,
      message: newMessage,
    };

    console.log("📤 전송할 메시지:", messageBody);

    // publish 시에는 토큰을 붙여도 되고, 안 붙여도 되고(서버 정책에 따라)
    // 여기서는 예시로 계속 붙이는 형식
    stompClient.current.publish({
      destination: `/publish/${roomId}`,
      body: JSON.stringify(messageBody),
      headers: { Authorization: `Bearer ${token}` },
    });

    console.log("📤 메시지 전송 완료");
    setNewMessage("");
  };

  /**
   * WebSocket 연결 해제
   */
  const disconnectWebSocket = async () => {
    console.log("🔌 WebSocket 연결 해제...");
    if (!stompClient.current || !stompClient.current.active) return;

    try {
      await markChatAsRead(roomId);
    } catch (error) {
      console.error("❌ 읽음 처리 실패:", error);
    }

    stompClient.current.deactivate();
    setConnected(false);
  };

  /**
   * STOMP 클라이언트 초기화
   */
  const resetStompClient = () => {
    if (stompClient.current) {
      try {
        if (stompClient.current.active) {
          stompClient.current.deactivate();
        }
        stompClient.current = null;
      } catch (error) {
        console.error("❌ STOMP 클라이언트 재설정 오류:", error);
      }
    }
    setConnected(false);
  };

  /**
   * 스크롤 아래로 이동
   */
  const scrollToBottom = () => {
    setTimeout(() => {
      if (chatBoxRef.current) {
        chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
      }
    }, 100);
  };

  /**
   * 서버 재연결 시도
   */
  const retryConnection = () => {
    setServerStatus("checking");
    setConnectionFailed(false);
    setReconnectAttempt(0);
    setLoading(true);

    (async () => {
      try {
        console.log("🔄 서버 재연결 시도 중...");
        const response = await fetch(`${API_BASE_URL}/health`, {
          method: "GET",
          signal: AbortSignal.timeout(5000),
          cache: "no-cache",
          headers: {
            "Cache-Control": "no-cache",
            Pragma: "no-cache",
          },
        });

        if (response.ok) {
          console.log("✅ 서버 온라인 상태 확인");
          setServerStatus("online");
        } else {
          console.error("❌ 서버 응답 오류:", response.status);
          setServerStatus("offline");
          setConnectionFailed(true);
        }
      } catch (error) {
        console.error("❌ 서버 상태 확인 실패:", error);
        setServerStatus("offline");
        setConnectionFailed(true);
      } finally {
        setLoading(false);
      }
    })();
  };

  return (
    <Container maxWidth="md">
      <Card>
        <CardHeader
          title={`채팅방 #${roomId}`}
          subheader={
            loading
              ? "채팅 기록 불러오는 중..."
              : connected
              ? "연결됨"
              : connectionFailed
              ? "서버 연결 실패"
              : `연결 중... (시도 ${
                  reconnectAttempt + 1
                }/${maxReconnectAttempts})`
          }
        />
        <CardContent>
          {serverStatus === "offline" && (
            <Alert severity="error" sx={{ mb: 2 }}>
              서버에 연결할 수 없습니다. 서버가 오프라인 상태이거나 네트워크
              문제가 있습니다.
            </Alert>
          )}

          {loading ? (
            <div
              style={{ display: "flex", justifyContent: "center", padding: 50 }}
            >
              <CircularProgress />
              <div style={{ marginLeft: 10, marginTop: 5 }}>
                채팅 기록을 불러오는 중입니다...
              </div>
            </div>
          ) : (
            <>
              <div style={chatBoxStyle} ref={chatBoxRef}>
                {messages.length === 0 ? (
                  <div
                    style={{ textAlign: "center", padding: 20, color: "#888" }}
                  >
                    아직 대화 내용이 없습니다. 첫 메시지를 보내보세요!
                  </div>
                ) : (
                  messages.map((msg, idx) => {
                    // 이메일 비교를 위한 정규화 함수
                    const normalizeEmail = (email) => {
                      if (!email) return "";
                      // @oauth.user 제거 및 점(.) 대신 공백으로 대체된 상태 정규화
                      return email
                        .replace("@oauth.user", "")
                        .replace(/\s+/g, ".")
                        .toLowerCase();
                    };

                    // 정규화된 이메일로 비교
                    const isCurrentUser =
                      normalizeEmail(msg.senderEmail) ===
                      normalizeEmail(senderEmail);

                    console.log(
                      `메시지 ${idx}: ${msg.senderEmail} vs ${senderEmail} => ${
                        isCurrentUser ? "내 메시지" : "상대방 메시지"
                      }`
                    );

                    return (
                      <div
                        key={idx}
                        style={
                          isCurrentUser
                            ? sentMessageContainer
                            : receivedMessageContainer
                        }
                      >
                        <div
                          style={
                            isCurrentUser
                              ? sentMessageStyle
                              : receivedMessageStyle
                          }
                        >
                          {!isCurrentUser && (
                            <strong>{msg.senderEmail}: </strong>
                          )}
                          {msg.message}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              {connectionFailed ? (
                <Box sx={{ textAlign: "center", padding: 20, color: "red" }}>
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {serverStatus === "offline"
                      ? "서버가 실행 중인지 확인해 주세요."
                      : "연결 시도 횟수를 초과했습니다."}
                  </Alert>
                  <Button
                    variant="outlined"
                    color="primary"
                    onClick={retryConnection}
                    sx={{ mr: 1 }}
                  >
                    재연결 시도
                  </Button>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={() => window.location.reload()}
                  >
                    페이지 새로고침
                  </Button>
                </Box>
              ) : (
                <>
                  <TextField
                    fullWidth
                    label="메시지 입력"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    onKeyPress={(e) => e.key === "Enter" && sendMessage()}
                    margin="normal"
                    disabled={!connected}
                    placeholder={
                      connected ? "메시지를 입력하세요" : "연결 중..."
                    }
                  />
                  <Button
                    variant="contained"
                    color="primary"
                    fullWidth
                    onClick={sendMessage}
                    disabled={!connected || newMessage.trim() === ""}
                  >
                    {connected ? "전송" : "연결 중..."}
                  </Button>
                </>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </Container>
  );
}

const chatBoxStyle = {
  height: "300px",
  overflowY: "auto",
  border: "1px solid #ddd",
  marginBottom: "10px",
  padding: "10px",
  backgroundColor: "#f9f9f9",
};

const receivedMessageContainer = {
  display: "flex",
  justifyContent: "flex-start",
  marginBottom: "10px",
};

const sentMessageContainer = {
  display: "flex",
  justifyContent: "flex-end",
  marginBottom: "10px",
};

const receivedMessageStyle = {
  backgroundColor: "#eaeaea",
  padding: "10px 15px",
  borderRadius: "18px 18px 18px 4px",
  maxWidth: "70%",
  wordBreak: "break-word",
  boxShadow: "0px 1px 2px rgba(0, 0, 0, 0.1)",
};

const sentMessageStyle = {
  backgroundColor: "#0084ff",
  color: "white",
  padding: "10px 15px",
  borderRadius: "18px 18px 4px 18px",
  maxWidth: "70%",
  wordBreak: "break-word",
  boxShadow: "0px 1px 2px rgba(0, 0, 0, 0.1)",
};
