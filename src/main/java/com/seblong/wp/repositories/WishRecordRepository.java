package com.seblong.wp.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seblong.wp.entities.WishRecord;

@Repository
public interface WishRecordRepository extends JpaRepository<WishRecord, Long> {

	WishRecord findByUserAndLotteryDate(String userId, String lotteryDate);

	List<WishRecord> findByUser(String userId);

	WishRecord findByDeviceIdAndLotteryDate(String deviceId, String lotteryDate);

	long countByUserAndLotteryDate(String userId, String lotteryDate);

	long countByLotteryDateAndAllowBig(String lotteryDate, boolean allowBig);

	Page<WishRecord> findByLotteryDateAndAllowBig(String lotteryDate, Boolean allowBig, Pageable pageable);

	Page<WishRecord> findByLotteryDate(String lotteryDate, Pageable pageable);

	long countByLotteryDate(String lotteryDate);
}
