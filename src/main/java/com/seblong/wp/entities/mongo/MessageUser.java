package com.seblong.wp.entities.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "c_messageUsers")
public class MessageUser {
	
	@Id
	protected ObjectId id;
	
	// 本系统用户的id
	@Indexed
	private String user;

	// 极光推送或者其他第三方平台的用户id
	@Indexed
	@Field("third_user")
	private String thirdUser;

	@PersistenceConstructor
	public MessageUser(String user, String thirdUser) {
		this.user = user;
		this.thirdUser = thirdUser;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getThirdUser() {
		return thirdUser;
	}

	public void setThirdUser(String thirdUser) {
		this.thirdUser = thirdUser;
	}

}
