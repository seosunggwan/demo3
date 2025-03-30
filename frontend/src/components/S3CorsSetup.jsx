import { useState } from "react";
import { Button, CircularProgress, Snackbar, Alert } from "@mui/material";
import axiosInstance from "../utils/axios";

/**
 * S3 CORS 설정을 수동으로 적용하는 버튼 컴포넌트
 */
const S3CorsSetup = () => {
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState({
    open: false,
    message: "",
    severity: "info",
  });

  const setupCors = async () => {
    try {
      setLoading(true);
      const response = await axiosInstance.post("/api/s3/setup-cors");

      if (response.data.status === "success") {
        setNotification({
          open: true,
          message: "S3 CORS 설정이 성공적으로 적용되었습니다.",
          severity: "success",
        });
      } else {
        throw new Error(
          response.data.message || "알 수 없는 오류가 발생했습니다."
        );
      }
    } catch (error) {
      console.error("CORS 설정 오류:", error);
      setNotification({
        open: true,
        message: error.message || "CORS 설정 중 오류가 발생했습니다.",
        severity: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCloseNotification = () => {
    setNotification({ ...notification, open: false });
  };

  return (
    <>
      <Button
        variant="outlined"
        color="primary"
        onClick={setupCors}
        disabled={loading}
        sx={{ mt: 2 }}
      >
        {loading ? (
          <>
            <CircularProgress size={16} sx={{ mr: 1 }} />
            S3 CORS 설정 적용 중...
          </>
        ) : (
          "S3 CORS 설정 적용"
        )}
      </Button>

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
    </>
  );
};

export default S3CorsSetup;
