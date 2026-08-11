package com.example.paceleague.media.application.port.in;

import com.example.paceleague.media.application.dto.MediaAttachmentResponse;

import java.util.List;

// board 도메인이 게시글에 첨부된 미디어를 조회하기 위한 크로스 도메인 포트.
// record.GetRecordSummaryPort와 동일한 패턴 — 소유권 검사 없이 postSno만으로 조회(공개 게시글은 누구나 볼 수 있으므로).
public interface GetPostAttachmentsPort {
    List<MediaAttachmentResponse> getByPostSno(Long postSno);

    long countByPostSno(Long postSno);
}
