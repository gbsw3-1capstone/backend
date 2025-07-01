package com.example.yoyakhaezoom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiSummaryDto {
    private String tag;
    private String tag2;
    private String small_tag;
    private String small_tag2;
    private String summaryHighlight;
    private String coreSummary;
}
