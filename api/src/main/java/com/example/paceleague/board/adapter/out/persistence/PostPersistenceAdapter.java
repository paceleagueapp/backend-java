package com.example.paceleague.board.adapter.out.persistence;

import com.example.paceleague.board.application.port.out.PostRepositoryPort;
import com.example.paceleague.board.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final PostJpaRepository postJpaRepository;

    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    public Optional<Post> findById(Long sno) {
        return postJpaRepository.findById(sno);
    }

    public boolean existsById(Long sno) {
        return postJpaRepository.existsById(sno);
    }

    public Page<Post> findByBoardSno(Long boardSno, Pageable pageable) {
        return postJpaRepository.findByBoardSno(boardSno, pageable);
    }

    public Optional<Post> findBySnoAndMemberSno(Long sno, Long memberSno) {
        return postJpaRepository.findBySnoAndMemberSno(sno, memberSno);
    }

    public Optional<Post> findBySnoForUpdate(Long sno) {
        return postJpaRepository.findBySnoForUpdate(sno);
    }

    public void incrementViewCount(Long sno) {
        postJpaRepository.incrementViewCount(sno);
    }

    public void delete(Post post) {
        postJpaRepository.delete(post);
    }
}
