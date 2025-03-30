import { createTheme } from "@mui/material/styles";

/**
 * 📌 MUI 테마 설정
 * - 기존 Vuetify 스타일을 MUI에 맞게 변경
 */
const theme = createTheme({
  palette: {
    primary: {
      main: "#1976d2", // Vuetify 기본 파란색 계열
    },
    secondary: {
      main: "#ff9800",
    },
  },
  typography: {
    fontFamily: "Roboto, Arial, sans-serif",
  },
});

export default theme;