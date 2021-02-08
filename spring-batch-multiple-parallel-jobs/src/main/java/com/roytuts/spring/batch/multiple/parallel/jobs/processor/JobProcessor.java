package com.roytuts.spring.batch.multiple.parallel.jobs.processor;

import org.springframework.batch.item.ItemProcessor;

import com.roytuts.spring.batch.multiple.parallel.jobs.model.Item;

public class JobProcessor implements ItemProcessor<Item, Item> {

	@Override
	public Item process(Item item) throws Exception {
		System.out.println("JobProcessor::process() -> item: " + item);

		Item model = new Item();
		model.setId(item.getId());
		model.setDate(item.getDate());

		return model;
	}

}
