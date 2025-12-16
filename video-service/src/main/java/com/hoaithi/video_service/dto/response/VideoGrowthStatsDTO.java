package com.hoaithi.video_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoGrowthStatsDTO {
    String period; // "Mon", "Jan 15", "January"
    Long newUsers;
    Long activeUsers;
    Long videoUploads;
    Long views;
    Integer year;
    Integer month;
    Integer day;
}