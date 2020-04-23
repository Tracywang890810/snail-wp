package com.seblong.wp.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.seblong.wp.entities.SnailWish;
import com.seblong.wp.entities.SnailWish.AwardType;
import com.seblong.wp.entities.SnailWish.WishStatus;
import com.seblong.wp.entities.WishRecord;
import com.seblong.wp.enums.EntityStatus;
import com.seblong.wp.exceptions.ValidationException;
import com.seblong.wp.jobs.PushJob;
import com.seblong.wp.jobs.PushTagClearJob;
import com.seblong.wp.jobs.PushTagJob;
import com.seblong.wp.repositories.SnailWishRepository;
import com.seblong.wp.repositories.WishRecordRepository;
import com.seblong.wp.services.PushService;
import com.seblong.wp.services.SnailWishLotteryRecordService;
import com.seblong.wp.services.SnailWishService;
import com.seblong.wp.utils.RedisLock;
import com.seblong.wp.utils.SnailTriggerUtils;

@Log4j2
@Service
public class SnailWishServiceImpl implements SnailWishService {

	@Autowired
	private SnailWishRepository snailWishRepo;

	@Autowired
	private WishRecordRepository wishRecordRepo;

	@Autowired
	private SnailWishLotteryRecordService snailWishLotteryRecordService;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private PushService pushService;

	@Autowired
	private RestTemplate restTemplate;

	private final static String key = "SNAIL::WISH::V2";

