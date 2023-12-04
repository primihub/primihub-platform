package com.primihub.biz.entity.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteDataResourceEvent {
    private Long resourceId;
    private Integer resourceState;
}
