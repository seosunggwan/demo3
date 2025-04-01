import axiosInstance from "../services/axiosInstance";

// 오늘의 인기글 조회
export const getPopularBoards = async () => {
  try {
    const response = await axiosInstance.get("/api/boards/popular/today");
    return response.data;
  } catch (error) {
    console.error("Error fetching popular boards:", error);
    throw error;
  }
};

// 특정 기간의 인기글 조회
export const getPopularBoardsByDate = async (days) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/popular/recent?days=${days}`
    );
    return response.data;
  } catch (error) {
    console.error("Error fetching popular boards by date:", error);
    throw error;
  }
};

// 인기글 결산 실행
export const calculatePopularBoards = async () => {
  try {
    const response = await axiosInstance.post("/api/boards/popular/refresh");
    return response.data;
  } catch (error) {
    console.error("Error calculating popular boards:", error);
    const errorMessage =
      error.response?.data?.message ||
      error.message ||
      "인기글 결산 중 오류가 발생했습니다.";
    throw new Error(errorMessage);
  }
};
