package org.platformlayer.xaas.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.platformlayer.ids.ItemType;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class Models {
	final List<ModelClass<?>> models;

	final Map<ItemType, ModelClass<?>> modelMap;
	final Map<Class<?>, ModelClass<?>> modelMapByJavaClass;

	public Models(List<? extends ModelClass<?>> list) {
		this.models = Collections.unmodifiableList(list);
		this.modelMap = Maps.uniqueIndex(models, new Function<ModelClass<?>, ItemType>() {
			@Override
			public ItemType apply(ModelClass<?> modelClass) {
				return modelClass.getItemType();
			}
		});
		this.modelMapByJavaClass = Maps.uniqueIndex(models, new Function<ModelClass<?>, Class<?>>() {
			@Override
			public Class<?> apply(ModelClass<?> modelClass) {
				return modelClass.getJavaClass();
			}
		});
	}

	public Iterable<ModelClass<?>> all() {
		return models;
	}

	public ModelClass<?> find(ItemType itemType) {
		return modelMap.get(itemType);
	}
}
