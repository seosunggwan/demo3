import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useLogin } from "../contexts/AuthContext";

const OrderForm = () => {
  const navigate = useNavigate();
  const { isLoggedIn, getAccessToken } = useLogin();
  const [members, setMembers] = useState([]);
  const [items, setItems] = useState([]);
  const [selectedMember, setSelectedMember] = useState("");
  const [selectedItem, setSelectedItem] = useState("");
  const [count, setCount] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login", { state: "/order" });
      return;
    }

    const fetchData = async () => {
      try {
        setLoading(true);
        const token = getAccessToken();

        if (!token) {
          alert("로그인이 필요합니다.");
          navigate("/login", { state: "/order" });
          return;
        }

        const response = await axios.get("/api/order", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setMembers(
          Array.isArray(response.data.members) ? response.data.members : []
        );
        setItems(Array.isArray(response.data.items) ? response.data.items : []);
      } catch (error) {
        console.error("데이터를 불러오는데 실패했습니다:", error);
        if (error.response?.status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login", { state: "/order" });
        } else {
          setError("데이터를 불러오는데 실패했습니다.");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isLoggedIn, navigate, getAccessToken]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = getAccessToken();

      if (!token) {
        alert("로그인이 필요합니다.");
        navigate("/login", { state: "/order" });
        return;
      }

      await axios.post(
        "/api/orders",
        {
          memberId: selectedMember,
          itemId: selectedItem,
          count: count,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      navigate("/orders");
    } catch (error) {
      console.error("주문 생성에 실패했습니다:", error);
      if (error.response?.status === 401) {
        alert("로그인이 필요합니다.");
        navigate("/login", { state: "/order" });
      } else {
        setError("주문 생성에 실패했습니다.");
      }
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: "center", marginTop: "20px" }}>로딩 중...</div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: "center", marginTop: "20px" }}>
        <p>{error}</p>
        <button
          onClick={() => window.location.reload()}
          style={{
            padding: "8px 16px",
            backgroundColor: "#4F46E5",
            color: "white",
            border: "none",
            borderRadius: "4px",
            marginTop: "10px",
            cursor: "pointer",
          }}
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: "600px", margin: "0 auto", padding: "20px" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "20px",
          borderBottom: "1px solid #ddd",
          paddingBottom: "10px",
        }}
      >
        <h1 style={{ fontSize: "24px", fontWeight: "bold" }}>주문하기</h1>
        <button
          onClick={() => navigate("/orders")}
          style={{
            backgroundColor: "transparent",
            border: "none",
            fontSize: "16px",
            cursor: "pointer",
            color: "#666",
          }}
        >
          닫기
        </button>
      </div>

      <form onSubmit={handleSubmit}>
        <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
          <div>
            <label
              style={{
                display: "block",
                marginBottom: "6px",
                fontWeight: "500",
              }}
            >
              회원 선택
            </label>
            <select
              value={selectedMember}
              onChange={(e) => setSelectedMember(e.target.value)}
              required
              style={{
                width: "100%",
                padding: "10px",
                border: "1px solid #ddd",
                borderRadius: "4px",
                backgroundColor: "white",
              }}
            >
              <option value="">회원을 선택하세요</option>
              {members.map((member) => (
                <option key={member.id} value={member.id}>
                  {member.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              style={{
                display: "block",
                marginBottom: "6px",
                fontWeight: "500",
              }}
            >
              상품 선택
            </label>
            <select
              value={selectedItem}
              onChange={(e) => setSelectedItem(e.target.value)}
              required
              style={{
                width: "100%",
                padding: "10px",
                border: "1px solid #ddd",
                borderRadius: "4px",
                backgroundColor: "white",
              }}
            >
              <option value="">상품을 선택하세요</option>
              {items.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name} - {item.price.toLocaleString()}원
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              style={{
                display: "block",
                marginBottom: "6px",
                fontWeight: "500",
              }}
            >
              주문 수량
            </label>
            <div style={{ position: "relative" }}>
              <input
                type="number"
                value={count}
                onChange={(e) => setCount(parseInt(e.target.value) || 1)}
                min="1"
                required
                style={{
                  width: "100%",
                  padding: "10px",
                  border: "1px solid #ddd",
                  borderRadius: "4px",
                  paddingRight: "30px",
                }}
              />
              <span
                style={{
                  position: "absolute",
                  right: "10px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  color: "#666",
                }}
              >
                개
              </span>
            </div>
          </div>

          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              gap: "10px",
              marginTop: "20px",
              paddingTop: "20px",
              borderTop: "1px solid #eee",
            }}
          >
            <button
              type="button"
              onClick={() => navigate("/orders")}
              style={{
                padding: "10px 20px",
                backgroundColor: "#fff",
                color: "#333",
                border: "1px solid #ddd",
                borderRadius: "4px",
                cursor: "pointer",
              }}
            >
              취소
            </button>
            <button
              type="submit"
              style={{
                padding: "10px 20px",
                backgroundColor: "#4F46E5",
                color: "white",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
              }}
            >
              주문하기
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default OrderForm;
