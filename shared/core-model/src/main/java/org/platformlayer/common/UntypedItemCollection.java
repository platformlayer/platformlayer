package org.platformlayer.common;

import java.util.List;

// We'd like to implements Iterable<UntypedItem> but that breaks GWT
// See bug (?): http://code.google.com/p/google-web-toolkit/issues/detail?id=4864
public interface UntypedItemCollection {

	List<UntypedItem> getItems();

}
