package com.roytuts.spring.batch.csvtoxml.fieldset.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import com.roytuts.spring.batch.csvtoxml.vo.Person;

public class PersonFieldSetMapper implements FieldSetMapper<Person> {

	@Override
	public Person mapFieldSet(FieldSet fieldSet) {
		Person person = new Person();
		person.setId(fieldSet.readInt(0));
		person.setFirstName(fieldSet.readString(1));
		person.setLastName(fieldSet.readString(2));
		
		return person;
	}
}
