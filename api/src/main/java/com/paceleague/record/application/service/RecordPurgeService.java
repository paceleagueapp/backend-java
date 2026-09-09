package com.paceleague.record.application.service;

import com.paceleague.record.application.port.in.shared.PurgeMemberRecordsPort;
import com.paceleague.record.application.port.out.RecordRepositoryPort;
import com.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordPurgeService implements PurgeMemberRecordsPort {

    private final RecordRepositoryPort recordRepositoryPort;
    private final RecordTrackRepositoryPort recordTrackRepositoryPort;

    @Override
    @Transactional
    public void purge(Long memberSno) {
        recordTrackRepositoryPort.deleteAllByUno(memberSno);
        recordRepositoryPort.deleteAllByUno(memberSno);
    }
}
