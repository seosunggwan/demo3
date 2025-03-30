import { createContext, useContext, useState } from "react";

const AuthContext = createContext();

export default function AuthProvider({ children }) {
  // 로컬 스토리지 값으로 초기 로그인 상태 설정
  const [isLoggedIn, setIsLoggedIn] = useState(
    !!localStorage.getItem("access_token")
  );
  const [loginUser, setLoginUser] = useState(localStorage.getItem("email"));

  // 로그인 함수 (localStorage에 저장 후 상태 업데이트)
  const login = (token, email, role, refreshToken = null) => {
    localStorage.setItem("access_token", token);
    localStorage.setItem("email", email);
    localStorage.setItem("role", role);

    // refresh_token이 제공된 경우 저장
    if (refreshToken) {
      localStorage.setItem("refresh_token", refreshToken);
      console.log("Refresh 토큰 저장됨:", refreshToken);
    }

    setIsLoggedIn(true);
    setLoginUser(email);
  };

  // 로그아웃 함수 (localStorage에서 제거 후 상태 초기화)
  const logout = () => {
    localStorage.removeItem("access_token");
    localStorage.removeItem("email");
    localStorage.removeItem("role");
    localStorage.removeItem("refresh_token");
    setIsLoggedIn(false);
    setLoginUser(null);
  };

  // 액세스 토큰을 가져오는 함수
  const getAccessToken = () => {
    return localStorage.getItem("access_token");
  };

  // 리프레시 토큰을 가져오는 함수
  const getRefreshToken = () => {
    return localStorage.getItem("refresh_token");
  };

  return (
    <AuthContext.Provider
      value={{
        isLoggedIn,
        setIsLoggedIn,
        loginUser,
        setLoginUser,
        login,
        logout,
        getAccessToken,
        getRefreshToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// 사용하기 편한 커스텀 훅
export const useLogin = () => useContext(AuthContext);
