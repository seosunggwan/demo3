import React, { useState } from "react";
import {
  Button,
  Box,
  CircularProgress,
  Typography,
  IconButton,
  Alert,
} from "@mui/material";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import DeleteIcon from "@mui/icons-material/Delete";
import {
  uploadProfileImage,
  deleteProfileImage,
} from "../services/profileService";

/**
 * 프로필 이미지 업로드 컴포넌트
 * @param {object} props - 컴포넌트 속성
 * @param {string} props.initialImage - 현재 프로필 이미지 URL
 * @param {function} props.onImageUploaded - 이미지 업로드 성공 시 호출되는 함수
 */
const ProfileImageUploader = ({ initialImage, onImageUploaded }) => {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);

  // 파일 선택 처리
  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (!selectedFile) return;

    // 파일 유효성 검사 (이미지 타입, 최대 5MB)
    if (!selectedFile.type.match("image.*")) {
      setError("이미지 파일만 업로드 가능합니다.");
      return;
    }

    if (selectedFile.size > 5 * 1024 * 1024) {
      setError("이미지 크기는 5MB 이하여야 합니다.");
      return;
    }

    setError("");
    setFile(selectedFile);

    // 이미지 미리보기 생성
    const reader = new FileReader();
    reader.onload = () => {
      setPreview(reader.result);
    };
    reader.readAsDataURL(selectedFile);
  };

  // 업로드 진행 상태 업데이트 콜백
  const updateProgress = (progress) => {
    setUploadProgress(progress);
  };

  // 업로드 버튼 클릭 처리
  const handleUpload = async () => {
    if (!file) {
      setError("업로드할 이미지를 선택해주세요.");
      return;
    }

    try {
      setIsUploading(true);
      setError("");

      // 프로필 이미지 업로드 요청
      const imageUrl = await uploadProfileImage(file, updateProgress);

      // 성공 시 콜백 호출
      if (onImageUploaded) {
        onImageUploaded(imageUrl);
      }

      setSuccessMessage("이미지가 성공적으로 업로드되었습니다.");
      setFile(null);
      setPreview(null);
      setUploadProgress(0);
    } catch (err) {
      setError(
        `업로드 중 오류가 발생했습니다: ${err.message || "알 수 없는 오류"}`
      );
    } finally {
      setIsUploading(false);
    }
  };

  // 이미지 삭제 처리
  const handleDelete = async () => {
    if (!initialImage) {
      setError("삭제할 이미지가 없습니다.");
      return;
    }

    if (window.confirm("프로필 이미지를 삭제하시겠습니까?")) {
      try {
        setIsDeleting(true);
        setError("");

        // 프로필 이미지 삭제 요청
        await deleteProfileImage(initialImage);

        // 성공 시 콜백 호출
        if (onImageUploaded) {
          onImageUploaded(null);
        }

        setSuccessMessage("이미지가 성공적으로 삭제되었습니다.");
      } catch (err) {
        setError(
          `이미지 삭제 중 오류가 발생했습니다: ${
            err.message || "알 수 없는 오류"
          }`
        );
      } finally {
        setIsDeleting(false);
      }
    }
  };

  return (
    <Box sx={{ mt: 2, mb: 2 }}>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>
          {error}
        </Alert>
      )}

      {successMessage && (
        <Alert
          severity="success"
          sx={{ mb: 2 }}
          onClose={() => setSuccessMessage("")}
        >
          {successMessage}
        </Alert>
      )}

      {/* 현재 프로필 이미지 표시 */}
      {initialImage && (
        <Box
          sx={{
            mb: 2,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
          }}
        >
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            현재 프로필 이미지
          </Typography>
          <Box sx={{ position: "relative", display: "inline-block" }}>
            <img
              src={initialImage}
              alt="프로필"
              style={{
                width: "150px",
                height: "150px",
                borderRadius: "50%",
                objectFit: "cover",
              }}
            />
            <IconButton
              color="error"
              sx={{
                position: "absolute",
                bottom: 0,
                right: 0,
                bgcolor: "white",
              }}
              onClick={handleDelete}
              disabled={isDeleting}
            >
              {isDeleting ? <CircularProgress size={24} /> : <DeleteIcon />}
            </IconButton>
          </Box>
        </Box>
      )}

      {/* 새 이미지 업로드 UI */}
      <Box
        sx={{
          border: "2px dashed #ccc",
          borderRadius: 2,
          p: 3,
          textAlign: "center",
          mb: 2,
        }}
      >
        <input
          accept="image/*"
          id="profile-image-upload"
          type="file"
          style={{ display: "none" }}
          onChange={handleFileChange}
          disabled={isUploading}
        />
        <label htmlFor="profile-image-upload">
          <Button
            variant="outlined"
            component="span"
            startIcon={<CloudUploadIcon />}
            disabled={isUploading}
          >
            이미지 선택
          </Button>
        </label>

        {file && (
          <Typography variant="body2" sx={{ mt: 1 }}>
            선택된 파일: {file.name}
          </Typography>
        )}

        {preview && (
          <Box sx={{ mt: 2, mb: 2 }}>
            <img
              src={preview}
              alt="미리보기"
              style={{
                maxWidth: "100%",
                maxHeight: "200px",
                borderRadius: "8px",
              }}
            />
          </Box>
        )}

        {file && (
          <Button
            variant="contained"
            color="primary"
            onClick={handleUpload}
            disabled={isUploading}
            sx={{ mt: 2 }}
          >
            {isUploading ? (
              <>
                <CircularProgress size={24} sx={{ mr: 1, color: "white" }} />
                업로드 중... {uploadProgress}%
              </>
            ) : (
              "업로드"
            )}
          </Button>
        )}
      </Box>
    </Box>
  );
};

export default ProfileImageUploader;
