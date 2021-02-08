package com.roytuts.spring.batch.multiple.parallel.jobs.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import com.roytuts.spring.batch.multiple.parallel.jobs.model.Item;

public class JobWriter implements ItemWriter<Item> {

	private Item item = new Item();

	@Override
	public void write(List<? extends Item> items) throws Exception {
		System.out.println("JobWriter::write() -> item: " + item);

		item.setId(items.get(0).getId());
		item.setDate(items.get(0).getDate());
	}

	public Item getOutput() {
		return item;
	}

}
