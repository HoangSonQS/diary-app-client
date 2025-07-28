package com.mydiary.diaryappclient.model.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {
    private Long id;
    private String title;
    private String content;
    @JsonProperty("isPrimary")
    private boolean isPrimary;
    @JsonProperty("entryDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;
    private LocalDateTime createdAt;

}