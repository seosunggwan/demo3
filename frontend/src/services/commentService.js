import axiosInstance from "../utils/axios";

/**
 * 게시글 ID에 해당하는 댓글 목록을 조회합니다.
 *
 * @param {number} boardId - 게시글 ID
 * @returns {Promise<Array>} 댓글 목록
 */
export const getCommentsByBoardId = async (boardId) => {
  try {
    const response = await axiosInstance.get(`/api/boards/${boardId}/comments`);
    return response.data;
  } catch (error) {
    console.error("댓글 목록 조회 실패:", error);
    throw new Error(
      error.response?.data?.message || "댓글 목록을 불러오는데 실패했습니다."
    );
  }
};

/**
 * 특정 댓글을 조회합니다.
 *
 * @param {number} boardId - 게시글 ID
 * @param {number} commentId - 댓글 ID
 * @returns {Promise<Object>} 댓글 정보
 */
export const getCommentById = async (boardId, commentId) => {
  try {
    const response = await axiosInstance.get(
      `/api/boards/${boardId}/comments/${commentId}`
    );
    return response.data;
  } catch (error) {
    console.error("댓글 조회 실패:", error);
    throw new Error(
      error.response?.data?.message || "댓글을 불러오는데 실패했습니다."
    );
  }
};

/**
 * 댓글을 생성합니다.
 *
 * @param {number} boardId - 게시글 ID
 * @param {Object} commentData - 댓글 데이터 (content, parentId)
 * @returns {Promise<Object>} 생성된 댓글 정보
 */
export const createComment = async (boardId, commentData) => {
  try {
    const response = await axiosInstance.post(
      `/api/boards/${boardId}/comments`,
      commentData
    );
    return response.data;
  } catch (error) {
    console.error("댓글 생성 실패:", error);
    throw new Error(
      error.response?.data?.message || "댓글 작성에 실패했습니다."
    );
  }
};

/**
 * 댓글을 수정합니다.
 *
 * @param {number} boardId - 게시글 ID
 * @param {number} commentId - 댓글 ID
 * @param {Object} commentData - 수정할 댓글 데이터 (content)
 * @returns {Promise<Object>} 수정된 댓글 정보
 */
export const updateComment = async (boardId, commentId, commentData) => {
  try {
    const response = await axiosInstance.put(
      `/api/boards/${boardId}/comments/${commentId}`,
      commentData
    );
    return response.data;
  } catch (error) {
    console.error("댓글 수정 실패:", error);
    throw new Error(
      error.response?.data?.message || "댓글 수정에 실패했습니다."
    );
  }
};

/**
 * 댓글을 삭제합니다.
 *
 * @param {number} boardId - 게시글 ID
 * @param {number} commentId - 댓글 ID
 * @returns {Promise<void>}
 */
export const deleteComment = async (boardId, commentId) => {
  try {
    await axiosInstance.delete(`/api/boards/${boardId}/comments/${commentId}`);
  } catch (error) {
    console.error("댓글 삭제 실패:", error);
    throw new Error(
      error.response?.data?.message || "댓글 삭제에 실패했습니다."
    );
  }
};

/**
 * 대댓글을 작성합니다.
 *
 * @param {number} boardId - 게시글 ID
 * @param {number} parentId - 부모 댓글 ID
 * @param {string} content - 댓글 내용
 * @returns {Promise<Object>} 생성된 대댓글 정보
 */
export const createReply = async (boardId, parentId, content) => {
  try {
    const response = await axiosInstance.post(
      `/api/boards/${boardId}/comments`,
      {
        content,
        parentId,
      }
    );
    return response.data;
  } catch (error) {
    console.error("대댓글 생성 실패:", error);
    throw new Error(
      error.response?.data?.message || "대댓글 작성에 실패했습니다."
    );
  }
};
