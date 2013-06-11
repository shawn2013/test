package com.embracesource.processer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cars.ict.ots.payqueue.selector.PayQueuePartitionSelector;
import com.taobao.metamorphosis.Message;
import com.taobao.metamorphosis.client.MessageSessionFactory;
import com.taobao.metamorphosis.client.MetaClientConfig;
import com.taobao.metamorphosis.client.MetaMessageSessionFactory;
import com.taobao.metamorphosis.client.consumer.ConsumerConfig;
import com.taobao.metamorphosis.client.consumer.MessageConsumer;
import com.taobao.metamorphosis.client.consumer.MessageListener;
import com.taobao.metamorphosis.client.producer.MessageProducer;
import com.taobao.metamorphosis.client.producer.RoundRobinPartitionSelector;
import com.taobao.metamorphosis.client.producer.SendResult;
import com.taobao.metamorphosis.exception.MetaClientException;
import com.taobao.metamorphosis.utils.ZkUtils.ZKConfig;

public class ProductorAndConsumer {
	public static void main(String[] args) throws Exception {
		String connect = "127.0.0.1:2181";
		product(connect);
	}
	
	private static ExecutorService pool = Executors.newFixedThreadPool(3);


	public static void product(String connect) throws MetaClientException,
			IOException, InterruptedException {
		final MetaClientConfig metaClientConfig = new MetaClientConfig();
		final ZKConfig zkConfig = new ZKConfig();
		// ����zookeeper��ַ
		zkConfig.zkConnect = connect;
		metaClientConfig.setZkConfig(zkConfig);
		// New session factory,ǿ�ҽ���ʹ�õ���
		MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(
				metaClientConfig);
		// create producer,ǿ�ҽ���ʹ�õ���
		MessageProducer producer = sessionFactory.createProducer();
		// publish topic
		final String topic = "test";
		producer.publish(topic);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String line = null;
		final String group = "test";
		ConsumerConfig consumerConfig = new ConsumerConfig(group);
		consumerConfig.setMaxDelayFetchTimeInMills(2000);
		MessageConsumer consumer = sessionFactory
				.createConsumer(consumerConfig);
		// subscribe topic
		consumer.subscribe(topic, 1024 * 1024, new MessageListener() {

			public void recieveMessages(Message message) {
				message.setAttribute("0");
				message.getAttribute();
				System.out.println("receive:" + new Date() + ";"
						+ new String(message.getData()) + ";thread:" + Thread.currentThread().getId());
			}

			public Executor getExecutor() {
				return pool;
			}
		});
		// complete subscribe
		consumer.completeSubscribe();
		while ((line = reader.readLine()) != null) {
			// send message
			SendResult sendResult = producer.sendMessage(new Message(topic,
					line.getBytes()));
			System.out.println("send   :" + new Date() + ";thread=" + Thread.currentThread().getId());
			// check result
			if (!sendResult.isSuccess()) {
				System.err.println("Send message failed,error message:"
						+ sendResult.getErrorMessage());
			} else {
//				System.out.println("Send message successfully,sent to "
//						+ sendResult.getPartition());
			}
		}
	}
}
