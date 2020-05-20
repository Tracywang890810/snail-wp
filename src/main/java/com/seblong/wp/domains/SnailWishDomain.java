package com.seblong.wp.domains;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import com.seblong.wp.entities.SnailWish;
import com.seblong.wp.entities.SnailWish.WishStatus;

import lombok.Data;

@ApiModel("SnailWish-许愿池")
@Data
public class SnailWishDomain implements Serializable {

	private static final long serialVersionUID = -9150451637419376093L;

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

	@ApiModelProperty(value = "毫秒级倒计时，根据状态代表不同的倒计时", name = "countDown", dataType = "Long")
	// 倒计时，毫秒数
	private long countDown;

	@ApiModelProperty(value = "状态, 依次代表还未开始许愿、已经开始、等待开奖、许愿活动结束", name = "status", dataType = "String", allowableValues = "NOT_START, START, WAIT_LOTTERY, END")
	private WishStatus status;

	@ApiModelProperty(value = "是否已经参加", name = "joined", dataType = "Boolean")
	private boolean joined;
	
	@ApiModelProperty(value = "昨天是否已经参加", name = "yesterdayJoined", dataType = "Boolean")
	private boolean yesterdayJoined;

	@ApiModelProperty(value = "当前的时间戳", name = "current", dataType = "Long")
	private long current;

	@ApiModelProperty(value = "开奖", name = "lotteryDate", dataType = "String", example = "20200321")
	private String lotteryDate;

	@ApiModelProperty(value = "第几次开奖", name = "num", dataType = "Integer")
	private int num;

	private SnailWishDomain() {
	}

	public static SnailWishDomain fromEntity(SnailWish snailWish) {
		SnailWishDomain domain = new SnailWishDomain();
		domain.unique = String.valueOf(snailWish.getId());
		domain.startDate = snailWish.getStartDate();
		domain.endDate = snailWish.getEndDate();
		domain.startTime = snailWish.getStartTime();
		domain.endTime = snailWish.getEndTime();
		domain.couponUrl = snailWish.getCouponUrl();
		domain.current = snailWish.getCurrent();
		domain.joined = snailWish.isJoined();
		domain.yesterdayJoined = snailWish.isYesterdayJoined();
		domain.countDown = snailWish.getCountDown();
		domain.status = snailWish.getStatus();
		domain.lotteryDate = snailWish.getLotteryDate();
		domain.num = snailWish.getNum();
		return domain;
	}

}
