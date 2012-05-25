package org.platformlayer.service.zookeeper.ops;

import java.io.IOException;
import java.util.List;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Joiner;
import com.netflix.curator.RetryPolicy;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.CuratorFrameworkFactory.Builder;
import com.netflix.curator.retry.RetryOneTime;

@Singleton
public class ZookeeperContext {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ZookeeperContext.class);

	public CuratorFramework buildZk(List<String> dnsNames) throws OpsException {
		String connectionString = Joiner.on(",").join(dnsNames);

		Builder builder = CuratorFrameworkFactory.builder();
		builder.connectString(connectionString);
		TimeSpan retryInterval = TimeSpan.FIVE_SECONDS;
		RetryPolicy retryPolicy = new RetryOneTime((int) retryInterval.getTotalMilliseconds());
		builder.retryPolicy(retryPolicy);

		CuratorFramework curatorFramework;
		try {
			curatorFramework = builder.build();
		} catch (IOException e) {
			throw new OpsException("Error building zookeeper connection", e);
		}

		return curatorFramework;
		// TimeSpan sessionTimeout = TimeSpan.TEN_SECONDS;
		//
		// ZooKeeper zk = new ZooKeeper(connectionString, (int) sessionTimeout.getTotalMilliseconds(), this);
		// return zk;

	}

	public ZookeeperClient buildZookeeperClient(List<String> dnsNames) throws OpsException {
		return new ZookeeperClient(this, dnsNames);
	}

}
