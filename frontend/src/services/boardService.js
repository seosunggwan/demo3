import axiosInstance from "../utils/axios";
import { redirectToLogin, decodeToken } from "../utils/auth";
import fetchReissue from "./fetchReissue";

/**
 * 토큰 유효성 및 만료 확인 함수
 * @returns {boolean} 토큰 유효 여부
 */
const checkTokenValidity = async () => {
  const access_token = localStorage.getItem("access_token");

  if (!access_token) {
    console.error("🚨 토큰이 없습니다. 로그인 후 이용해주세요.");
    return false;
  }

  // 토큰 만료 확인 - 사전 체크
  try {
    const tokenData = decodeToken(access_token);
    if (tokenData) {
      console.log(
        "🔍 토큰 만료 시간:",
        new Date(tokenData.exp * 1000).toLocaleString()
      );

      // 토큰 만료 10초 전에 미리 갱신
      const now = new Date().getTime();
      const expTime = tokenData.exp * 1000;
      const timeToExpire = expTime - now;

      if (timeToExpire < 10000 && timeToExpire > 0) {
        // 10초 이내로 만료 예정
        console.log("⚠️ 토큰이 곧 만료됩니다. 갱신 시도 중...");
        const refreshed = await fetchReissue();
        return refreshed;
      } else if (timeToExpire <= 0) {
        // 이미 만료됨
        console.log("⚠️ 토큰이 이미 만료되었습니다. 갱신 시도 중...");
        const refreshed = await fetchReissue();
        return refreshed;
      }

      return true; // 토큰 유효
    }
  } catch (error) {
    console.error("❌ 토큰 디코딩 중 오류 발생:", error);
    return false;
  }

  return true; // 기본적으로 유효하다고 간주
};

/**
 * 페이지네이션을 적용한 게시글 목록 조회
 */
export const fetchBoardsWithPaging = async (
  page = 0,
  size = 10,
  navigate,
  location
) => {
  try {
    // 토큰 사전 검증
    const isTokenValid = await checkTokenValidity();
    if (!isTokenValid && navigate) {
      redirectToLogin(
        navigate,
        location?.pathname || "/boards",
        "인증이 만료되었습니다. 다시 로그인해주세요."
      );
      return null;
    }

    const response = await axiosInstance.get(
      `/api/boards/page?page=${page}&size=${size}`
    );
    console.log("Board list response:", response.data);
    return response.data;
  } catch (error) {
    console.error("게시글 목록 조회 중 오류 발생:", error);

    // 인증 오류 처리
    if (error.response?.status === 401 && navigate) {
      redirectToLogin(
        navigate,
        location?.pathname || "/boards",
        "게시글 목록 조회를 위해 다시 로그인해주세요."
      );
      return null;
    }

    throw error;
  }
};

/**
 * 키워드로 게시글 검색 (제목 + 내용)
 */
export const searchBoardsByKeyword = async (keyword, page = 0, size = 10) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/search?keyword=${encodeURIComponent(
        keyword
      )}&page=${page}&size=${size}`
    );
    return response.data;
  } catch (error) {
    console.error("게시글 검색 중 오류 발생:", error);
    throw error;
  }
};

/**
 * 작성자 이름으로 게시글 검색
 */
export const searchBoardsByAuthor = async (authorName, page = 0, size = 10) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/search/author?authorName=${encodeURIComponent(
        authorName
      )}&page=${page}&size=${size}`
    );
    return response.data;
  } catch (error) {
    console.error("작성자별 게시글 검색 중 오류 발생:", error);
    throw error;
  }
};

/**
 * 게시글 상세 조회
 */
export const fetchBoardDetail = async (boardId, navigate, location) => {
  try {
    // 토큰 사전 검증
    const isTokenValid = await checkTokenValidity();
    if (!isTokenValid && navigate) {
      redirectToLogin(
        navigate,
        location?.pathname || `/boards/${boardId}`,
        "인증이 만료되었습니다. 다시 로그인해주세요."
      );
      return null;
    }

    const response = await axiosInstance.get(`/api/boards/${boardId}`);
    return response.data;
  } catch (error) {
    console.error(`게시글 상세 조회 중 오류 발생 (ID: ${boardId}):`, error);

    // 인증 오류가 해결되지 않은 경우
    if (error.response?.status === 401 && navigate) {
      console.log("인증 오류 발생, redirectToLogin 호출");
      const returnPath = location?.pathname || `/boards/${boardId}`;
      redirectToLogin(
        navigate,
        returnPath,
        "게시글 조회를 위해 다시 로그인해주세요."
      );
      return null; // 오류 발생 시 null 반환
    }

    throw error;
  }
};

/**
 * 게시글 생성
 */
export const createBoard = async (boardData, navigate, location) => {
  try {
    // 토큰 사전 검증
    const isTokenValid = await checkTokenValidity();
    if (!isTokenValid && navigate) {
      redirectToLogin(
        navigate,
        location?.pathname || "/boards/new",
        "인증이 만료되었습니다. 다시 로그인해주세요."
      );
      return null;
    }

    const response = await axiosInstance.post("/api/boards", boardData);
    return response.data;
  } catch (error) {
    console.error("게시글 생성 중 오류 발생:", error);

    // 인증 오류가 해결되지 않은 경우
    if (error.response?.status === 401 && navigate) {
      console.log("인증 오류 발생, redirectToLogin 호출");
      // location 객체가 없으면 기본 경로 사용
      const returnPath = location?.pathname || "/boards/new";
      redirectToLogin(
        navigate,
        returnPath,
        "게시글 작성을 위해 다시 로그인해주세요."
      );
      return null; // 오류 발생 시 null 반환
    }

    throw error;
  }
};

