/**
 * ISO 8601 형식의 날짜 문자열을 'YYYY-MM-DD' 형식으로 변환합니다.
 * @param {string} dateString - ISO 8601 형식의 날짜 문자열 (e.g., "2023-04-01T12:34:56")
 * @returns {string} 'YYYY-MM-DD' 형식의 날짜 문자열
 */
export const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);

  // 유효하지 않은 날짜인 경우
  if (isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

/**
 * ISO 8601 형식의 날짜 문자열을 'YYYY-MM-DD HH:MM:SS' 형식으로 변환합니다.
 * @param {string} dateString - ISO 8601 형식의 날짜 문자열 (e.g., "2023-04-01T12:34:56")
 * @returns {string} 'YYYY-MM-DD HH:MM:SS' 형식의 날짜 문자열
 */
export const formatDateTime = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);

  // 유효하지 않은 날짜인 경우
  if (isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

/**
 * ISO 8601 형식의 날짜 문자열을 'YYYY년 MM월 DD일' 형식으로 변환합니다.
 * @param {string} dateString - ISO 8601 형식의 날짜 문자열 (e.g., "2023-04-01T12:34:56")
 * @returns {string} 'YYYY년 MM월 DD일' 형식의 날짜 문자열
 */
export const formatDateKorean = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);

  // 유효하지 않은 날짜인 경우
  if (isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  return `${year}년 ${month}월 ${day}일`;
};
