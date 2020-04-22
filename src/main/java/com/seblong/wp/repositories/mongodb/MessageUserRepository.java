package com.seblong.wp.repositories.mongodb;

import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.seblong.wp.entities.mongo.MessageUser;

public interface MessageUserRepository extends MongoRepository<MessageUser, ObjectId>{
	
	MessageUser findByUser(String user);
	
	List<MessageUser> findByUserIn(Collection<String> users);
	
}
