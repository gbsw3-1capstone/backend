package com.example.yoyakhaezoom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiSummaryDto {
    private String summaryHighlight;
    private String coreSummary;
    private List<TermDto> terms;
    private String discussionPoint;
}