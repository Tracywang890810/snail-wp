package com.seblong.wp.controllers.manage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seblong.wp.domains.SnailWishDomain;
import com.seblong.wp.entities.SnailWish;
import com.seblong.wp.exceptions.ValidationException;
import com.seblong.wp.resource.StandardEntityResource;
import com.seblong.wp.resource.StandardRestResource;
import com.seblong.wp.services.SnailWishService;
import com.seblong.wp.utils.RegexUtils;

@Api("许愿池的管理后台接口")
@Controller
@RequestMapping(value = "/manage/wish", produces = MediaType.APPLICATION_JSON_VALUE)
public class APISnailWishManageController {

	@Autowired
	private SnailWishService snailWishService;

	@ApiOperation(value = "获取许愿池")
	@ApiResponses(value = { @ApiResponse(code = 1404, message = "snailwish-not-exist"),
			@ApiResponse(code = 200, message = "OK") })
	@GetMapping(value = "/get")
	public ResponseEntity<StandardEntityResource<SnailWishDomain>> get() {
		SnailWish snailWish = snailWishService.get();
		if (snailWish == null) {
			throw new ValidationException(1404, "snailwish-not-exist");
		}
		return new ResponseEntity<StandardEntityResource<SnailWishDomain>>(
				new StandardEntityResource<SnailWishDomain>(SnailWishDomain.fromEntity(snailWish)), HttpStatus.OK);
	}

