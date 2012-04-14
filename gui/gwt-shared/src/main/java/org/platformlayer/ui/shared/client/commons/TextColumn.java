package org.platformlayer.ui.shared.client.commons;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;

public class TextColumn<T, V> extends Column<T, String> {
	private final Accessor<T, V> accessor;

	public static <T, V> TextColumn<T, V> build(Accessor<T, V> accessor) {
		return new TextColumn<T, V>(accessor);
	}

	public TextColumn(Accessor<T, V> accessor) {
		super(new TextCell());
		this.accessor = accessor;
	}

	@Override
	public String getValue(T object) {
		V value = accessor.get(object);
		if (value == null) {
			return null;
		}
		return value.toString();
	}
}
