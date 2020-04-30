package com.seblong.wp.services.impl;

import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jiguang.common.resp.DefaultResult;
import cn.jpush.api.device.DeviceClient;
import cn.jpush.api.device.TagAliasResult;
import cn.jpush.api.device.TagListResult;

import com.seblong.wp.entities.mongo.MessageUser;
import com.seblong.wp.repositories.mongodb.MessageUserRepository;
import com.seblong.wp.services.PushService;

@Log4j2
@Service
public class PushServiceImpl implements PushService {

	@Value("${snail.wp.jpush.ak}")
	private String jpushAK;

	@Value("${snail.wp.jpush.sk}")
	private String jpushSK;

	@Value("${snail.wp.push.url}")
	private String pushUrl;

	@Autowired
	private MessageUserRepository messageUserRepo;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public void deleteTag(String tag) {
		DeviceClient deviceClient = new DeviceClient(jpushSK, jpushAK);
		try {
			DefaultResult result = deviceClient.deleteTag(tag, null);
			log.info(result.getResponseCode());
		} catch (APIConnectionException e) {
			log.error(e.getLocalizedMessage());
		} catch (APIRequestException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void addToTag(String tag, Set<String> userIds) {
		List<MessageUser> messageUsers = messageUserRepo.findByUserIn(userIds);
		if (null != messageUsers && messageUsers.size() > 0) {
			Set<String> registerIds = messageUsers.stream().map(MessageUser::getThirdUser).collect(Collectors.toSet());
			DeviceClient deviceClient = new DeviceClient(jpushSK, jpushAK);
			try {
				DefaultResult result = deviceClient.addRemoveDevicesFromTag(tag, registerIds, null);
				log.info(result.getResponseCode());
			} catch (APIConnectionException e) {
				log.error(e.getLocalizedMessage());
			} catch (APIRequestException e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void send(String content, String url, String imageUrl, long sendDate, String group, String messageType) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("verify", "6d4a2667e8e720cc60f2a04ec4d2c28ec4f7b0df");
			params.add("title", "蜗牛睡眠官方");
			params.add("content", content);
			if (!StringUtils.isEmpty(url)) {
				params.add("url", URLEncoder.encode(url, "utf-8"));
			}
			if (!StringUtils.isEmpty(imageUrl)) {
				params.add("imageUrl", imageUrl);
			}
			params.add("sendDate", String.valueOf(sendDate));
			if (!"NORMAL".equals(messageType)) {
				params.add("messageType", messageType);
			}
			if (!StringUtils.isEmpty(group)) {
				params.add("sendType", "1");
				params.add("targets", group);
			}
			restTemplate.postForObject(pushUrl, params, String.class);
			log.info("开奖推送: " + group + " 发送成功。");
		} catch (Exception e) {
			log.error("开奖推送: " + group + " 发送失败。由于 " + e.getLocalizedMessage());
		}
	}

	public void list() {

		DeviceClient deviceClient = new DeviceClient(jpushSK, jpushAK);
		try {
			TagListResult tagListResult = deviceClient.getTagList();
			log.info(tagListResult.getOriginalContent());
			TagAliasResult result = deviceClient.getDeviceTagAlias("170976fa8ad8ecc4ccf");
			log.info(result.getOriginalContent());
			result = deviceClient.getDeviceTagAlias("1114a8979286387916f");
			log.info(result.getOriginalContent());
		} catch (APIConnectionException e) {
			log.error(e.getLocalizedMessage());
		} catch (APIRequestException e) {
			log.error(e.getLocalizedMessage());
		}
		
	}

}
