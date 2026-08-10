package com.example.paceleague.board.application.port.out;

import com.example.paceleague.board.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepositoryPort {
    Post save(Post post);

    Optional<Post> findById(Long sno);

    boolean existsById(Long sno);

    Page<Post> findByBoardSno(Long boardSno, Pageable pageable);

    Optional<Post> findBySnoAndMemberSno(Long sno, Long memberSno);

    // @Lock(PESSIMISTIC_WRITE) — 어댑터 구현에서 그대로 보존
    Optional<Post> findBySnoForUpdate(Long sno);

    void incrementViewCount(Long sno);

    void delete(Post post);
}
