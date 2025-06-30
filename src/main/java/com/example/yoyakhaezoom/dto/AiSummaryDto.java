package com.example.yoyakhaezoom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiSummaryDto {
    private String tag;
    private String small_tag;
    private String summaryHighlight;
    private String coreSummary;
}