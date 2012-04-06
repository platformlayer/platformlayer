package org.platformlayer.ui.web.client.widgets;

import java.util.List;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;

public abstract class GridBase<T extends EntityProxy> extends Composite {
    protected final SingleSelectionModel<T> selectionModel = new SingleSelectionModel<T>();

    public interface Style extends CssResource {
    }

    public interface TableResources extends DataGrid.Resources {
        @Override
        @Source(value = { DataGrid.Style.DEFAULT_CSS, "DataGridPatch.css" })
        DataGrid.Style dataGridStyle();
    }

    protected int offsetOf(EntityProxyId<T> personId) {
        List<T> displayedItems = getTable().getVisibleItems();
        for (int offset = 0, j = displayedItems.size(); offset < j; offset++) {
            if (personId.equals(displayedItems.get(offset).stableId())) {
                return offset;
            }
        }
        return -1;
    }

    protected abstract DataGrid<T> getTable();

    protected void refreshSelection() {
        T person = selectionModel.getSelectedObject();
        if (person == null) {
            return;
        }
        // eventBus.fireEvent(new EditPersonEvent(person));
        selectionModel.setSelected(person, false);
    }

}
