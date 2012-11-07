//package org.platformlayer.service.dns.client;
//
//import org.platformlayer.core.model.PlatformLayerKey;
//import org.platformlayer.gwt.client.ShellAbstractActivity;
//import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
//import org.platformlayer.gwt.client.places.ShellPlace;
//import org.platformlayer.service.dns.client.domainlist.DomainListPlace;
//import org.platformlayer.service.dns.client.home.HomePlace;
//
//import com.google.gwt.place.shared.Place;
//
//public abstract class DnsActivity extends ShellAbstractActivity {
//	public DnsPlugin getPlugin() {
//		return DnsPlugin.get();
//	}
//
//	public void goToServiceHome() {
//		DomainListPlace newPlace = getHomePlace().getDomainListPlace();
//		goTo(newPlace);
//	}
//
//	protected HomePlace getHomePlace() {
//		return getPlace().findParent(HomePlace.class);
//	}
//
//	public void goTo(DomainName v) {
//		// Place newPlace = DomainListPlace.INSTANCE.getProjectPlace(project.getProjectName());
//		// placeController.goTo(newPlace);
//	}
//
//	public void goTo(DomainContactInfo v) {
//		PlatformLayerKey key = v.getKey();
//
//		Place newPlace = getHomePlace().getContactInfoPlace(key.getItemIdString());
//		goTo(newPlace);
//	}
//
//	public void addNewContact() {
//		Place newPlace = getHomePlace().getContactInfoPlace("");
//		goTo(newPlace);
//	}
//
//	protected PlatformLayerKey buildPlatformLayerKey(String serviceType, String itemType, String itemId) {
//		return PlatformLayerKey.build(null, getProject().getProjectName(), serviceType, itemType, itemId);
//	}
//
//	protected abstract PlatformLayerKey getKey();
//}
