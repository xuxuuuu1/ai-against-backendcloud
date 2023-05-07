package com.kob.botrunningsystem.service.impl.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bot {
    private Integer userId;
    private String botCode;
    // 地图局面
    private String input;
}
