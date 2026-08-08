package com.example.paceleague.board.service;

import com.example.paceleague.board.dto.CommentTranslationResponse;
import com.example.paceleague.board.dto.PostTranslationResponse;

public interface TranslationService {
    PostTranslationResponse translatePost(Long postSno, String targetLanguage);

    CommentTranslationResponse translateComment(Long commentSno, String targetLanguage);
}
