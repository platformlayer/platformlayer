package org.platformlayer.service.zookeeper.ops;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.platformlayer.ops.OpsException;

import com.netflix.curator.framework.CuratorFramework;

public class ZookeeperClient implements Watcher, Closeable {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ZookeeperClient.class);

	private final List<String> dnsNames;

	private final CuratorFramework zk;

	final ZookeeperContext zookeeperContext;

	public ZookeeperClient(ZookeeperContext zookeeperContext, List<String> dnsNames) throws OpsException {
		this.zookeeperContext = zookeeperContext;
		this.dnsNames = dnsNames;
		this.zk = zookeeperContext.buildZk(dnsNames);

		this.zk.start();
	}

	@Override
	public void process(WatchedEvent event) {
		log.info("Got ZK event: " + event);
	}

	public byte[] read(String zkPath) throws OpsException {
		try {
			byte[] data = zk.getData().forPath(zkPath);
			return data;
		} catch (NoNodeException e) {
			log.debug("Mapping no-node to null", e);
			return null;
			// try {
			// data = zk.getData(zkPath, false, null);
			// } catch (NoNodeException e) {
			// log.debug("Mapping no-node to null", e);
			// return null;
			// } catch (KeeperException e) {
			// throw new OpsException("Error reading zookeeper", e);
			// } catch (InterruptedException e) {
			// Thread.currentThread().interrupt();
			// throw new OpsException("Interrupted while reading zookeeper", e);
			// }
			// return data;
		} catch (Exception e) {
			// Ugh ... let's hide Netflix's sins...
			throw new OpsException("Error reading zookeeper", e);
		}
	}

	public void put(String zkPath, byte[] data) throws OpsException {
		try {
			try {
				zk.setData().forPath(zkPath, data);
			} catch (NoNodeException e) {
				log.debug("Creating new node after no-node error", e);
				zk.create().forPath(zkPath, data);
			}
			// try {
			// data = zk.getData(zkPath, false, null);
			// } catch (NoNodeException e) {
			// log.debug("Mapping no-node to null", e);
			// return null;
			// } catch (KeeperException e) {
			// throw new OpsException("Error reading zookeeper", e);
			// } catch (InterruptedException e) {
			// Thread.currentThread().interrupt();
			// throw new OpsException("Interrupted while reading zookeeper", e);
			// }
		} catch (Exception e) {
			// Ugh ... let's hide Netflix's sins...
			throw new OpsException("Error writing zookeeper", e);
		}

		// try {
		// try {
		// zk.setData(zkPath, data, -1);
		// } catch (NoNodeException e) {
		// log.debug("Creating new node after no-node error", e);
		// zk.create(zkPath, data, null, CreateMode.PERSISTENT);
		// }
		// } catch (KeeperException e) {
		// throw new OpsException("Error writing zookeeper", e);
		// } catch (InterruptedException e) {
		// Thread.currentThread().interrupt();
		// throw new OpsException("Interrupted while writing zookeeper", e);
		// }
	}

	@Override
	public void close() throws IOException {
		zk.close();
	}

}
