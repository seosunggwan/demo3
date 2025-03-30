import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import fetchAuthorizedPage from "../services/fetchAuthorizedPage";
import S3CorsSetup from "../components/S3CorsSetup";
import { Box, Typography, Divider, Card, CardContent } from "@mui/material";

export default function Admin() {
  const navigate = useNavigate(); // 페이지 이동용
  const location = useLocation(); // 현재 경로
  const [data, setData] = useState(""); // 받아온 데이터 저장할 상태

  useEffect(() => {
    // 컴포넌트 마운트될 때 한번만 실행됨
    // 환경 변수를 사용하여 백엔드 URL 설정
    const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/admin`;
    fetchAuthorizedPage(apiUrl, navigate, location).then((result) =>
      setData(result)
    );
  }, [navigate, location]);

  return (
    <Box sx={{ p: 3, maxWidth: 800, mx: "auto" }}>
      <Typography variant="h4" gutterBottom>
        관리자 페이지
      </Typography>

      {data ? (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              상태 정보
            </Typography>
            <Typography>{data}</Typography>
          </CardContent>
        </Card>
      ) : (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography>로딩 중...</Typography>
          </CardContent>
        </Card>
      )}

      <Divider sx={{ my: 3 }} />

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            S3 설정
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            S3 버킷에 CORS 설정을 적용합니다. 이미지 업로드 오류가 발생할 경우
            이 버튼을 클릭하세요.
          </Typography>
          <S3CorsSetup />
        </CardContent>
      </Card>
    </Box>
  );
}
