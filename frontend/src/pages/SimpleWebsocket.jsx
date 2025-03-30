import { useEffect, useState } from "react";
import SockJS from "sockjs-client";

const SimpleWebsocket = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    const loadStomp = async () => {
      const Stomp = (await import("stompjs")).default; // ✅ Dynamic Import 사용
      const socket = new SockJS("http://localhost:8080/connect");
      const client = Stomp.over(socket);

      client.connect({}, () => {
        console.log("✅ WebSocket 연결 성공");
        client.subscribe("/topic/1", (message) => {
          setMessages((prevMessages) => [
            ...prevMessages,
            JSON.parse(message.body),
          ]);
        });
      });

      setStompClient(client);
    };

    loadStomp();

    return () => {
      if (stompClient) {
        stompClient.disconnect(() => {
          console.log("❌ WebSocket 연결 종료");
        });
      }
    };
  }, []);

  const sendMessage = () => {
    if (stompClient && input.trim()) {
      const chatMessage = {
        roomId: 1,
        message: input,
        senderEmail: "test@example.com",
      };
      stompClient.send("/publish/1", {}, JSON.stringify(chatMessage));
      setInput("");
    }
  };

  return (
    <div>
      <h2>WebSocket 채팅</h2>
      <div>
        {messages.map((msg, index) => (
          <p key={index}>
            <strong>{msg.senderEmail}:</strong> {msg.message}
          </p>
        ))}
      </div>
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="메시지를 입력하세요..."
      />
      <button onClick={sendMessage}>전송</button>
    </div>
  );
};

export default SimpleWebsocket;
