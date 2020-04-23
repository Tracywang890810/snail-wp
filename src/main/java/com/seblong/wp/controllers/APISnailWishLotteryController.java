package com.seblong.wp.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seblong.wp.domains.SnailWishLotteryRecordDomain;
import com.seblong.wp.entities.SnailWish.AwardType;
import com.seblong.wp.entities.SnailWishLotteryRecord;
import com.seblong.wp.exceptions.ValidationException;
import com.seblong.wp.resource.StandardEntitiesResource;
import com.seblong.wp.resource.StandardEntityResource;
import com.seblong.wp.services.SnailWishLotteryRecordService;

@Api(tags = "获奖记录接口")
@Controller
@RequestMapping(value = "/wish/lottery", produces = MediaType.APPLICATION_JSON_VALUE)
public class APISnailWishLotteryController {

	@Autowired
	private SnailWishLotteryRecordService snailWishLotteryRecordService;

	@ApiOperation(value = "获取用户的获奖记录")
    @ApiImplicitParams(
            value = {
            		@ApiImplicitParam(name = "user", value = "用户id", dataType = "Long", paramType = "query", required = true, example = "0"),
            		@ApiImplicitParam(name = "date", value = "日期", dataType = "String", paramType = "query", format = "yyyyMMdd", example = "20200310", required = true )		
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 1404, message = "lottery-record-not-exist")
    })
	@GetMapping(value = "/get")
	public ResponseEntity<StandardEntityResource<SnailWishLotteryRecordDomain>> get(@RequestParam(value = "user", required = true) String user,
			@RequestParam(value = "date", required = true) String date) {
		SnailWishLotteryRecord record = snailWishLotteryRecordService.get(user, date);
		if (record == null) {
			throw new ValidationException(1404, "lottery-record-not-exist");
		}
		return new ResponseEntity<StandardEntityResource<SnailWishLotteryRecordDomain>>(new StandardEntityResource<SnailWishLotteryRecordDomain>(
				SnailWishLotteryRecordDomain.fromEntity(record)), HttpStatus.OK);
	}

	@ApiOperation(value = "分页获取某个日期的获奖记录")
    @ApiImplicitParams(
            value = {
            		@ApiImplicitParam(name = "type", value = "获奖类型", dataType = "String", paramType = "query", allowableValues = "GOODS, COUPON_BIG, COUPON_SMALL", required = true),
            		@ApiImplicitParam(name = "date", value = "日期", dataType = "String", paramType = "query", format = "yyyyMMdd", example = "20200310" , required = true),
            		@ApiImplicitParam(name = "page", value = "页数，从1开始", dataType = "Integer", paramType = "query", example = "1", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 1404, message = "no-more-lottery-record")
    })
	@GetMapping(value = "/list")
	public ResponseEntity<StandardEntitiesResource<SnailWishLotteryRecordDomain>> list(@RequestParam(value = "type", required = true) AwardType type,
			@RequestParam(value = "date", required = true) String date,
			@RequestParam(value = "page", required = true) int page) {

		Page<SnailWishLotteryRecord> recordPage = snailWishLotteryRecordService.list(date, type, page);
		if (recordPage != null && recordPage.hasContent()) {
			List<SnailWishLotteryRecordDomain> domains = recordPage.stream()
					.map(SnailWishLotteryRecordDomain::fromEntity).collect(Collectors.toList());
			return new ResponseEntity<StandardEntitiesResource<SnailWishLotteryRecordDomain>>(
					new StandardEntitiesResource<SnailWishLotteryRecordDomain>(domains, recordPage.getTotalElements(),
							recordPage.hasPrevious(), recordPage.hasNext()),
					HttpStatus.OK);
		} else {
			throw new ValidationException(1404, "no-more-lottery-record");
		}

	}
}
