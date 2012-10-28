package org.platformlayer.common;

import java.util.List;

import com.google.common.collect.Lists;

public class UntypedItemCollectionBase implements UntypedItemCollection {

	final List<UntypedItem> items;

	public UntypedItemCollectionBase() {
		this.items = Lists.newArrayList();
	}

	public UntypedItemCollectionBase(Iterable<UntypedItem> items) {
		this.items = Lists.newArrayList(items);
	}

	public void addAll(Iterable<UntypedItem> items) {
		for (UntypedItem item : items) {
			this.items.add(item);
		}
	}

	@Override
	public List<UntypedItem> getItems() {
		return items;
	}

	public static UntypedItemCollection empty() {
		return new UntypedItemCollectionBase();
	}

	public static UntypedItemCollection concat(Iterable<UntypedItemCollection> join) {
		UntypedItemCollectionBase ret = new UntypedItemCollectionBase();
		for (UntypedItemCollection c : join) {
			ret.addAll(c.getItems());
		}
		return ret;
	}

}
