package com.seblong.wp.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_wish_record", indexes = {@Index(columnList = "USER", unique = false),
        @Index(columnList = "DEVICEID", unique = false), @Index(columnList = "LOTTERYDATE")})
public class WishRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * 用户id
     */
    @Column(name = "USER", columnDefinition = "varchar(40)")
    private String user;

    /**
     * 开奖时间
     */
    @Column(name = "LOTTERYDATE", columnDefinition = "varchar(20)")
    private String lotteryDate;

    /**
     * 创建时间
     */
    @Column(name = "CREATED")
    private Long created;

    /**
     * 更新时间
     */
    @Column(name = "UPDATED")
    private Long updated;

    /**
     * 状态
     */
    @Column(name = "STATUS", columnDefinition = "varchar(20)")
    private String status;

    /**
     * 设备id
     */
    @Column(name = "DEVICEID", columnDefinition = "varchar(80)")
    private String deviceId;

    /**
     * 奖品类型
     */
    @Column(name = "AWARDTYPE")
    private String awardType;
    
    /**
     * 是否允许获得奖品或者大额优惠卷
     */
    @Column(name = "ALLOWBIG")
    private boolean allowBig;

    public WishRecord() {
    }
}
