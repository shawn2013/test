package com.embracesource.infinispan.monitor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.log4j.Logger;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.TypedProperties;

public class GenericKeyedObjectPoolMonitor {
	private GenericKeyedObjectPool connectionPool;
	private ConfigurationProperties cfg;
	private ScheduledExecutorService poolExecutorService;
	private String clusterName = "";

	private final static AtomicInteger counter = new AtomicInteger(0);

	private ScheduledExecutorService getExecutor() {
		ThreadFactory tf = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread th = new Thread(r, "GenericKeyedObjectPoolMonitor -" + counter.getAndIncrement());
				th.setDaemon(true);
				th.setPriority(1);
				return th;
			}
		};

		return new ScheduledThreadPoolExecutor(1, tf);
	}

	public GenericKeyedObjectPoolMonitor(GenericKeyedObjectPool connectionPool, ConfigurationProperties cfg) {
		this.poolExecutorService = getExecutor();
		this.connectionPool = connectionPool;
		this.cfg = cfg;
	}

	public void stop() {
		poolExecutorService.shutdown();
	}

	public void monitor() {
		int period = 60 * 1000;
		if (cfg != null) {
			period = ((TypedProperties) cfg.getProperties()).getIntProperty(
					"infinispan.client.hotrod.pool.monitor.period", 60 * 1000);
			clusterName = ((TypedProperties) cfg.getProperties()).getProperty("infinispan.cluster", "");
		}

		poolExecutorService.scheduleAtFixedRate(new Runnable() {
			protected Logger log = Logger.getLogger("hotrodpoolmonitor");

			public void run() {
				if (connectionPool != null) {
					if (log.isInfoEnabled()) {
						log.info(String.format("cluster %s, Active: %d, Idle: %d", clusterName,
								connectionPool.getNumActive(), connectionPool.getNumIdle()));
					}
				}

			}
		}, 60 * 1000, period, TimeUnit.MILLISECONDS);

	}
}