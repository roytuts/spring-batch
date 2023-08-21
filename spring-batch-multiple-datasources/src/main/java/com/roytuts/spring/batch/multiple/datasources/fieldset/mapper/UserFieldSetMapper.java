package com.roytuts.spring.batch.multiple.datasources.fieldset.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.boot.context.properties.bind.BindException;

import com.roytuts.spring.batch.multiple.datasources.vo.User;

public class UserFieldSetMapper implements FieldSetMapper<User> {

	@Override
	public User mapFieldSet(FieldSet fieldSet) throws BindException {
		User user = new User();
		user.setName(fieldSet.readString(0));
		user.setEmail(fieldSet.readString(1));
		return user;
	}

}
