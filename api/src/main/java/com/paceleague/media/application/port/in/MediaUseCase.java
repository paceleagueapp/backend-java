package com.paceleague.media.application.port.in;

import com.paceleague.media.application.dto.MediaStatusResponse;
import com.paceleague.media.application.dto.MediaUploadInitResponse;
import com.paceleague.media.application.dto.MediaUploadRequest;

import java.util.List;

public interface MediaUseCase {
    // presigned PUT URL 발급 + PENDING 상태의 media row 생성
    MediaUploadInitResponse requestUpload(Long memberSno, MediaUploadRequest req);

    // 업로드 완료 통보 — 이미지는 즉시 모더레이션 후 결과 반환, 동영상은 비동기 잡만 시작하고 PENDING 유지
    MediaStatusResponse completeUpload(Long memberSno, Long mediaSno);

    // 동영상 모더레이션 진행 상태 폴링(호출 시점에 아직 처리 중이면 Rekognition에 재조회 후 갱신)
    MediaStatusResponse getUploadStatus(Long memberSno, Long mediaSno);

    // 링크는 업로드/모더레이션 없이 즉시 APPROVED로 생성
    Long createLink(Long memberSno, String url);

    // 게시글 작성 시 첨부 확정 — 본인 소유 + APPROVED + 미첨부 상태만 허용
    void attachToPost(Long memberSno, List<Long> mediaSnos, Long postSno);
}
