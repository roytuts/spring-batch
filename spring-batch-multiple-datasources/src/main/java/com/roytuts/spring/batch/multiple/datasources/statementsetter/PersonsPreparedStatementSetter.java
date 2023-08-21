package com.roytuts.spring.batch.multiple.datasources.statementsetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import com.roytuts.spring.batch.multiple.datasources.vo.User;

public class PersonsPreparedStatementSetter implements ItemPreparedStatementSetter<User> {

	@Override
	public void setValues(User item, PreparedStatement ps) throws SQLException {
		ps.setString(1, item.getName());
		ps.setString(2, item.getEmail());
	}

}
