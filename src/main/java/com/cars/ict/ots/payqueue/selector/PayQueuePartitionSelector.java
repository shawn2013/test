package com.cars.ict.ots.payqueue.selector;

import java.util.List;

import com.taobao.metamorphosis.Message;
import com.taobao.metamorphosis.client.producer.PartitionSelector;
import com.taobao.metamorphosis.client.producer.RoundRobinPartitionSelector;
import com.taobao.metamorphosis.cluster.Partition;
import com.taobao.metamorphosis.exception.MetaClientException;

public class PayQueuePartitionSelector implements PartitionSelector {

	@Override
	public Partition getPartition(String topic, List<Partition> partitions,
			Message message) throws MetaClientException {
		return partitions.get(0);
	}

	
}
