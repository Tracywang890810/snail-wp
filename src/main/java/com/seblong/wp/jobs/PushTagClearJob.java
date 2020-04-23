package com.seblong.wp.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seblong.wp.services.SnailWishService;
import com.seblong.wp.utils.SpringContextUtil;

public class PushTagClearJob implements Job{

	private final static Logger LOG = LoggerFactory.getLogger(PushJob.class);

	public final static String TRIGGER = "WISH_PUSH_TAG_CLEAR_TRIGGER";
	
	public final static String JOB = "WISH_PUSH_TAG_CLEAR_JOB";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		SnailWishService snailWishService = SpringContextUtil.getBean(SnailWishService.class);
		if( snailWishService != null ) {
			snailWishService.pushClearTag();
		}else {
			LOG.error("SnailWishService not exist....................");
		}
		
	}
	
}
