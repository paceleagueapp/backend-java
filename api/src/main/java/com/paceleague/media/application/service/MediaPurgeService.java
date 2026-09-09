package com.paceleague.media.application.service;

import com.paceleague.media.application.port.in.shared.PurgeMemberMediaPort;
import com.paceleague.media.application.port.out.MediaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaPurgeService implements PurgeMemberMediaPort {

    private final MediaRepositoryPort mediaRepositoryPort;

    @Override
    @Transactional
    public void purge(Long memberSno) {
        // S3 객체 삭제는 후순위(버킷 라이프사이클로 정리 가능). 여기서는 행만 제거.
        mediaRepositoryPort.deleteAllByMemberSno(memberSno);
    }
}
