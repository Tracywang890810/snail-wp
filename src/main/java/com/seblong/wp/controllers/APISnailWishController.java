package com.seblong.wp.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.seblong.wp.domains.SnailWishDomain;
import com.seblong.wp.entities.SnailWish;
import com.seblong.wp.exceptions.ValidationException;
import com.seblong.wp.resource.StandardEntityResource;
import com.seblong.wp.services.SnailWishService;

@Api(tags = "许愿池的前端接口")
@Controller
@RequestMapping(value = "/wish", produces = MediaType.APPLICATION_JSON_VALUE)
public class APISnailWishController {

	@Autowired
	private SnailWishService snailWishService;

	@ApiOperation(value = "获取许愿池")
	@ApiImplicitParam(name = "user", value = "用户id", dataType = "String", paramType = "query", required = true)
	@ApiResponses(value = { @ApiResponse(code = 1404, message = "user-not-exist"),
			@ApiResponse(code = 1405, message = "snailwish-not-exist")})
	@GetMapping(value = "/get")
	public ResponseEntity<StandardEntityResource<SnailWishDomain>> get(@RequestParam(value = "user", required = true) String user) {

		if (!ObjectId.isValid(user)) {
			throw new ValidationException(1404, "user-not-exist");
		}
		SnailWish snailWish = snailWishService.get(user);
		if (snailWish == null) {
			throw new ValidationException(1405, "snailwish-not-exist");
		}
		return new ResponseEntity<StandardEntityResource<SnailWishDomain>>(new StandardEntityResource<SnailWishDomain>(
				SnailWishDomain.fromEntity(snailWish)), HttpStatus.OK);
	}

}
