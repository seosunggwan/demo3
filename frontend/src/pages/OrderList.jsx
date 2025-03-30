import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";

const OrderList = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [orderSearch, setOrderSearch] = useState({
    memberName: "",
    orderStatus: null,
  });
  const { isLoggedIn, getAccessToken, logout } = useLogin();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login", { state: "/orders" });
      return;
    }

    // 초기 로드시에는 검색 조건 없이 전체 주문 목록 조회
    fetchOrders();
  }, [isLoggedIn, navigate, getAccessToken]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const token = getAccessToken();

      if (!token) {
        console.error("토큰이 없습니다.");
        alert("로그인이 필요합니다.");
        logout(); // 토큰이 없으면 로그아웃 처리
        navigate("/login", { state: "/orders" });
        return;
      }

      console.log("사용 토큰:", token);
      console.log("검색 조건:", {
        memberName: orderSearch.memberName || "",
        orderStatus: orderSearch.orderStatus || "",
      });

      // URL 파라미터 직접 구성
      let url = "/api/orders/search";
      const params = {};

      // 빈 문자열이 아닌 경우에만 파라미터로 추가
      if (orderSearch.memberName && orderSearch.memberName.trim() !== "") {
        params.memberName = orderSearch.memberName.trim();
      }

      if (orderSearch.orderStatus) {
        params.orderStatus = orderSearch.orderStatus;
      }

      // 검색 API 호출
      const response = await axios.get(url, {
        params: params,
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        // 타임아웃 설정 추가
        timeout: 10000,
      });

      console.log("응답 데이터:", response.data);
      setOrders(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("주문 목록을 불러오는데 실패했습니다:", error);
      console.error("에러 상세:", {
        status: error.response?.status,
        data: error.response?.data,
        headers: error.response?.headers,
        message: error.message,
      });

      if (error.response?.status === 401) {
        console.log(
          "인증 토큰이 만료되었거나 유효하지 않습니다. 재로그인 필요"
        );
        alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
        // 토큰 만료 시 로그아웃 처리
        logout();
        navigate("/login", { state: "/orders" });
      } else {
        setError(
          "주문 목록을 불러오는데 실패했습니다: " +
            (error.response?.data || error.message)
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchOrders();
  };

  const handleCancelOrder = async (orderId) => {
    try {
      const token = getAccessToken();

      if (!token) {
        console.error("토큰이 없습니다.");
        alert("로그인이 필요합니다.");
        navigate("/login", { state: "/orders" });
        return;
      }

      await axios.post(
        `/api/orders/${orderId}/cancel`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      alert("주문이 취소되었습니다.");
      fetchOrders();
    } catch (error) {
      console.error("주문 취소에 실패했습니다:", error);
      if (error.response?.status === 401) {
        alert("로그인이 필요합니다.");
        navigate("/login", { state: "/orders" });
      } else {
        alert("주문 취소에 실패했습니다.");
      }
    }
  };

  const formatDate = (dateStr) => {
    try {
      if (!dateStr) return "날짜 정보 없음";

      // 숫자 문자열이 밀리초 타임스탬프인 경우
      const timestamp = Number(dateStr);
      if (!isNaN(timestamp) && String(timestamp).length > 10) {
        const date = new Date(timestamp);
        if (date.toString() !== "Invalid Date") {
          return date.toLocaleString("ko-KR", {
            year: "numeric",
            month: "long",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            hour12: true,
          });
        }
      }

      // ISO 형식 문자열인 경우
      const date = new Date(dateStr);
      if (date.toString() !== "Invalid Date") {
        return date.toLocaleString("ko-KR", {
          year: "numeric",
          month: "long",
          day: "numeric",
          hour: "2-digit",
          minute: "2-digit",
          hour12: true,
        });
      }

      return dateStr;
    } catch (e) {
      console.error("날짜 변환 오류:", e);
      return dateStr;
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
    <div style={{ maxWidth: "1000px", margin: "0 auto", padding: "20px" }}>
      <h1
        style={{ fontSize: "24px", fontWeight: "bold", marginBottom: "20px" }}
      >
        주문 목록
      </h1>

      <form
        onSubmit={handleSearch}
        style={{
          marginBottom: "20px",
          padding: "15px",
          backgroundColor: "#f9fafb",
          borderRadius: "8px",
          border: "1px solid #eee",
        }}
      >
        <div
          style={{
            display: "flex",
            flexWrap: "wrap",
            gap: "15px",
            alignItems: "flex-end",
          }}
        >
          <div style={{ flex: "1", minWidth: "200px" }}>
            <label
              style={{
                display: "block",
                marginBottom: "6px",
                fontWeight: "500",
              }}
            >
              회원명
            </label>
            <input
              type="text"
              value={orderSearch.memberName}
              onChange={(e) =>
                setOrderSearch({
                  ...orderSearch,
                  memberName: e.target.value,
                })
              }
              placeholder="회원 이름 입력"
              style={{
                width: "100%",
                padding: "8px",
                border: "1px solid #ddd",
                borderRadius: "4px",
              }}
            />
          </div>
          <div style={{ flex: "1", minWidth: "200px" }}>
            <label
              style={{
                display: "block",
                marginBottom: "6px",
                fontWeight: "500",
              }}
            >
              주문상태
            </label>
            <select
              value={orderSearch.orderStatus || ""}
              onChange={(e) =>
                setOrderSearch({
                  ...orderSearch,
                  orderStatus: e.target.value || null,
                })
              }
              style={{
                width: "100%",
                padding: "8px",
                border: "1px solid #ddd",
                borderRadius: "4px",
                backgroundColor: "white",
              }}
            >
              <option value="">전체</option>
              <option value="ORDER">주문</option>
              <option value="CANCEL">취소</option>
            </select>
          </div>
          <div>
            <button
              type="submit"
              style={{
                padding: "8px 16px",
                backgroundColor: "#4F46E5",
                color: "white",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
              }}
            >
              검색
            </button>
          </div>
        </div>
      </form>

      <div style={{ overflowX: "auto" }}>
        <table
          style={{
            width: "100%",
            borderCollapse: "collapse",
            marginTop: "10px",
          }}
        >
          <thead>
            <tr
              style={{
                backgroundColor: "#f3f4f6",
                borderBottom: "1px solid #e5e7eb",
              }}
            >
              <th
                style={{
                  padding: "10px",
                  textAlign: "left",
                  fontWeight: "500",
                }}
              >
                주문번호
              </th>
              <th
                style={{
                  padding: "10px",
                  textAlign: "left",
                  fontWeight: "500",
                }}
              >
                회원명
              </th>
              <th
                style={{
                  padding: "10px",
                  textAlign: "left",
                  fontWeight: "500",
                }}
              >
                주문상태
              </th>
              <th
                style={{
                  padding: "10px",
                  textAlign: "left",
                  fontWeight: "500",
                }}
              >
                주문일시
              </th>
              <th
                style={{
                  padding: "10px",
                  textAlign: "left",
                  fontWeight: "500",
                }}
              >
                주문금액
              </th>
              <th
                style={{
                  padding: "10px",
                  textAlign: "left",
                  fontWeight: "500",
                }}
              >
                관리
              </th>
            </tr>
          </thead>
          <tbody>
            {orders.length > 0 ? (
              orders.map((order) => (
                <tr
                  key={order.orderId}
                  style={{ borderBottom: "1px solid #e5e7eb" }}
                >
                  <td style={{ padding: "12px 10px" }}>{order.orderId}</td>
                  <td style={{ padding: "12px 10px" }}>{order.memberName}</td>
                  <td style={{ padding: "12px 10px" }}>
                    <span
                      style={{
                        display: "inline-block",
                        padding: "2px 8px",
                        borderRadius: "12px",
                        fontSize: "14px",
                        backgroundColor:
                          order.orderStatus === "ORDER" ? "#dcfce7" : "#fee2e2",
                        color:
                          order.orderStatus === "ORDER" ? "#166534" : "#991b1b",
                      }}
                    >
                      {order.orderStatus === "ORDER" ? "주문" : "취소"}
                    </span>
                  </td>
                  <td style={{ padding: "12px 10px" }}>
                    {formatDate(order.orderDate)}
                  </td>
                  <td style={{ padding: "12px 10px", fontWeight: "500" }}>
                    {order.orderItems
                      .reduce(
                        (total, item) => total + item.orderPrice * item.count,
                        0
                      )
                      .toLocaleString()}
                    원
                  </td>
                  <td style={{ padding: "12px 10px" }}>
                    {order.orderStatus === "ORDER" && (
                      <button
                        onClick={() => handleCancelOrder(order.orderId)}
                        style={{
                          backgroundColor: "transparent",
                          color: "#ef4444",
                          border: "none",
                          cursor: "pointer",
                          fontSize: "14px",
                          fontWeight: "500",
                        }}
                      >
                        취소
                      </button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td
                  colSpan="6"
                  style={{ padding: "40px 10px", textAlign: "center" }}
                >
                  <div>
                    <p style={{ fontSize: "16px", marginBottom: "8px" }}>
                      주문 내역이 없습니다.
                    </p>
                    <p style={{ fontSize: "14px", color: "#6b7280" }}>
                      새로운 주문을 생성해보세요.
                    </p>
                  </div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrderList;
