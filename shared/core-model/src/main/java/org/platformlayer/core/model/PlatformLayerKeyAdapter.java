//package org.platformlayer.core.model;
//
//import javax.xml.bind.annotation.adapters.XmlAdapter;
//
//import org.apache.log4j.Logger;
//
///**
// * This would be nice, but breaks the format which we're storing in the DB
// * 
// * @author justinsb
// * 
// */
//public class PlatformLayerKeyAdapter extends XmlAdapter<String, PlatformLayerKey> {
//	@SuppressWarnings("unused")
//	private static final Logger log = LoggerFactory.getLogger(PlatformLayerKeyAdapter.class);
//
//	@Override
//	public PlatformLayerKey unmarshal(String v) throws Exception {
//		if (v == null) {
//			return null;
//		}
//		PlatformLayerKey key = PlatformLayerKey.parse(v);
//		return key;
//	}
//
//	@Override
//	public String marshal(PlatformLayerKey v) throws Exception {
//		if (v == null) {
//			return null;
//		}
//		return v.getUrl();
//
//	}
// }
