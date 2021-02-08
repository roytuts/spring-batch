package com.roytuts.spring.batch.multiple.parallel.jobs.listener;

import java.util.List;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.stereotype.Component;

@Component
public class WriteListener<S> implements ItemWriteListener<S> {

	@Override
	public void beforeWrite(List<? extends S> items) {
		System.out.println("ReaderListener::beforeWrite() -> " + items);
	}

	@Override
	public void afterWrite(List<? extends S> items) {
		System.out.println("ReaderListener::afterWrite() -> " + items);
	}

	@Override
	public void onWriteError(Exception exception, List<? extends S> items) {
		System.out.println("ReaderListener::onWriteError() -> " + exception + ", " + items);
	}

}
