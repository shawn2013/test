package com.embracesource.infinispan.topologyId;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.TypedProperties;

public class TopologyResetTask {

	private AtomicInteger topologyId = null;
	
	private ConfigurationProperties cfg;
	private ScheduledExecutorService poolExecutorService;
	
	private final static AtomicInteger counter = new AtomicInteger(0);

	private ScheduledExecutorService getExecutor() {
		ThreadFactory tf = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread th = new Thread(r, "TopologyResetTask -"
						+ counter.getAndIncrement());
				th.setDaemon(true);
				th.setPriority(1);
				return th;
			}
		};

		return new ScheduledThreadPoolExecutor(1, tf);
	}

	public TopologyResetTask(AtomicInteger topologyId, ConfigurationProperties cfg) {
		super();
		this.topologyId = topologyId;
		this.cfg = cfg;
		this.poolExecutorService = getExecutor();
	}

	public void stop() {
		poolExecutorService.shutdown();
	}

	public void start() {
		int period = 60 * 1000;
		if (cfg != null) {
			period = ((TypedProperties) cfg.getProperties()).getIntProperty(
					"infinispan.client.hotrod.topology.reset.period", 60 * 1000);
		}
		poolExecutorService.scheduleAtFixedRate(new Runnable() {
			public void run() {
				topologyId.set(0);
			}
		},60 * 1000, period, TimeUnit.MILLISECONDS);

	}
}