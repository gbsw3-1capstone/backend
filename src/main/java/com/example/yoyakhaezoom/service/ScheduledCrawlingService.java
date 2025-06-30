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

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j(topic = "Scheduled Crawling")
@Service
@RequiredArgsConstructor
public class ScheduledCrawlingService {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;

    private final AtomicBoolean isCrawlingEnabled = new AtomicBoolean(true);

    @Scheduled(cron = "0 0 8,18 * * *")
    public void crawlLatestNews() {
        if (!isCrawlingEnabled.get()) {
            log.info("자동 크롤링이 비활성화되어 있습니다. 작업을 건너뜁니다.");
            return;
        }

        log.info("자동 뉴스 크롤링 및 요약을 시작합니다.");


        int summarizedCount = 0;
        final int CRAWLING_LIMIT = 150;

        String newsListPageUrl = "https://www.yna.co.kr/";

        try {
            Document doc = Jsoup.connect(newsListPageUrl).get();
            Elements articleLinks = doc.select("a.tit-news");

            for (Element link : articleLinks) {
                if (!isCrawlingEnabled.get()) {
                    log.info("크롤링 작업 중 중단 요청이 들어왔습니다.");
                    break;
                }

                if (summarizedCount >= CRAWLING_LIMIT) {
                    log.info("요약 한도(" + CRAWLING_LIMIT + "개)에 도달하여 크롤링을 중단합니다.");
                    break;
                }

                String articleUrl = link.absUrl("href");

                if (articleUrl.startsWith("http") && articleRepository.findByOriginalUrl(articleUrl).isEmpty()) {
                    log.info("새로운 뉴스 발견: {}", articleUrl);

                    try {
                        articleService.summarizeArticleByUrl(articleUrl);
                        summarizedCount++;
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

    public void enableCrawling() {
        this.isCrawlingEnabled.set(true);
    }

    public void disableCrawling() {
        this.isCrawlingEnabled.set(false);
    }
}