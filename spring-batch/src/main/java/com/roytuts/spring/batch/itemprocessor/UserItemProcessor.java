package com.roytuts.spring.batch.itemprocessor;

import org.springframework.batch.item.ItemProcessor;

import com.roytuts.spring.batch.vo.User;

public class UserItemProcessor implements ItemProcessor<User, User> {

	@Override
	public User process(final User user) throws Exception {
		final String domain = "roytuts.com";
		final String name = user.getName().toUpperCase();
		final String email = user.getEmail().substring(0, user.getEmail().indexOf("@") + 1) + domain;
		final User transformedUser = new User(name, email);
		System.out.println("Converting [" + user + "] => [" + transformedUser + "]");
		return transformedUser;
	}

}
