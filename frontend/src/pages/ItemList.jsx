import React, { useState, useEffect, Suspense } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useLogin } from "../contexts/AuthContext";
import axiosInstance from "../utils/axios";
import { decodeToken, redirectToLogin } from "../utils/auth";

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

  // 페이지네이션 컴포넌트
  const Pagination = () => {
    const pageButtons = [];
    const maxButtonCount = 5; // 표시할 최대 페이지 버튼 수

    // 현재 페이지 주변 버튼만 표시하기 위한 시작/끝 계산
    let startPage = Math.max(0, pageInfo.page - Math.floor(maxButtonCount / 2));
    let endPage = Math.min(
      pageInfo.totalPages - 1,
      startPage + maxButtonCount - 1
    );

    // 최대 버튼 수에 맞게 시작 페이지 조정
    if (endPage - startPage + 1 < maxButtonCount) {
      startPage = Math.max(0, endPage - maxButtonCount + 1);
    }

    // 이전 페이지 버튼
    pageButtons.push(
      <button
        key="prev"
        onClick={() => handlePageChange(pageInfo.page - 1)}
        disabled={!pageInfo.hasPrevious}
        style={{
          padding: "5px 10px",
          margin: "0 5px",
          backgroundColor: !pageInfo.hasPrevious ? "#e5e7eb" : "#f3f4f6",
          border: "1px solid #d1d5db",
          borderRadius: "4px",
          cursor: pageInfo.hasPrevious ? "pointer" : "not-allowed",
        }}
      >
        이전
      </button>
    );

    // 페이지 번호 버튼들
    for (let i = startPage; i <= endPage; i++) {
      pageButtons.push(
        <button
          key={i}
          onClick={() => handlePageChange(i)}
          style={{
            padding: "5px 10px",
            margin: "0 5px",
            backgroundColor: i === pageInfo.page ? "#4F46E5" : "#f3f4f6",
            color: i === pageInfo.page ? "white" : "black",
            border: "1px solid #d1d5db",
            borderRadius: "4px",
            cursor: "pointer",
          }}
        >
          {i + 1}
        </button>
      );
    }

    // 다음 페이지 버튼
    pageButtons.push(
      <button
        key="next"
        onClick={() => handlePageChange(pageInfo.page + 1)}
        disabled={!pageInfo.hasNext}
        style={{
          padding: "5px 10px",
          margin: "0 5px",
          backgroundColor: !pageInfo.hasNext ? "#e5e7eb" : "#f3f4f6",
          border: "1px solid #d1d5db",
          borderRadius: "4px",
          cursor: pageInfo.hasNext ? "pointer" : "not-allowed",
        }}
      >
        다음
      </button>
    );

    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          marginTop: "20px",
          padding: "10px",
        }}
      >
        {pageButtons}
      </div>
    );
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
      <div style={{ textAlign: "center", marginTop: "20px" }}>로딩 중...</div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: "center", marginTop: "20px" }}>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>다시 시도</button>
      </div>
    );
  }

  return (
    <Suspense fallback={<div>로딩 중...</div>}>
      <div style={{ maxWidth: "800px", margin: "0 auto", padding: "20px" }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "20px",
          }}
        >
          <h1 style={{ fontSize: "24px", fontWeight: "bold" }}>상품 목록</h1>
          <Link
            to="/items/new"
            style={{
              padding: "8px 16px",
              backgroundColor: "#4F46E5",
              color: "white",
              textDecoration: "none",
              borderRadius: "4px",
            }}
          >
            상품 등록
          </Link>
        </div>

        {/* 검색 폼 추가 */}
        <div className="mb-6">
          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              type="text"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder="상품명으로 검색"
              className="flex-1 p-2 border border-gray-300 rounded"
            />
            <button
              type="submit"
              className="bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded"
            >
              검색
            </button>
            {keyword && (
              <button
                type="button"
                onClick={handleClearSearch}
                className="bg-gray-300 hover:bg-gray-400 text-gray-800 py-2 px-4 rounded"
              >
                초기화
              </button>
            )}
          </form>
        </div>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))",
            gap: "16px",
          }}
        >
          {Array.isArray(items) && items.length > 0 ? (
            items.map((item) => (
              <div
                key={item.id}
                style={{
                  border: "1px solid #ddd",
                  borderRadius: "8px",
                  padding: "16px",
                  display: "flex",
                  flexDirection: "column",
                  height: "100%",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
                  transition: "transform 0.2s, box-shadow 0.2s",
                  cursor: "pointer",
                  overflow: "hidden",
                }}
                onClick={() => navigate(`/items/${item.id}/edit`)}
              >
                <div
                  style={{
                    position: "relative",
                    width: "100%",
                    paddingBottom: "75%", // 4:3 비율
                    marginBottom: "12px",
                    backgroundColor: "#f5f5f5",
                    borderRadius: "4px",
                    overflow: "hidden",
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
                    <div
                      style={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        color: "#9ca3af",
                        fontSize: "14px",
                      }}
                    >
                      이미지 없음
                    </div>
                  )}
                </div>

                <h2
                  style={{
                    fontSize: "18px",
                    fontWeight: "bold",
                    marginBottom: "8px",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {item.name}
                </h2>

                <div style={{ marginBottom: "16px", flex: 1 }}>
                  <div style={{ marginBottom: "4px", color: "#374151" }}>
                    가격: {item.price.toLocaleString()}원
                  </div>
                  <div
                    style={{
                      color: item.stockQuantity > 0 ? "#374151" : "#ef4444",
                    }}
                  >
                    재고: {item.stockQuantity}개
                    {item.stockQuantity === 0 && " (품절)"}
                  </div>
                </div>

                <div
                  style={{
                    display: "flex",
                    gap: "8px",
                    marginTop: "auto",
                  }}
                  onClick={(e) => e.stopPropagation()} // 버튼 클릭 시 상위 요소의 onClick 방지
                >
                  <Link
                    to={`/items/${item.id}/edit`}
                    style={{
                      padding: "6px 12px",
                      backgroundColor: "#E5E7EB",
                      color: "#374151",
                      textDecoration: "none",
                      borderRadius: "4px",
                      fontSize: "14px",
                      textAlign: "center",
                      flex: 1,
                    }}
                  >
                    수정
                  </Link>
                  <button
                    onClick={() => handleDelete(item.id)}
                    style={{
                      padding: "6px 12px",
                      backgroundColor: "#fee2e2",
                      color: "#b91c1c",
                      border: "none",
                      borderRadius: "4px",
                      cursor: "pointer",
                      fontSize: "14px",
                      flex: 1,
                    }}
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div
              style={{
                padding: "32px",
                textAlign: "center",
                backgroundColor: "#f9fafb",
                borderRadius: "8px",
                gridColumn: "1 / -1",
              }}
            >
              <p style={{ marginBottom: "16px", color: "#4b5563" }}>
                등록된 상품이 없습니다.
              </p>
              <Link
                to="/items/new"
                style={{
                  padding: "8px 16px",
                  backgroundColor: "#4F46E5",
                  color: "white",
                  textDecoration: "none",
                  borderRadius: "4px",
                  display: "inline-block",
                }}
              >
                상품 등록하기
              </Link>
            </div>
          )}
        </div>

        {pageInfo.totalPages > 1 && <Pagination />}
      </div>
    </Suspense>
  );
};

export default ItemList;
