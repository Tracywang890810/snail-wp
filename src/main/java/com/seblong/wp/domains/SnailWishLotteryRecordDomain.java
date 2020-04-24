package com.seblong.wp.domains;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.seblong.wp.entities.SnailWish.AwardType;
import com.seblong.wp.entities.SnailWishLotteryRecord;

import lombok.Data;

@ApiModel("SnailWishLotteryRecord-许愿池获奖记录")
@Data
public class SnailWishLotteryRecordDomain implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7288906995259810681L;
	
	@ApiModelProperty(value = "许愿池获奖记录id", name = "unique", dataType = "String")
	private String unique;

	@ApiModelProperty(value = "用户id", name = "unique", dataType = "String")
	private String user;

	@ApiModelProperty(value = "获奖类型", name = "type", dataType = "String", allowableValues = "GOODS, COUPON")
	@JsonSerialize(using = ToStringSerializer.class)
	private AwardType type;

	@ApiModelProperty(value = "用户昵称", name = "name", dataType = "String")
	private String name;

	@ApiModelProperty(value = "蜗牛id", name = "uId", dataType = "Long")
	private long uId;

	@ApiModelProperty(value = "用户头像url", name = "avatar", dataType = "String", allowEmptyValue = true)
	private String avatar;
	
	private SnailWishLotteryRecordDomain() {
	}

	public static SnailWishLotteryRecordDomain fromEntity(SnailWishLotteryRecord snailWishLotteryRecord) {
		SnailWishLotteryRecordDomain domain = new SnailWishLotteryRecordDomain();
		domain.unique = String.valueOf(snailWishLotteryRecord.getId());
		domain.user = snailWishLotteryRecord.getUser();
		domain.type = snailWishLotteryRecord.getType();
		domain.name = snailWishLotteryRecord.getName();
		domain.uId = snailWishLotteryRecord.getUId();
		domain.avatar = snailWishLotteryRecord.getAvatar();
		return domain;
	}
	

}
