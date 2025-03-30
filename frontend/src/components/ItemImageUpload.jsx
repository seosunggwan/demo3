import React, { useState, useRef } from "react";

const ItemImageUpload = ({ onImageSelect, initialImageUrl }) => {
  const [previewUrl, setPreviewUrl] = useState(initialImageUrl || "");
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/gif"];
  const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = async (e) => {
    e.preventDefault();
    setIsDragging(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      await handleFileSelect(files[0]);
    }
  };

  const handleFileInputChange = (e) => {
    const files = e.target.files;
    if (files.length > 0) {
      handleFileSelect(files[0]);
    }
  };

  const handleFileSelect = async (file) => {
    // 파일 형식 검증
    if (!ALLOWED_TYPES.includes(file.type)) {
      setError("JPG, PNG, GIF 형식의 이미지만 업로드 가능합니다.");
      return;
    }

    // 파일 크기 검증
    if (file.size > MAX_FILE_SIZE) {
      setError("파일 크기는 5MB를 초과할 수 없습니다.");
      return;
    }

    try {
      setError(null);
      // 파일 미리보기 생성
      const previewUrl = URL.createObjectURL(file);
      setPreviewUrl(previewUrl);
      // 선택된 파일을 부모 컴포넌트로 전달
      onImageSelect(file);
    } catch (error) {
      console.error("이미지 선택 실패:", error);
      setError("이미지 선택에 실패했습니다.");
    }
  };

  return (
    <div>
      <div
        onClick={() => fileInputRef.current?.click()}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        style={{
          border: `2px dashed ${isDragging ? "#4F46E5" : "#ddd"}`,
          borderRadius: "8px",
          padding: "20px",
          textAlign: "center",
          cursor: "pointer",
          backgroundColor: isDragging ? "#F3F4F6" : "#fff",
          transition: "all 0.2s ease",
        }}
      >
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileInputChange}
          accept=".jpg,.jpeg,.png,.gif"
          style={{ display: "none" }}
        />
        {previewUrl ? (
          <div>
            <img
              src={previewUrl}
              alt="선택된 이미지"
              style={{
                maxWidth: "100%",
                maxHeight: "200px",
                objectFit: "contain",
                marginBottom: "10px",
              }}
            />
            <p style={{ color: "#666", fontSize: "14px" }}>
              이미지를 클릭하여 변경
            </p>
          </div>
        ) : (
          <div>
            <p style={{ marginBottom: "10px" }}>
              이미지를 드래그하거나 클릭하여 선택
            </p>
            <p style={{ color: "#666", fontSize: "14px" }}>
              JPG, PNG, GIF 파일 (최대 5MB)
            </p>
          </div>
        )}
      </div>
      {error && (
        <p style={{ color: "#DC2626", fontSize: "14px", marginTop: "8px" }}>
          {error}
        </p>
      )}
    </div>
  );
};

export default ItemImageUpload;
