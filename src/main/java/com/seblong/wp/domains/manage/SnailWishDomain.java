package com.seblong.wp.domains.manage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import com.seblong.wp.entities.SnailWish;

import lombok.Data;

@ApiModel("SnailWish-许愿池-管理后台")
@Data
public class SnailWishDomain implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8447002868084841494L;

	@ApiModelProperty(value = "许愿池id", name = "unique", dataType = "String")
	private String unique;

	@ApiModelProperty(value = "开始日期", name = "startDate", dataType = "String", example = "20200320")
	// 开始日期
	private String startDate;

	@ApiModelProperty(value = "结束日期", name = "endDate", dataType = "String", example = "20200324")
	// 结束日期
	private String endDate;

	@ApiModelProperty(value = "许愿开始时间", name = "startTime", dataType = "String", example = "210000")
	// 每天的开始时间
	private String startTime;

	@ApiModelProperty(value = "许愿结束时间", name = "endTime", dataType = "String", example = "235959")
	// 每天的结束时间
	private String endTime;

	@ApiModelProperty(value = "优惠卷url", name = "couponUrl", dataType = "String", allowEmptyValue = true)
	// 中额优惠卷地址
	private String couponUrl;

	
	private SnailWishDomain () {
	}

	public static SnailWishDomain fromEntity(SnailWish snailWish) {
		SnailWishDomain domain = new SnailWishDomain();
		domain.unique = String.valueOf(snailWish.getId());
		domain.startDate = snailWish.getStartDate();
		domain.endDate = snailWish.getEndDate();
		domain.startTime = snailWish.getStartTime();
		domain.endTime = snailWish.getEndTime();
		domain.couponUrl = snailWish.getCouponUrl();
		return domain;
	}
	
}
