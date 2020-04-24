package com.seblong.wp.domains;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seblong.wp.entities.WishRecord;

@ApiModel("WishRecord-用户许愿记录")
@Data
public class WishRecordDomain {

    @ApiModelProperty(value = "unique", name = "unique", dataType = "Long", example = "记录unique")
    @JsonProperty(value = "unique")
    private Long unique;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "user", name = "user", dataType = "String", example = "用户id")
    private String user;

    /**
     * 开奖时间
     */
    @ApiModelProperty(value = "lotteryDate", name = "lotteryDate", dataType = "String", example = "开奖时间:20200224")
    private String lotteryDate;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "created", name = "created", dataType = "Long", example = "创建时间戳")
    private Long created;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "updated", name = "updated", dataType = "Long", example = "更新时间戳")
    private Long updated;

    /**
     * 状态
     */
    @ApiModelProperty(value = "status", name = "status", dataType = "String", example = "状态")
    private String status;

    /**
     * 设备id
     */
    @ApiModelProperty(value = "deviceId", name = "deviceId", dataType = "String", example = "设备id")
    private String deviceId;

    /**
     * 奖品类型
     */
    @ApiModelProperty(value = "awardType", name = "awardType", dataType = "String", example = "用户中奖状态,GOODS:实体奖品, COUPON:优惠卷")
    private String awardType;

    public WishRecordDomain() {
    }

    public static WishRecordDomain fromEntity(WishRecord entity){
        WishRecordDomain domain = new WishRecordDomain();
        domain.awardType = entity.getAwardType();
        domain.created = entity.getCreated();
        domain.deviceId = entity.getDeviceId();
        domain.user = entity.getUser();
        domain.unique = entity.getId();
        domain.lotteryDate = entity.getLotteryDate();
        domain.status = entity.getStatus();
        domain.updated = entity.getUpdated();
        return domain;
    }
}
