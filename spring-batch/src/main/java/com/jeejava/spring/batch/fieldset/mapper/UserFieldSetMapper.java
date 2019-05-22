package com.jeejava.spring.batch.fieldset.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.stereotype.Component;

import com.jeejava.spring.batch.vo.User;

@Component
public class UserFieldSetMapper implements FieldSetMapper<User> {

	@Override
	public User mapFieldSet(FieldSet fieldSet) throws BindException {
		User user = new User();
		user.setName(fieldSet.readString(0));
		user.setEmail(fieldSet.readString(1));
		return user;
	}

}
