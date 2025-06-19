package com.example.yoyakhaezoom.service;

import com.example.yoyakhaezoom.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j(topic = "Scheduled Crawling")
@Service
@RequiredArgsConstructor
public class ScheduledCrawlingService {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;

    @Scheduled(cron = "0 0 8,18 * * *")
    public void crawlLatestNews() {
        log.info("자동 뉴스 크롤링 및 요약을 시작합니다.");

        String newsListPageUrl = "https://www.yna.co.kr/international/all";

        try {
            Document doc = Jsoup.connect(newsListPageUrl).get();
            Elements articleLinks = doc.select("div.list div.item-box a");

            for (Element link : articleLinks) {
                String articleUrl = link.absUrl("href");

                if (articleRepository.findByOriginalUrl(articleUrl).isEmpty()) {
                    log.info("새로운 뉴스 발견: {}", articleUrl);

                    try {
                        articleService.summarizeArticleByUrl(articleUrl);
                    } catch (Exception e) {
                        log.error("개별 기사 처리 중 오류 발생. URL: {}", articleUrl, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("뉴스 목록 페이지 크롤링 중 오류 발생", e);
        }
        log.info("자동 뉴스 크롤링 및 요약 작업을 종료합니다.");
    }
}