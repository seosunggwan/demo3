import "./App.css";
import { ThemeProvider, CssBaseline } from "@mui/material";
import AuthProvider from "./contexts/AuthContext";
import NavBar from "./components/NavBar";
import MyRoutes from "./routes/MyRoutes";
import MainContent from "./components/MainContent"; // ✅ 따로 분리해서 import
import theme from "./theme";

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <div className="App">
          <NavBar />
          <MainContent />
          <MyRoutes />
        </div>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
