package com.roytuts.spring.batch.quartz.scheduler.itemprocessor;

import org.springframework.batch.item.ItemProcessor;

import com.roytuts.spring.batch.quartz.scheduler.vo.Person;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	@Override
	public Person process(Person person) throws Exception {
		System.out.println("Processing: " + person);

		final String initCapFirstName = person.getFirstName().substring(0, 1).toUpperCase()
				+ person.getFirstName().substring(1);
		final String initCapLastName = person.getLastName().substring(0, 1).toUpperCase()
				+ person.getLastName().substring(1);

		Person transformedPerson = new Person();
		transformedPerson.setId(person.getId());
		transformedPerson.setFirstName(initCapFirstName);
		transformedPerson.setLastName(initCapLastName);

		return transformedPerson;
	}

}
