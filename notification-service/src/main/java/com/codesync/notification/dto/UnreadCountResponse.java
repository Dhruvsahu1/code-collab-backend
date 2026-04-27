package com.codesync.notification.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class UnreadCountResponse {

    private Long unreadCount;

    public UnreadCountResponse(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}