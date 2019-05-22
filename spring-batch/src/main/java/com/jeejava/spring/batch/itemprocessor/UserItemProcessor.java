package com.jeejava.spring.batch.itemprocessor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.jeejava.spring.batch.vo.User;

@Component
public class UserItemProcessor implements ItemProcessor<User, User> {

	@Override
	public User process(final User user) throws Exception {
		final String domain = "jeejava.com";
		final String name = user.getName().toUpperCase();
		final String email = user.getEmail().substring(0, user.getEmail().indexOf("@") + 1) + domain;
		final User transformedUser = new User(name, email);
		System.out.println("Converting [" + user + "] => [" + transformedUser + "]");
		return transformedUser;
	}

}
