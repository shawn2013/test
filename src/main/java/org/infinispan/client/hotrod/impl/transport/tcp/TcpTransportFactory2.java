package org.infinispan.client.hotrod.impl.transport.tcp;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.TypedProperties;
import org.infinispan.client.hotrod.impl.protocol.Codec;

import com.embracesource.infinispan.monitor.GenericKeyedObjectPoolMonitor;
import com.embracesource.infinispan.topologyId.TopologyResetTask;

public class TcpTransportFactory2 extends TcpTransportFactory {
	private static final Logger log = Logger
			.getLogger(TcpTransportFactory2.class);

	private volatile int retryCount;

	private GenericKeyedObjectPoolMonitor genericKeyedObjectPoolMonitor;
	
	private TopologyResetTask topologyResetTask;

	private final Object lock = new Object();

	@Override
	public void start(Codec codec, ConfigurationProperties cfg,
			Collection<SocketAddress> staticConfiguredServers,
			AtomicInteger topologyId, ClassLoader classLoader) {
		super.start(codec, cfg, staticConfiguredServers, topologyId,
				classLoader);
		synchronized (lock) {
			long maxWait=((TypedProperties)cfg.getProperties()).getLongProperty("maxWait", 500);
			super.getConnectionPool().setMaxWait(maxWait);
			
			retryCount = ((TypedProperties) cfg.getProperties())
					.getIntProperty("infinispan.client.hotrod.retry_count", 0);
			
			genericKeyedObjectPoolMonitor = new GenericKeyedObjectPoolMonitor(
					super.getConnectionPool(), cfg);
			genericKeyedObjectPoolMonitor.monitor();
			
			topologyResetTask = new TopologyResetTask(topologyId,cfg);
			topologyResetTask.start();
		}
		if (log.isInfoEnabled()) {
			log.info(String.format("Statically configured servers: %s",
					staticConfiguredServers));
			log.info(String
					.format("retryCount = %d; Tcp no delay = %b; client socket timeout = %d ms; connect timeout = %d ms",
							retryCount, cfg.getTcpNoDelay(),
							cfg.getSoTimeout(), cfg.getConnectTimeout()));
			log.info("genericKeyedObjectPoolMonitor start...");
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		synchronized (lock) {
			if (genericKeyedObjectPoolMonitor != null) {
				genericKeyedObjectPoolMonitor.stop();
			}
		}
		if (log.isInfoEnabled()) {
			log.info("genericKeyedObjectPoolMonitor destroy...");
		}
	}

	@Override
	public int getTransportCount() {
		if (Thread.currentThread().isInterrupted()) {
			return -1;
		}
		if (retryCount > 0) {
			return retryCount;
		}
		return super.getTransportCount();
	}

}
