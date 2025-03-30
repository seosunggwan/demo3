import axiosInstance from "../utils/axios";

/**
 * Presigned URL을 사용하여 S3에 이미지 직접 업로드
 * @param {File} file - 업로드할 이미지 파일
 * @returns {Promise<string>} 업로드된 이미지의 URL
 */
export const uploadItemImage = async (file) => {
  try {
    console.log("이미지 업로드 시작:", file.name, file.size);

    // 파일 크기 체크 (10MB)
    if (file.size > 10 * 1024 * 1024) {
      throw new Error("이미지 크기는 10MB를 초과할 수 없습니다.");
    }

    // 인증 토큰 확인
    const token = localStorage.getItem("access_token");
    if (!token) {
      throw new Error("로그인이 필요합니다");
    }

    // 1. CORS 설정 요청 (필요한 경우 호출)
    // const corsSetupResponse = await axiosInstance.post("/api/s3/setup-cors");
    // console.log("CORS 설정 응답:", corsSetupResponse.data);

    // 2. 백엔드에서 Presigned URL 요청
    console.log("Presigned URL 요청 중...");
    const presignedResponse = await axiosInstance.post(
      "/api/items/image/presigned",
      {
        filename: file.name,
      }
    );

    if (!presignedResponse.data || !presignedResponse.data.presignedUrl) {
      throw new Error("Presigned URL을 받아오지 못했습니다.");
    }

    const { presignedUrl, fileUrl } = presignedResponse.data;
    console.log("Presigned URL 받음:", presignedUrl);
    console.log("최종 파일 URL:", fileUrl);

    // 3. S3에 직접 업로드
    console.log("S3에 직접 업로드 중...");
    const contentType = file.type || getContentTypeFromFileName(file.name);

    // 백엔드에서 제공한 URL에 Content-Type이 이미 파라미터로 포함된 경우, 헤더에서는 제외
    const hasContentTypeParam = presignedUrl.includes("Content-Type=");

    try {
      // XMLHttpRequest 사용 (CORS 우회 가능)
      console.log("XMLHttpRequest로 업로드 시도...");
      await new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();

        // 업로드 진행 이벤트
        xhr.upload.onprogress = (event) => {
          if (event.lengthComputable) {
            const percentComplete = (event.loaded / event.total) * 100;
            console.log(`업로드 진행률: ${percentComplete.toFixed(2)}%`);
          }
        };

        // 업로드 완료 시 호출
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            console.log("업로드 성공:", xhr.status);
            resolve();
          } else {
            console.error(`업로드 실패 (${xhr.status}): ${xhr.statusText}`);
            reject(new Error(`업로드 실패 (${xhr.status}): ${xhr.statusText}`));
          }
        };

        // 오류 발생 시 호출
        xhr.onerror = () => {
          console.error("XHR 업로드 중 오류 발생");
          reject(new Error("네트워크 오류로 업로드에 실패했습니다."));
        };

        // 타임아웃 설정
        xhr.timeout = 30000; // 30초
        xhr.ontimeout = () => {
          console.error("업로드 시간 초과");
          reject(new Error("업로드 시간이 초과되었습니다."));
        };

        // 요청 설정
        xhr.open("PUT", presignedUrl, true);

        // Content-Type 설정 (URL에 이미 포함되어 있지 않은 경우만)
        if (!hasContentTypeParam) {
          xhr.setRequestHeader("Content-Type", contentType);
        }

        // CORS 관련 설정
        xhr.withCredentials = false; // 크로스 도메인 쿠키 전송 안함

        try {
          // 파일 전송
          xhr.send(file);
        } catch (sendError) {
          console.error("파일 전송 중 오류:", sendError);
          reject(new Error("파일 전송에 실패했습니다: " + sendError.message));
        }
      });

      console.log("XMLHttpRequest 업로드 완료");
    } catch (uploadError) {
      console.error("S3 업로드 오류:", uploadError);

      // 서버 측 CORS 설정을 다시 적용해보기
      try {
        console.log("S3 CORS 설정 재적용 시도...");
        await axiosInstance.post("/api/s3/setup-cors");
        console.log(
          "CORS 설정 적용 완료, 이미지는 아직 업로드되지 않았습니다."
        );
        throw new Error(
          "이미지 업로드에 실패했습니다. CORS 설정을 재적용했습니다. 다시 시도해주세요."
        );
      } catch (corsError) {
        console.error("CORS 설정 적용 실패:", corsError);
        throw new Error(
          "이미지 업로드 중 오류가 발생했습니다: " + uploadError.message
        );
      }
    }

    // 업로드 성공
    console.log("이미지 업로드 성공:", fileUrl);
    return fileUrl;
  } catch (error) {
    console.error("이미지 업로드 실패:", error);

    // 오류 메시지 정리
    let errorMessage = "이미지 업로드에 실패했습니다.";

    if (error.message && error.message.includes("CORS")) {
      errorMessage =
        "이미지 서버 연결 문제가 발생했습니다. 관리자에게 문의하세요.";
    } else if (error.response?.status === 401) {
      localStorage.removeItem("access_token");
      errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
    } else if (error.message) {
      errorMessage = error.message;
    }

    throw new Error(errorMessage);
  }
};

/**
 * 파일 이름으로부터 MIME 타입을 추론합니다.
 * @param {string} fileName - 파일 이름
 * @returns {string} MIME 타입
 */
function getContentTypeFromFileName(fileName) {
  const extension = fileName.split(".").pop().toLowerCase();

  const mimeTypes = {
    jpg: "image/jpeg",
    jpeg: "image/jpeg",
    png: "image/png",
    gif: "image/gif",
    bmp: "image/bmp",
    webp: "image/webp",
  };

  return mimeTypes[extension] || "application/octet-stream";
}
