package org.platformlayer.service.dns.client;

import org.platformlayer.service.dns.client.model.DnsZone;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.Window;

public class DnsZoneItemActivity {
	// Empty interface declaration, similar to UiBinder
	interface Driver extends SimpleBeanEditorDriver<DnsZone, DnsZoneEditor> {
	}

	// Create the Driver
	Driver driver = GWT.create(Driver.class);

	public DnsZoneEditor edit(DnsZone p) {
		// PersonEditor is a DialogBox that extends Editor<Person>
		DnsZoneEditor editor = new DnsZoneEditor();
		// Initialize the driver with the top-level editor
		driver.initialize(editor);
		// Copy the data in the object into the UI
		driver.edit(p);

		return editor;
	}

	// Called by some UI action
	void save() {
		DnsZone edited = driver.flush();
		if (driver.hasErrors()) {
			// A sub-editor reported errors
		} else {
			doSomethingWithEditedPerson(edited);
		}
	}

	private void doSomethingWithEditedPerson(DnsZone edited) {
		Window.alert("DNS name was " + edited.getDnsName());
	}
}