	@Override
	public SnailWish create(String startDate, String endDate, String startTime, String endTime, String couponUrl,
			String h5Url) throws ValidationException {
		if (get() != null) {
			throw new ValidationException(1411, "snailwish-exist");
		}
		SnailWish snailWish = new SnailWish(startDate, endDate, startTime, endTime, couponUrl, h5Url);
		snailWish.calculateStart();
		snailWish.calculateEnd();
		LocalDate startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.BASIC_ISO_DATE);
		LocalDate lotteryDate = startLocalDate.plusDays(1);
		snailWish.setNum(1);
		snailWish.setLotteryDate(lotteryDate.format(DateTimeFormatter.BASIC_ISO_DATE));
		// 创建推送标签任务
		createPushTagJobAtDate(lotteryDate.format(DateTimeFormatter.BASIC_ISO_DATE));
		return snailWishRepo.save(snailWish);
	}

	@Override
	public SnailWish update(long id, String couponUrl, String h5Url) throws ValidationException {
		Optional<SnailWish> optional = snailWishRepo.findById(id);
		if (!optional.isPresent()) {
			throw new ValidationException(1404, "snailwish-not-exist");
		}
		SnailWish snailWish = optional.get();
		snailWish.setCouponUrl(couponUrl);
		snailWish.setH5Url(h5Url);
		removeSnailWish();
		return snailWishRepo.save(snailWish);
	}

	@Override
	public SnailWish get() {
		SnailWish snailWish = loadSnailWish();
		if (snailWish == null) {
			List<SnailWish> snailWishes = snailWishRepo.findAll();
			if (!CollectionUtils.isEmpty(snailWishes)) {
				snailWish = snailWishes.get(0);
				putSnailWish(snailWish);
				snailWish.calculateStatus();
			}
		} else {
			snailWish.calculateStatus();
		}
		return snailWish;
	}

	@Override
	public SnailWish get(String user) {
		SnailWish snailWish = loadSnailWish();
		if (snailWish == null) {
			List<SnailWish> snailWishes = snailWishRepo.findAll();
			if (!CollectionUtils.isEmpty(snailWishes)) {
				snailWish = snailWishes.get(0);
				putSnailWish(snailWish);
			}
		}
		if (snailWish != null) {
			snailWish.calculate();
			if (snailWish.getStatus().equals(SnailWish.WishStatus.WAIT_LOTTERY)) {
				LocalTime nowTime = LocalTime.now();
				LocalTime startLotteryTime = LocalTime.parse("115500", DateTimeFormatter.ofPattern("HHmmss"));
				LocalTime lotteryTime = LocalTime.parse("120000", DateTimeFormatter.ofPattern("HHmmss"));
				if (nowTime.compareTo(startLotteryTime) >= 0 && nowTime.compareTo(lotteryTime) <= 0) {
					LocalDate todayDate = LocalDate.now();
					LocalDate lotteryDate = LocalDate.parse(snailWish.getLotteryDate(),
							DateTimeFormatter.BASIC_ISO_DATE);
					if (todayDate.compareTo(lotteryDate) < 0) {
						snailWish.setLotteryDate(todayDate.format(DateTimeFormatter.BASIC_ISO_DATE));
						snailWish.setNum(snailWish.getNum() - 1);
					}
				}
			}
			// 判断用户是否已经参加
			if (wishRecordRepo.countByUserAndLotteryDate(user, snailWish.getLotteryDate()) > 0) {
				// 若已经参加则调用snailWish.joined();
				snailWish.joined();
			}
			LocalDate lotteryLocalDate = LocalDate.parse(snailWish.getLotteryDate(), DateTimeFormatter.BASIC_ISO_DATE);
			String yesterdayLottery = lotteryLocalDate.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
			if (!yesterdayLottery.equals(snailWish.getStartDate())) {
				snailWish.setYesterdayJoined(wishRecordRepo.countByUserAndLotteryDate(user, yesterdayLottery) > 0);
			}
		}
		return snailWish;
	}

	@Override
	public void delete(long id) {
		List<SnailWish> snailWishes = snailWishRepo.findAll();
		SnailWish snailWish = null;
		if (!CollectionUtils.isEmpty(snailWishes)) {
			snailWish = snailWishes.get(0);
			if (snailWish.getId() != id) {
				snailWish = null;
			}
		}
		if (snailWish != null) {
			snailWish.calculateStatus();
			if (!snailWish.getStatus().equals(WishStatus.END)) {
				LocalDate startLocalDate = LocalDate.parse(snailWish.getStartDate(), DateTimeFormatter.BASIC_ISO_DATE);
				LocalTime startLocalTime = LocalTime.parse(snailWish.getStartTime(),
						DateTimeFormatter.ofPattern("HHmmss"));
				LocalDateTime startLocalDateTime = LocalDateTime.of(startLocalDate, startLocalTime);
				LocalDateTime nowLocalDateTime = LocalDateTime.now();
				if (nowLocalDateTime.isAfter(startLocalDateTime)) {
					throw new ValidationException(1405, "snailwish-has-started");
				}
			}
			snailWishRepo.delete(snailWish);
			removeSnailWish();
			clearBigUser(snailWish);
		} else {
			throw new ValidationException(1404, "snailwish-not-exist");
		}

	}

	@Override
	public boolean isAllowBig(SnailWish snailWish, String user) {
		return !isBigUser(snailWish, user);
	}

	@Override
	public void lottery() {
		log.info("开始开奖方法。。。  ");
		String lockKey = "WISH::LOTTERY:LOCK::V2";
		RedisLock redisLock = new RedisLock(redisTemplate, lockKey);
		if (redisLock.tryLock()) {
			log.info("获取到锁，进入开奖逻辑。。。 ");
			try {
				SnailWish snailWish = get();
				if (snailWish != null && snailWish.getStatus().equals(WishStatus.WAIT_LOTTERY)) {
					log.info("符合开奖的条件，开始。。。  ");
					LocalDate nowLocalDate = LocalDate.now();
					String nowLocalDateStr = nowLocalDate.format(DateTimeFormatter.BASIC_ISO_DATE);
					if (nowLocalDateStr.equals(snailWish.getLotteryDate())) {
						// 70
						// 取出不允许奖品的记录
						int page = 0, size = 128;
						Sort sort = Sort.by("id");
						Pageable notAllowBigPageable = PageRequest.of(page, size, sort);
						Page<WishRecord> notAllowBigRecordPage = wishRecordRepo.findByLotteryDateAndAllowBig(
								nowLocalDateStr, false, notAllowBigPageable);
						long current = System.currentTimeMillis();
						while (notAllowBigRecordPage != null && notAllowBigRecordPage.hasContent()) {
							for (WishRecord wishRecord : notAllowBigRecordPage) {
								log.info("Record:  " + wishRecord.getId() + " 获得优惠卷");
								wishRecord.setAwardType(AwardType.COUPON.toString());
								wishRecord.setUpdated(current);
								wishRecord.setStatus(EntityStatus.DONE.toString());
							}
							snailWishLotteryRecordService.create(notAllowBigRecordPage.getContent());
							wishRecordRepo.saveAll(notAllowBigRecordPage.getContent());
							if (notAllowBigRecordPage.hasNext()) {
								notAllowBigPageable = PageRequest.of(++page, size, sort);
								notAllowBigRecordPage = wishRecordRepo.findByLotteryDateAndAllowBig(nowLocalDateStr,
										false, notAllowBigPageable);
							} else {
								break;
							}
						}
						// 取出允许奖品的记录
						int prize = 70, prizeRemain = 70;
						long allowBigTotal = wishRecordRepo.countByLotteryDateAndAllowBig(nowLocalDateStr, true);
						if (allowBigTotal > 0) {
							double total = (double) allowBigTotal;
							log.info("allowBigTotal : " + allowBigTotal);
							page = 0;
							Pageable allowBigPageable = PageRequest.of(page, size, sort);
							Page<WishRecord> allowBigRecordPage = wishRecordRepo.findByLotteryDateAndAllowBig(
									nowLocalDateStr, true, allowBigPageable);
							while (allowBigRecordPage != null && allowBigRecordPage.hasContent()) {
								int count = allowBigRecordPage.getNumberOfElements();
								double rate = 0;
								if (count >= allowBigTotal) {
									rate = 1;
								} else {
									rate = count / total;
								}
								log.info(" count :  " + count);
								log.info("Rate : " + rate);
								int countPrize = new Double(Math.ceil(prize * rate)).intValue();
								if (count > countPrize) {
									count -= countPrize;
								} else {
									countPrize = count;
									count = 0;
								}
								log.info("countPrize1 : " + countPrize);
								if (prizeRemain > 0 && prizeRemain > countPrize) {
									prizeRemain -= countPrize;
								} else {
									countPrize = prizeRemain;
									prizeRemain = 0;
								}
								log.info("countPrize2 : " + countPrize);
								List<WishRecord> wishRecords = new ArrayList<WishRecord>(allowBigRecordPage
										.getContent().size());
								wishRecords.addAll(allowBigRecordPage.getContent());
								List<String> bigUsers = new ArrayList<String>();
								List<WishRecord> prizeRecords = new ArrayList<WishRecord>();
								Random random = new Random();
								for (int i = 0; i < countPrize; i++) {
									int index = random.nextInt(wishRecords.size());
									WishRecord wishRecord = wishRecords.remove(index);
									log.info("Record:  " + wishRecord.getId() + " 获得实物奖品");
									wishRecord.setAwardType(AwardType.GOODS.toString());
									wishRecord.setUpdated(current);
									wishRecord.setStatus(EntityStatus.DONE.toString());
									prizeRecords.add(wishRecord);
									bigUsers.add(wishRecord.getUser());
								}
								if (!CollectionUtils.isEmpty(bigUsers))
									putBigUser(snailWish, bigUsers);

								for (int i = 0; i < wishRecords.size(); i++) {
									WishRecord wishRecord = wishRecords.get(i);
									log.info("Record:  " + wishRecord.getId() + " 获得优惠卷");
									wishRecord.setAwardType(AwardType.COUPON.toString());
									wishRecord.setUpdated(current);
									wishRecord.setStatus(EntityStatus.DONE.toString());
								}
								wishRecords.addAll(prizeRecords);

								snailWishLotteryRecordService.create(wishRecords);
								wishRecordRepo.saveAll(wishRecords);
								if (allowBigRecordPage.hasNext()) {
									allowBigPageable = PageRequest.of(++page, size, sort);
									allowBigRecordPage = wishRecordRepo.findByLotteryDateAndAllowBig(nowLocalDateStr,
											true, allowBigPageable);
								} else {
									break;
								}

							}
						}
					}
					log.info("结算逻辑结束");
					LocalDate endLocalDate = LocalDate.parse(snailWish.getEndDate(), DateTimeFormatter.BASIC_ISO_DATE);
					LocalDate endLotteryLocalDate = endLocalDate.plusDays(1);
					if (endLotteryLocalDate.compareTo(nowLocalDate) > 0) {
						log.info("明天继续开奖");
						nowLocalDate = nowLocalDate.plusDays(1);
						snailWish.setLotteryDate(nowLocalDate.format(DateTimeFormatter.BASIC_ISO_DATE));
						snailWish.setNum(snailWish.getNum() + 1);
						log.info("开始创建推送标签任务 " + snailWish.getLotteryDate());
						createPushTagJobAtDate(snailWish.getLotteryDate());
					}
					removeSnailWish();
					putSnailWish(snailWish);
					snailWishRepo.save(snailWish);
				}

			} catch (Exception e) {
				log.error("开奖出现异常。。。。。。。。。。。。。。。");
				e.printStackTrace();
			} finally {
				redisLock.unlock();
			}
		}
	}

	@Override
	public void push() {
		log.info("推送方法调用。。。");
		SnailWish snailWish = get();
		if (snailWish != null) {
			LocalDate nowLocalDate = LocalDate.now();
			LocalDate nextLocalDate = nowLocalDate.plusDays(1);
			String nextDate = nextLocalDate.format(DateTimeFormatter.BASIC_ISO_DATE);
			String nowDate = nowLocalDate.format(DateTimeFormatter.BASIC_ISO_DATE);
			if( nextDate.equals(snailWish.getLotteryDate()) ) {
				if( wishRecordRepo.countByLotteryDate(nowDate) > 0  ) {
					String tag = "WISH_USERS_" + nowDate;
					log.info("开始发送开奖推送: " + nowDate);
					pushService.send("今日许愿池已开奖！快来看看你的获奖信息吧～", snailWish.getH5Url(), "", 0, tag, "WISHING");
				}
			}
		}
	}

	@Override
	public void pushTag() {
		log.info("推送标签创建方法调用。。。");
		SnailWish snailWish = get();
		String nowDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		if (snailWish != null && nowDate.equals(snailWish.getLotteryDate())) {
			String tag = "WISH_USERS_" + nowDate;
			int page = 0, size = 128;
			Sort sort = Sort.by("id");
			Pageable pageable = PageRequest.of(page, size, sort);
			Page<WishRecord> wishPage = wishRecordRepo.findByLotteryDate(nowDate, pageable);
			boolean push = false;
			while (wishPage != null && wishPage.hasContent()) {
				Set<String> userIds = wishPage.stream().map(WishRecord::getUser).collect(Collectors.toSet());
				pushService.addToTag(tag, userIds);
				if (!push)
					push = true;
				if (wishPage.hasNext()) {
					pageable = PageRequest.of(++page, size, sort);
					wishPage = wishRecordRepo.findByLotteryDate(nowDate, pageable);
				} else {
					break;
				}
			}
			if (push) {
				log.info("开始创建推送任务：" + nowDate);
				createPushJobAtDate(nowDate);
				log.info("开始创建推送标签清理任务：" + nowDate);
				createPushTagJobClearAtDate(nowDate);
			}
		}
	}

	@Override
	public void pushClearTag() {
		log.info("推送标签清理方法调用。。。");
		SnailWish snailWish = get();
		if (snailWish != null) {
			LocalDate nowLocalDate = LocalDate.now();
			LocalDate nextLocalDate = nowLocalDate.plusDays(1);
			String nextDate = nextLocalDate.format(DateTimeFormatter.BASIC_ISO_DATE);
			String nowDate = nowLocalDate.format(DateTimeFormatter.BASIC_ISO_DATE);
			if( nextDate.equals(snailWish.getLotteryDate()) ) {
				if( wishRecordRepo.countByLotteryDate(nowDate) > 0  ) {
					String tag = "WISH_USERS_" + nowDate;
					log.info("开始清理推送标签: " + nowDate);
					pushService.deleteTag(tag);
				}
			}
		}
	}

	private void putSnailWish(SnailWish snailWish) {
		redisTemplate.boundValueOps(key).set(snailWish);
	}

	private SnailWish loadSnailWish() {
		Object result = redisTemplate.boundValueOps(key).get();
		if (result != null) {
			return (SnailWish) result;
		}
		return null;
	}

	private void removeSnailWish() {
		redisTemplate.delete(key);
	}

	private void putBigUser(SnailWish snailWish, List<String> users) {
		if (!CollectionUtils.isEmpty(users)) {
			String key = "SNAIL::WISH::BIG::V2::" + snailWish.getId();
			redisTemplate.boundSetOps(key).add(users.toArray());
		}
	}

	private boolean isBigUser(SnailWish snailWish, String user) {
		String key = "SNAIL::WISH::BIG::V2::" + snailWish.getId();
		return redisTemplate.boundSetOps(key).isMember(user);
	}

	private void clearBigUser(SnailWish snailWish) {
		String key = "SNAIL::WISH::BIG::V2::" + snailWish.getId();
		redisTemplate.delete(key);
	}

	private void createPushJobAtDate(String date) {

		LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
		LocalTime localTime = LocalTime.parse("120000", DateTimeFormatter.ofPattern("HHmmss"));
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
		long timestamp = localDateTime.toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
		try {
			JobKey jobKey = new JobKey(PushJob.JOB);
			if (scheduler.checkExists(jobKey)) {
				scheduler.deleteJob(jobKey);
			}
			Trigger trigger = SnailTriggerUtils.getTriggerAtDate(PushJob.TRIGGER, "", timestamp);
			scheduler.scheduleJob(JobBuilder.newJob(PushJob.class).withIdentity(PushJob.JOB).build(), trigger);
			log.info("创建推送任务成功: " + date + "120000");
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private void createPushTagJobAtDate(String date) {

		LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
		LocalTime localTime = LocalTime.parse("020000", DateTimeFormatter.ofPattern("HHmmss"));
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
		long timestamp = localDateTime.toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
		try {
			JobKey jobKey = new JobKey(PushTagJob.JOB);
			if (scheduler.checkExists(jobKey)) {
				scheduler.deleteJob(jobKey);
			}
			Trigger trigger = SnailTriggerUtils.getTriggerAtDate(PushTagJob.TRIGGER, "", timestamp);
			scheduler.scheduleJob(JobBuilder.newJob(PushTagJob.class).withIdentity(PushTagJob.JOB).build(), trigger);
			log.info("创建推送标签任务成功: " + date + "020000");
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	private void createPushTagJobClearAtDate(String date) {

		LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
		LocalTime localTime = LocalTime.parse("130000", DateTimeFormatter.ofPattern("HHmmss"));
		LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
		long timestamp = localDateTime.toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
		try {
			JobKey jobKey = new JobKey(PushTagClearJob.JOB);
			if (scheduler.checkExists(jobKey)) {
				scheduler.deleteJob(jobKey);
			}
			Trigger trigger = SnailTriggerUtils.getTriggerAtDate(PushTagClearJob.TRIGGER, "", timestamp);
			scheduler.scheduleJob(JobBuilder.newJob(PushTagClearJob.class).withIdentity(PushTagClearJob.JOB).build(), trigger);
			log.info("创建推送标签清理任务成功: " + date + "130000");
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
