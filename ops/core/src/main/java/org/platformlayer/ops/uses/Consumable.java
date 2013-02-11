package org.platformlayer.ops.uses;

import java.util.Map;

import org.platformlayer.InetAddressChooser;
import org.platformlayer.ops.OpsException;

public interface Consumable {

	Map<String, String> buildConsumerConfiguration(InetAddressChooser inetAddressChooser) throws OpsException;

}
