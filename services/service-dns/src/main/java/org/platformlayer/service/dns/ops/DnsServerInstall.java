package org.platformlayer.service.dns.ops;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.standardservice.StandardServiceInstall;

public class DnsServerInstall extends StandardServiceInstall {

	@Bound
	DnsServerTemplate template;

	@Override
	protected DnsServerTemplate getTemplate() {
		return template;
	}

}
