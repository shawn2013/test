package com.embracesource.infinispan;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.util.FileLookupFactory;
import org.junit.Test;


public class LocalTest {

	public static String HOTROD_CLIENT_PROPERTIES = "hotrod-client.properties";

	@Test
	public void testGet(){
		InputStream stream = FileLookupFactory.newInstance().lookupFile(
				HOTROD_CLIENT_PROPERTIES,
				Thread.currentThread().getContextClassLoader());
		Properties properties = new Properties();
		try {
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			throw new HotRodClientException(
					"Issues configuring from client hotrod-client.properties",
					e);
			
			
		}
		RemoteCacheManager basicCacheContainer = new RemoteCacheManager(properties);
		RemoteCache cache = basicCacheContainer.getCache();
		cache.clear();
		long a = System.currentTimeMillis();
		cache.put("key", "value");
		
		Set keySet = cache.keySet();
		for (Object object : keySet) {
			Object val = cache.get(object);
			@SuppressWarnings("unchecked")
			MetadataValue data = cache.getWithMetadata(object);
			System.out.println("key=" + object + "; value=" + val);
		}
		System.out.println(cache.size());
	}
}
