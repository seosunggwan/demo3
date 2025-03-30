import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axiosInstance from "../utils/axios";
import { useLogin } from "../contexts/AuthContext";
import ImageUploader from "../components/ImageUploader";

const ItemForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { isLoggedIn } = useLogin();
  const [formData, setFormData] = useState({
    name: "",
    price: 0,
    stockQuantity: 0,
    author: "",
    isbn: "",
    imageUrl: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login", { state: `/items/${id ? `${id}/edit` : "new"}` });
      return;
    }

    if (id) {
      const fetchItem = async () => {
        try {
          setLoading(true);
          const response = await axiosInstance.get(`/api/items/${id}/edit`);
          setFormData(response.data);
        } catch (error) {
          console.error("상품 정보를 불러오는데 실패했습니다:", error);
          if (error.response?.status === 401) {
            alert("로그인이 필요합니다.");
            navigate("/login", { state: `/items/${id}/edit` });
          } else {
            setError("상품 정보를 불러오는데 실패했습니다.");
          }
        } finally {
          setLoading(false);
        }
      };

      fetchItem();
    }
  }, [id, isLoggedIn, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        name === "price" || name === "stockQuantity"
          ? parseInt(value) || 0
          : value,
    }));
  };

  const handleImageUploaded = (imageUrl) => {
    setFormData((prev) => ({
      ...prev,
      imageUrl,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);

      // 인증 토큰 확인
      const token = localStorage.getItem("access_token");
      if (!token || !isLoggedIn) {
        alert("로그인이 필요합니다.");
        navigate("/login", { state: `/items/${id ? `${id}/edit` : "new"}` });
        return;
      }

      // 데이터 전송
      if (id) {
        await axiosInstance.put(`/api/items/${id}`, formData);
      } else {
        await axiosInstance.post("/api/items", formData);
      }
      navigate("/items");
    } catch (error) {
      console.error("상품 저장에 실패했습니다:", error);

      // 인증 오류인 경우
      if (
        error.response?.status === 401 ||
        (error.message &&
          (error.message.includes("인증") || error.message.includes("로그인")))
      ) {
        alert(error.message || "인증이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("access_token"); // 토큰 제거
        navigate("/login", { state: `/items/${id ? `${id}/edit` : "new"}` });
      } else if (error.message) {
        setError(error.message);
      } else {
        setError("상품 저장에 실패했습니다.");
      }
    } finally {
      setLoading(false);
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
    <div style={{ maxWidth: "800px", margin: "0 auto", padding: "20px" }}>
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
        <h1 style={{ fontSize: "24px", fontWeight: "bold" }}>
          {id ? "상품 수정" : "상품 등록"}
        </h1>
        <button
          onClick={() => navigate("/items")}
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
        <div style={{ marginBottom: "20px" }}>
          <label
            htmlFor="name"
            style={{
              display: "block",
              marginBottom: "5px",
              fontSize: "14px",
              fontWeight: "bold",
            }}
          >
            상품명
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            style={{
              width: "100%",
              padding: "10px",
              fontSize: "16px",
              border: "1px solid #ddd",
              borderRadius: "4px",
            }}
          />
        </div>

        <div style={{ marginBottom: "20px" }}>
          <label
            htmlFor="price"
            style={{
              display: "block",
              marginBottom: "5px",
              fontSize: "14px",
              fontWeight: "bold",
            }}
          >
            가격
          </label>
          <input
            type="number"
            id="price"
            name="price"
            value={formData.price}
            onChange={handleChange}
            required
            min="0"
            style={{
              width: "100%",
              padding: "10px",
              fontSize: "16px",
              border: "1px solid #ddd",
              borderRadius: "4px",
            }}
          />
        </div>

        <div style={{ marginBottom: "20px" }}>
          <label
            htmlFor="stockQuantity"
            style={{
              display: "block",
              marginBottom: "5px",
              fontSize: "14px",
              fontWeight: "bold",
            }}
          >
            재고수량
          </label>
          <input
            type="number"
            id="stockQuantity"
            name="stockQuantity"
            value={formData.stockQuantity}
            onChange={handleChange}
            required
            min="0"
            style={{
              width: "100%",
              padding: "10px",
              fontSize: "16px",
              border: "1px solid #ddd",
              borderRadius: "4px",
            }}
          />
        </div>

        <div style={{ marginBottom: "20px" }}>
          <label
            htmlFor="author"
            style={{
              display: "block",
              marginBottom: "5px",
              fontSize: "14px",
              fontWeight: "bold",
            }}
          >
            저자
          </label>
          <input
            type="text"
            id="author"
            name="author"
            value={formData.author || ""}
            onChange={handleChange}
            style={{
              width: "100%",
              padding: "10px",
              fontSize: "16px",
              border: "1px solid #ddd",
              borderRadius: "4px",
            }}
          />
        </div>

        <div style={{ marginBottom: "20px" }}>
          <label
            htmlFor="isbn"
            style={{
              display: "block",
              marginBottom: "5px",
              fontSize: "14px",
              fontWeight: "bold",
            }}
          >
            ISBN
          </label>
          <input
            type="text"
            id="isbn"
            name="isbn"
            value={formData.isbn || ""}
            onChange={handleChange}
            style={{
              width: "100%",
              padding: "10px",
              fontSize: "16px",
              border: "1px solid #ddd",
              borderRadius: "4px",
            }}
          />
        </div>

        <div style={{ marginBottom: "20px" }}>
          <label
            style={{
              display: "block",
              marginBottom: "5px",
              fontSize: "14px",
              fontWeight: "bold",
            }}
          >
            상품 이미지
          </label>
          <ImageUploader
            onImageUploaded={handleImageUploaded}
            initialImage={formData.imageUrl}
          />
        </div>

        <div style={{ marginTop: "30px" }}>
          <button
            type="submit"
            disabled={loading}
            style={{
              backgroundColor: "#4F46E5",
              color: "white",
              padding: "10px 20px",
              fontSize: "16px",
              border: "none",
              borderRadius: "4px",
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.7 : 1,
            }}
          >
            {loading ? "저장 중..." : id ? "수정하기" : "등록하기"}
          </button>
          <button
            type="button"
            onClick={() => navigate("/items")}
            style={{
              backgroundColor: "#6B7280",
              color: "white",
              padding: "10px 20px",
              fontSize: "16px",
              border: "none",
              borderRadius: "4px",
              marginLeft: "10px",
              cursor: "pointer",
            }}
          >
            취소
          </button>
        </div>
      </form>
    </div>
  );
};

export default ItemForm;
