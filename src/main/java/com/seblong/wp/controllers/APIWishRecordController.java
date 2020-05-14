package com.seblong.wp.controllers;

import com.seblong.wp.domains.WishRecordDomain;
import com.seblong.wp.entities.SnailWish;
import com.seblong.wp.entities.WishRecord;
import com.seblong.wp.entities.mongo.User;
import com.seblong.wp.resource.StandardListResource;
import com.seblong.wp.resource.StandardRestResource;
import com.seblong.wp.services.SnailWishService;
import com.seblong.wp.services.UserService;
import com.seblong.wp.services.WishRecordService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "许愿记录相关接口")
@RestController
@RequestMapping(value = "/record", produces = {MediaType.APPLICATION_JSON_VALUE})
public class APIWishRecordController {

    @Autowired
    private WishRecordService wishRecordService;

    @Autowired
    private SnailWishService snailWishService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户参加许愿接口")
    @ApiImplicitParams(
            value = {@ApiImplicitParam(name = "user", value = "用户id", dataType = "String", paramType = "query"),
                    @ApiImplicitParam(name = "deviceId", value = "设备id", dataType = "String", paramType = "query")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 1403, message = "invalid-wish"),
            @ApiResponse(code = 1404, message = "user-not-exists"),
            @ApiResponse(code = 1405, message = "user-was-joined"),
            @ApiResponse(code = 1406, message = "device-was-joined"),
            @ApiResponse(code = 200, message = "OK")
    })
    @PostMapping("/wishing")
    public Map<String, Object> wishing(
            @RequestParam(value = "user") String userId,
            @RequestParam(value = "deviceId") String deviceId){
        Map<String, Object> rMap = new HashMap<>(4);
        SnailWish snailWish = snailWishService.get();
        if(!SnailWish.WishStatus.START.equals(snailWish.getStatus())){
            rMap.put("status", 1403);
            rMap.put("message", "invalid-wish");
            return rMap;
        }
        User user = userService.getBriefUser(userId);
        if(user == null){
            rMap.put("status", 1404);
            rMap.put("message", "user-not-exists");
            return rMap;
        }
        WishRecord record = wishRecordService.findByUserAndDate(userId, snailWish.getLotteryDate());
        if(record != null){
            rMap.put("status", 1405);
            rMap.put("message", "user-was-joined");
            return rMap;
        }
        WishRecord recordDevice = wishRecordService.findByDeviceIdAndDate(deviceId, snailWish.getLotteryDate());
        if(recordDevice != null){
            rMap.put("status", 1406);
            rMap.put("message", "device-was-joined");
            return rMap;
        }
        boolean isAllowBig = snailWishService.isAllowBig(snailWish, userId);
        wishRecordService.wishing(userId, deviceId, snailWish.getLotteryDate(), isAllowBig);
        rMap.put("status", 200);
        rMap.put("message", "OK");
        return rMap;
    }

    @ApiOperation(value = "获取用户许愿记录")
    @ApiImplicitParams(
            value = {@ApiImplicitParam(name = "user", value = "用户id", dataType = "String", paramType = "query")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = WishRecordDomain.class, responseContainer = "List")
    })
    @GetMapping("/list")
    public ResponseEntity<StandardRestResource> list(
            @RequestParam(value = "user") String userId){
        List<WishRecord> wishRecordList = wishRecordService.listByUser(userId);
        List<WishRecordDomain> domains = new ArrayList<>();
        wishRecordList.forEach(wishRecord -> {
            domains.add(WishRecordDomain.fromEntity(wishRecord));
        });
        return new ResponseEntity<StandardRestResource>(new StandardListResource<WishRecordDomain>(domains), HttpStatus.OK);
    }
}
