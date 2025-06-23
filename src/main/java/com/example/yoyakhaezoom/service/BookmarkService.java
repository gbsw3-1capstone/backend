package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.entity.Article;
import com.example.yoyakhaezoom.entity.Bookmark;
import com.example.yoyakhaezoom.entity.User;
import com.example.yoyakhaezoom.repository.ArticleRepository;
import com.example.yoyakhaezoom.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public String toggleBookmark(Long articleId, User user) {
        Article article = articleRepository.findById(articleId).orElseThrow(
                () -> new IllegalArgumentException("게시글이 존재하지 않습니다.")
        );

        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByUserAndArticle(user, article);

        if (bookmarkOptional.isPresent()) {
            bookmarkRepository.delete(bookmarkOptional.get());
            return "북마크가 취소되었습니다.";
        } else {
            Bookmark bookmark = Bookmark.builder().user(user).article(article).build();
            bookmarkRepository.save(bookmark);
            return "북마크에 추가되었습니다.";
        }
    }
}