	@ApiOperation(value = "创建许愿池", notes = "系统中只能存在一个许愿池")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "startDate", value = "开始日期", dataType = "String", paramType = "form", format = "yyyyMMdd", example = "20200320", required = true),
			@ApiImplicitParam(name = "endDate", value = "结束日期", dataType = "String", paramType = "form", format = "yyyyMMdd", example = "20200324", required = true),
			@ApiImplicitParam(name = "startTime", value = "许愿开始时间", dataType = "String", paramType = "form", format = "HHmmss", example = "210000", required = true),
			@ApiImplicitParam(name = "endTime", value = "许愿结束时间,注意没有240000，若为晚上12点，设置为235959", dataType = "String", paramType = "form", format = "HHmmss", example = "235959", required = true),
			@ApiImplicitParam(name = "couponUrl", value = "优惠卷地址", dataType = "String", paramType = "form", required = true),
			@ApiImplicitParam(name = "h5Url", value = "h5客户端地址，推送要用到", dataType = "String", paramType = "form", required = true)})
	@ApiResponses(value = { @ApiResponse(code = 1401, message = "invalid-startDate"),
			@ApiResponse(code = 1402, message = "invalid-endDate"),
			@ApiResponse(code = 1403, message = "invalid-startTime"),
			@ApiResponse(code = 1405, message = "invalid-endTime"),
			@ApiResponse(code = 1409, message = "invalid-h5Url"),
			@ApiResponse(code = 1410, message = "invalid-couponUrl"),
			@ApiResponse(code = 1411, message = "snailwish-exist"), @ApiResponse(code = 200, message = "OK") })
	@PostMapping(value = "/create")
	public ResponseEntity<StandardEntityResource<SnailWishDomain>> create(
			@RequestParam(value = "startDate", required = true) String startDate,
			@RequestParam(value = "endDate", required = true) String endDate,
			@RequestParam(value = "startTime", required = true) String startTime,
			@RequestParam(value = "endTime", required = true) String endTime,
			@RequestParam(value = "couponUrl", required = true) String couponUrl,
			@RequestParam(value = "h5Url", required = true) String h5Url) {
		validate(startDate, endDate, startTime, endTime,couponUrl, h5Url);
		SnailWish snailWish = snailWishService.create(startDate, endDate, startTime, endTime, couponUrl,h5Url);
		return new ResponseEntity<StandardEntityResource<SnailWishDomain>>(
				new StandardEntityResource<SnailWishDomain>(SnailWishDomain.fromEntity(snailWish)), HttpStatus.OK);
	}

	@ApiOperation(value = "更新许愿池")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(name = "unique", value = "许愿池id", dataType = "Long", paramType = "form", required = true, example = "0"),
			@ApiImplicitParam(name = "couponUrl", value = "优惠卷地址", dataType = "String", paramType = "form", required = false),
			@ApiImplicitParam(name = "h5Url", value = "h5客户端地址，推送要用到", dataType = "String", paramType = "form", required = true) })
	@ApiResponses(value = {
			@ApiResponse(code = 1404, message = "snailwish-not-exist"),
			@ApiResponse(code = 1409, message = "invalid-h5Url"),
			@ApiResponse(code = 1410, message = "invalid-couponUrl"), @ApiResponse(code = 200, message = "OK") })
	@PostMapping(value = "/update")
	public ResponseEntity<StandardEntityResource<SnailWishDomain>> update(
			@RequestParam(value = "unique", required = true) long id,
			@RequestParam(value = "couponUrl", required = true) String couponUrl,
			@RequestParam(value = "h5Url", required = true) String h5Url) {
		validate(couponUrl, h5Url);
		SnailWish snailWish = snailWishService.update(id, couponUrl,h5Url);
		return new ResponseEntity<StandardEntityResource<SnailWishDomain>>(
				new StandardEntityResource<SnailWishDomain>(SnailWishDomain.fromEntity(snailWish)), HttpStatus.OK);
	}

	@ApiOperation(value = "删除许愿池")
	@ApiImplicitParam(name = "unique", value = "许愿池id", dataType = "Long", paramType = "form", example = "0", required = true)
	@ApiResponses(value = { @ApiResponse(code = 1404, message = "snailwish-not-exist"),
			@ApiResponse(code = 200, message = "OK") })
	@PostMapping(value = "/delete")
	public ResponseEntity<StandardRestResource> delete(@RequestParam(value = "unique", required = true) long id) {
		snailWishService.delete(id);
		return new ResponseEntity<StandardRestResource>(new StandardRestResource(200, "OK"), HttpStatus.OK);
	}

	private void validate(String startDate, String endDate, String startTime, String endTime, String couponUrl, String h5Url) {

		LocalDate startLocalDate = null;
		try {
			startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (DateTimeParseException e) {
			throw new ValidationException(1401, "invalid-startDate");
		}

		LocalDate endLocalDate = null;
		try {
			endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.BASIC_ISO_DATE);
		} catch (DateTimeParseException e) {
			throw new ValidationException(1402, "invalid-endDate");
		}

		LocalTime startLocalTime = null;
		try {
			startLocalTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HHmmss"));
		} catch (DateTimeParseException e) {
			throw new ValidationException(1403, "invalid-startTime");
		}

		LocalTime lotteryLocalTime = LocalTime.parse("120000", DateTimeFormatter.ofPattern("HHmmss"));
		if (startLocalTime.compareTo(lotteryLocalTime) <= 0) {
			throw new ValidationException(1403, "invalid-startTime");
		}
		LocalTime endLocalTime = null;
		if ("240000".equals(endTime)) {
			endTime = "235959";
		}
		try {
			endLocalTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HHmmss"));
		} catch (DateTimeParseException e) {
			throw new ValidationException(1405, "invalid-endTime");
		}

		if (endLocalDate.compareTo(startLocalDate) <= 0) {
			throw new ValidationException(1402, "invalid-endDate");
		}
		if (endLocalTime.compareTo(startLocalTime) <= 0) {
			throw new ValidationException(1405, "invalid-endTime");
		}

		if (!StringUtils.isEmpty(couponUrl) && !RegexUtils.checkURL(couponUrl)) {
			throw new ValidationException(1410, "invalid-couponUrl");
		}
		
		if (StringUtils.isEmpty(couponUrl) || !RegexUtils.checkURL(couponUrl)) {
			throw new ValidationException(1409, "invalid-h5Url");
		}
		
		
	}
	
	private void validate(String couponUrl, String h5Url) {
		if (!StringUtils.isEmpty(couponUrl) && !RegexUtils.checkURL(couponUrl)) {
			throw new ValidationException(1410, "invalid-couponUrl");
		}
		
		if (StringUtils.isEmpty(couponUrl) || !RegexUtils.checkURL(couponUrl)) {
			throw new ValidationException(1409, "invalid-h5Url");
		}
	}

}
