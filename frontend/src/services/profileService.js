import axios from "axios";
import axiosInstance from "../utils/axios";

/**
 * 프로필 이미지 업로드를 위한 Presigned URL을 요청합니다.
 *
 * @param {string} filename 업로드할 파일 이름
 * @returns {Promise<Object>} Presigned URL 및 파일 URL 정보
 */
export const getProfileImagePresignedUrl = async (filename) => {
  try {
    const token = localStorage.getItem("access_token");
    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    // axiosInstance를 사용하여 토큰이 자동으로 포함되도록 함
    const response = await axiosInstance.post(
      "/api/users/profile/image/presigned",
      { filename }
    );

    return response.data;
  } catch (error) {
    console.error("Presigned URL 요청 실패:", error);

    // 서버에서 자세한 오류 정보가 있는 경우
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }

    if (error.response && error.response.status === 401) {
      localStorage.removeItem("access_token");
      throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
    }

    if (error.response && error.response.status === 500) {
      throw new Error(
        "서버 내부 오류: 프로필 이미지 업로드 서비스가 준비되지 않았습니다."
      );
    }

    throw error;
  }
};

/**
 * S3 버킷에 직접 파일을 업로드합니다.
 *
 * @param {string} presignedUrl Presigned URL
 * @param {File} file 업로드할 파일
 * @returns {Promise<void>}
 */
export const uploadFileToS3 = async (presignedUrl, file) => {
  try {
    // 컨텐츠 타입 결정
    const contentType = file.type || getContentTypeFromFileName(file.name);

    // Presigned URL에 파일 업로드 - 여기서는 일반 axios 사용 (인증 토큰 불필요)
    await axios.put(presignedUrl, file, {
      headers: {
        "Content-Type": contentType,
      },
      withCredentials: false, // CORS 문제 방지
    });
  } catch (error) {
    console.error("S3 업로드 실패:", error);
    throw new Error("이미지를 S3에 업로드하는 중 오류가 발생했습니다.");
  }
};

/**
 * 파일명에서 컨텐츠 타입을 추출합니다.
 *
 * @param {string} filename 파일명
 * @returns {string} 컨텐츠 타입
 */
const getContentTypeFromFileName = (filename) => {
  const extension = filename.toLowerCase().split(".").pop();

  switch (extension) {
    case "jpg":
    case "jpeg":
      return "image/jpeg";
    case "png":
      return "image/png";
    case "gif":
      return "image/gif";
    case "bmp":
      return "image/bmp";
    case "webp":
      return "image/webp";
    case "svg":
      return "image/svg+xml";
    default:
      return "application/octet-stream";
  }
};

/**
 * 프로필 이미지를 업로드합니다.
 *
 * @param {File} file 업로드할 이미지 파일
 * @param {Function} onProgress 업로드 진행상태 콜백 함수
 * @returns {Promise<string>} 업로드된 이미지의 URL
 */
export const uploadProfileImage = async (file, onProgress) => {
  try {
    // 1. 서버에서 Presigned URL 요청
    const presignedUrlResponse = await axiosInstance.post(
      "/api/users/profile/image/presigned",
      {
        filename: file.name,
      }
    );

    // 2. 응답에서 Presigned URL과 파일 URL 추출
    const { presignedUrl, fileUrl } = presignedUrlResponse.data;

    // 3. Presigned URL을 사용하여 S3에 직접 업로드
    // 참고: S3 업로드는 인증 토큰이 필요 없으므로 기본 fetch API 사용
    const xhr = new XMLHttpRequest();

    // 프로그래스 이벤트 설정
    if (onProgress) {
      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable) {
          const percentComplete = Math.round(
            (event.loaded / event.total) * 100
          );
          onProgress(percentComplete);
        }
      };
    }

    // Promise로 XHR 요청 래핑
    await new Promise((resolve, reject) => {
      xhr.open("PUT", presignedUrl, true);
      xhr.setRequestHeader("Content-Type", file.type);

      xhr.onload = () => {
        if (xhr.status === 200) {
          resolve();
        } else {
          reject(new Error(`S3 업로드 실패: ${xhr.status} ${xhr.statusText}`));
        }
      };

      xhr.onerror = () => {
        reject(new Error("네트워크 오류로 업로드에 실패했습니다."));
      };

      xhr.send(file);
    });

    // 4. 프로필 정보 업데이트 (이미지 URL 저장)
    await axiosInstance.put("/api/users/profile", {
      profileImageUrl: fileUrl,
    });

    return fileUrl;
  } catch (error) {
    console.error("이미지 업로드 중 오류 발생:", error);

    // 오류 메시지 처리
    let errorMessage = "이미지 업로드에 실패했습니다.";

    if (error.response) {
      // 서버 응답이 있는 경우
      errorMessage =
        error.response.data?.message || `서버 오류: ${error.response.status}`;
    } else if (error.request) {
      // 요청은 보냈지만 응답이 없는 경우
      errorMessage = "서버 응답이 없습니다.";
    } else {
      // 요청 설정 중 오류
      errorMessage = error.message;
    }

    throw new Error(errorMessage);
  }
};

/**
 * 프로필 이미지 URL로 사용자 프로필을 업데이트합니다.
 *
 * @param {string} profileImageUrl 프로필 이미지 URL
 * @returns {Promise<Object>} 업데이트된 사용자 프로필
 */
export const updateProfileWithImage = async (profileImageUrl) => {
  try {
    const token = localStorage.getItem("access_token");
    if (!token) {
      throw new Error("인증 토큰이 없습니다. 다시 로그인해주세요.");
    }

    // axiosInstance를 사용하여 토큰이 자동으로 포함되도록 함
    const response = await axiosInstance.put("/api/users/profile", {
      profileImageUrl,
    });

    return response.data;
  } catch (error) {
    console.error("프로필 업데이트 실패:", error);

    if (error.response && error.response.status === 401) {
      localStorage.removeItem("access_token");
      throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
    }

    throw error;
  }
};

/**
 * 프로필 이미지 삭제 함수
 *
 * @param {string} imageUrl - 삭제할 이미지 URL
 * @returns {Promise<void>}
 */
export const deleteProfileImage = async (imageUrl) => {
  try {
    // 이미지 삭제 요청
    await axiosInstance.delete("/api/users/profile/image", {
      data: { imageUrl },
    });
  } catch (error) {
    console.error("이미지 삭제 중 오류 발생:", error);

    // 오류 메시지 처리
    let errorMessage = "이미지 삭제에 실패했습니다.";

    if (error.response) {
      // 서버 응답이 있는 경우
      errorMessage =
        error.response.data?.message || `서버 오류: ${error.response.status}`;
    } else if (error.request) {
      // 요청은 보냈지만 응답이 없는 경우
      errorMessage = "서버 응답이 없습니다.";
    } else {
      // 요청 설정 중 오류
      errorMessage = error.message;
    }

    throw new Error(errorMessage);
  }
};
