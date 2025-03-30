import axiosInstance from "../utils/axios";

/**
 * S3에서 이미지를 삭제합니다.
 * @param {string} imageUrl - 삭제할 이미지의 URL
 * @returns {Promise<boolean>} 삭제 성공 여부
 */
export const deleteItemImage = async (imageUrl) => {
  try {
    console.log("이미지 삭제 시작:", imageUrl);

    // 인증 토큰 확인
    const token = localStorage.getItem("access_token");
    if (!token) {
      throw new Error("로그인이 필요합니다");
    }

    // 이미지 URL이 없는 경우
    if (!imageUrl) {
      console.warn("삭제할 이미지 URL이 제공되지 않았습니다.");
      return false;
    }

    // API 호출하여 이미지 삭제
    const response = await axiosInstance.delete("/api/items/image", {
      data: { imageUrl },
    });

    console.log("이미지 삭제 성공:", response.data);
    return true;
  } catch (error) {
    console.error("이미지 삭제 실패:", error);

    if (error.response?.status === 401) {
      localStorage.removeItem("access_token");
      throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
    }

    const errorMessage =
      error.response?.data?.message ||
      error.message ||
      "이미지 삭제에 실패했습니다.";
    throw new Error(errorMessage);
  }
};
