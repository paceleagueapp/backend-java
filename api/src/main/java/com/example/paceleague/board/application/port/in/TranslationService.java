package com.example.paceleague.board.application.port.in;

import com.example.paceleague.board.application.dto.CommentTranslationResponse;
import com.example.paceleague.board.application.dto.PostTranslationResponse;

public interface TranslationService {
    PostTranslationResponse translatePost(Long memberSno, Long postSno, String targetLanguage);

    CommentTranslationResponse translateComment(Long memberSno, Long commentSno, String targetLanguage);
}
