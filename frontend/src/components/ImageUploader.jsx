import { useState } from "react";
import { uploadItemImage } from "../services/uploadItemImage";
import { deleteItemImage } from "../services/deleteItemImage";
import {
  Box,
  Button,
  CircularProgress,
  Typography,
  Paper,
  Snackbar,
  Alert,
  IconButton,
} from "@mui/material";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import SettingsIcon from "@mui/icons-material/Settings";
import DeleteIcon from "@mui/icons-material/Delete";
import axiosInstance from "../utils/axios";

/**
 * 이미지 업로드 컴포넌트
 *
 * @param {Object} props
 * @param {Function} props.onImageUploaded - 이미지 업로드 완료 시 호출되는 콜백 함수
 * @param {string} props.initialImage - 초기 이미지 URL (있는 경우)
 * @returns {JSX.Element}
 */
const ImageUploader = ({ onImageUploaded, initialImage = "" }) => {
  const [imageUrl, setImageUrl] = useState(initialImage);
  const [loading, setLoading] = useState(false);
  const [corsLoading, setCorsLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [error, setError] = useState("");
  const [notification, setNotification] = useState({
    open: false,
    message: "",
    severity: "info",
  });

  /**
   * 파일 선택 핸들러
   * @param {Event} event
   */
  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // 이미지 파일 타입 체크
    if (!file.type.startsWith("image/")) {
      setError("이미지 파일만 업로드 가능합니다.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      // 서비스를 통해 이미지 업로드
      const uploadedImageUrl = await uploadItemImage(file);

      // 상태 업데이트 및 콜백 호출
      setImageUrl(uploadedImageUrl);
      if (onImageUploaded) {
        onImageUploaded(uploadedImageUrl);
      }
    } catch (err) {
      setError(err.message || "이미지 업로드에 실패했습니다.");
      console.error("이미지 업로드 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 이미지 삭제 핸들러
   */
  const handleDeleteImage = async () => {
    if (!imageUrl) return;

    if (!window.confirm("이미지를 삭제하시겠습니까?")) {
      return;
    }

    try {
      setDeleteLoading(true);
      setError("");

      // 서비스를 통해 이미지 삭제
      await deleteItemImage(imageUrl);

      // 상태 업데이트 및 콜백 호출
      setImageUrl("");
      if (onImageUploaded) {
        onImageUploaded("");
      }

      setNotification({
        open: true,
        message: "이미지가 성공적으로 삭제되었습니다.",
        severity: "success",
      });
    } catch (err) {
      setError(err.message || "이미지 삭제에 실패했습니다.");
      console.error("이미지 삭제 실패:", err);
    } finally {
      setDeleteLoading(false);
    }
  };

  /**
   * CORS 설정 적용 핸들러
   */
  const setupCors = async () => {
    try {
      setCorsLoading(true);
      const response = await axiosInstance.post("/api/s3/setup-cors");

      if (response.data.status === "success") {
        setNotification({
          open: true,
          message:
            "S3 CORS 설정이 적용되었습니다. 이제 이미지 업로드를 다시 시도해보세요.",
          severity: "success",
        });
      } else {
        throw new Error(response.data.message || "CORS 설정에 실패했습니다.");
      }
    } catch (error) {
      console.error("CORS 설정 오류:", error);
      setNotification({
        open: true,
        message: "CORS 설정 중 오류가 발생했습니다. 관리자에게 문의하세요.",
        severity: "error",
      });
    } finally {
      setCorsLoading(false);
    }
  };

  const handleCloseNotification = () => {
    setNotification({ ...notification, open: false });
  };

  return (
    <Box sx={{ width: "100%", my: 2 }}>
      <input
        accept="image/*"
        id="image-upload-input"
        type="file"
        style={{ display: "none" }}
        onChange={handleFileChange}
        disabled={loading || deleteLoading}
      />

      <Box sx={{ display: "flex", gap: 1, mb: 2 }}>
        <label htmlFor="image-upload-input" style={{ flexGrow: 1 }}>
          <Button
            variant="outlined"
            component="span"
            startIcon={<CloudUploadIcon />}
            disabled={loading || deleteLoading}
            fullWidth
          >
            {loading
              ? "업로드 중..."
              : imageUrl
              ? "이미지 변경"
              : "이미지 선택"}
          </Button>
        </label>

        {imageUrl && (
          <Button
            variant="outlined"
            color="error"
            size="small"
            startIcon={
              deleteLoading ? <CircularProgress size={16} /> : <DeleteIcon />
            }
            onClick={handleDeleteImage}
            disabled={deleteLoading || loading}
            sx={{ minWidth: 100 }}
          >
            삭제
          </Button>
        )}

        <Button
          variant="outlined"
          color="secondary"
          size="small"
          startIcon={<SettingsIcon />}
          onClick={setupCors}
          disabled={corsLoading}
          sx={{ minWidth: "auto", px: 1 }}
          title="이미지 업로드 CORS 설정"
        >
          {corsLoading ? <CircularProgress size={16} /> : "CORS 설정"}
        </Button>
      </Box>

      {loading && (
        <Box sx={{ display: "flex", justifyContent: "center", my: 2 }}>
          <CircularProgress size={24} />
        </Box>
      )}

      {error && (
        <Box sx={{ mt: 1, mb: 2 }}>
          <Typography color="error" variant="body2">
            {error}
          </Typography>
          {error.includes("CORS") && (
            <Button
              variant="text"
              color="primary"
              size="small"
              onClick={setupCors}
              disabled={corsLoading}
              sx={{ mt: 1 }}
            >
              {corsLoading ? "CORS 설정 적용 중..." : "CORS 설정 적용하기"}
            </Button>
          )}
        </Box>
      )}

      {imageUrl && (
        <Paper
          elevation={2}
          sx={{
            mt: 2,
            p: 1,
            borderRadius: 1,
            textAlign: "center",
            position: "relative",
          }}
        >
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            이미지 미리보기
          </Typography>
          <Box
            component="img"
            src={imageUrl}
            alt="업로드된 이미지"
            sx={{
              maxWidth: "100%",
              maxHeight: "300px",
              objectFit: "contain",
            }}
            onError={(e) => {
              e.target.onerror = null;
              e.target.src =
                "https://via.placeholder.com/150?text=이미지+로드+실패";
            }}
          />
        </Paper>
      )}

      <Snackbar
        open={notification.open}
        autoHideDuration={6000}
        onClose={handleCloseNotification}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert
          onClose={handleCloseNotification}
          severity={notification.severity}
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ImageUploader;