/**
 * 게시글 수정
 */
export const updateBoard = async (boardId, boardData, navigate, location) => {
  try {
    // 토큰 사전 검증
    const isTokenValid = await checkTokenValidity();
    if (!isTokenValid && navigate) {
      redirectToLogin(
        navigate,
        location?.pathname || `/boards/${boardId}/edit`,
        "인증이 만료되었습니다. 다시 로그인해주세요."
      );
      return null;
    }

    const response = await axiosInstance.put(
      `/api/boards/${boardId}`,
      boardData
    );
    return response.data;
  } catch (error) {
    console.error(`게시글 수정 중 오류 발생 (ID: ${boardId}):`, error);

    // 인증 오류가 해결되지 않은 경우
    if (error.response?.status === 401 && navigate) {
      console.log("인증 오류 발생, redirectToLogin 호출");
      const returnPath = location?.pathname || `/boards/${boardId}/edit`;
      redirectToLogin(
        navigate,
        returnPath,
        "게시글 수정을 위해 다시 로그인해주세요."
      );
      return null; // 오류 발생 시 null 반환
    }

    throw error;
  }
};

/**
 * 게시글 삭제
 */
export const deleteBoard = async (boardId, navigate, location) => {
  try {
    // 토큰 사전 검증
    const isTokenValid = await checkTokenValidity();
    if (!isTokenValid && navigate) {
      redirectToLogin(
        navigate,
        location?.pathname || `/boards/${boardId}`,
        "인증이 만료되었습니다. 다시 로그인해주세요."
      );
      return null;
    }

    const response = await axiosInstance.delete(`/api/boards/${boardId}`);
    return response.status === 204; // 삭제 성공 여부 반환
  } catch (error) {
    console.error(`게시글 삭제 중 오류 발생 (ID: ${boardId}):`, error);

    // 인증 오류 처리
    if (error.response?.status === 401 && navigate) {
      const returnPath = location?.pathname || `/boards/${boardId}`;
      redirectToLogin(
        navigate,
        returnPath,
        "게시글 삭제를 위해 다시 로그인해주세요."
      );
      return null;
    }

    throw error;
  }
};

/**
 * 게시글 좋아요 토글 (좋아요 추가 또는 삭제)
 * @param {number} boardId - 게시글 ID
 * @returns {Promise<{liked: boolean, likeCount: number}>} - 좋아요 상태와 개수
 */
export const toggleBoardLike = async (boardId) => {
  try {
    const response = await axiosInstance.post(`/api/boards/${boardId}/likes`);
    return response.data;
  } catch (error) {
    console.error("게시글 좋아요 토글 실패:", error);
    throw error;
  }
};

/**
 * 게시글 좋아요 상태 조회
 * @param {number} boardId - 게시글 ID
 * @returns {Promise<{liked: boolean, likeCount: number}>} - 좋아요 상태와 개수
 */
export const getBoardLikeStatus = async (boardId) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/${boardId}/likes/status`
    );
    return response.data;
  } catch (error) {
    console.error("게시글 좋아요 상태 조회 실패:", error);
    throw error;
  }
};

/**
 * 게시글 좋아요 개수 조회
 * @param {number} boardId - 게시글 ID
 * @returns {Promise<{likeCount: number}>} - 좋아요 개수
 */
export const getBoardLikeCount = async (boardId) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/${boardId}/likes/count`
    );
    return response.data;
  } catch (error) {
    console.error("게시글 좋아요 개수 조회 실패:", error);
    throw error;
  }
};

/**
 * 오늘의 인기글 목록 조회
 * @returns {Promise<{date: string, boards: Array}>} - 인기글 목록
 */
export const fetchTodayPopularBoards = async () => {
  try {
    const response = await axiosInstance.get("/api/boards/popular/today");
    return response.data;
  } catch (error) {
    console.error("오늘의 인기글 목록 조회 실패:", error);
    throw error;
  }
};

/**
 * 특정 날짜의 인기글 목록 조회
 * @param {string} date - 날짜 (YYYY-MM-DD 형식)
 * @returns {Promise<{date: string, boards: Array}>} - 인기글 목록
 */
export const fetchPopularBoardsByDate = async (date) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/popular/date/${date}`
    );
    return response.data;
  } catch (error) {
    console.error("특정 날짜의 인기글 목록 조회 실패:", error);
    throw error;
  }
};

/**
 * 최근 N일간의 인기글 목록 조회
 * @param {number} days - 일수 (기본값: 7)
 * @returns {Promise<{days: number, startDate: string, endDate: string, boards: Array}>} - 인기글 목록
 */
export const fetchRecentPopularBoards = async (days = 7) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/popular/recent?days=${days}`
    );
    return response.data;
  } catch (error) {
    console.error("최근 인기글 목록 조회 실패:", error);
    throw error;
  }
};
