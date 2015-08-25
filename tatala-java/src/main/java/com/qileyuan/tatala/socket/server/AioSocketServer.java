package com.qileyuan.tatala.socket.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.qileyuan.tatala.proxy.DefaultProxy;
import com.qileyuan.tatala.zookeeper.ServiceRegistry;

public class AioSocketServer {
	static Logger log = Logger.getLogger(AioSocketServer.class);

	private int listenPort;
	private int poolSize;
	private AsynchronousServerSocketChannel serverSocketChannel;
	private DefaultProxy defaultProxy;
	private static Map<Long, ServerSession> sessionMap = Collections.synchronizedMap(new HashMap<Long, ServerSession>());
	private List<SessionFilter> sessionFilterList = new ArrayList<SessionFilter>();
	
	private String zkRegistryAddress;

	public AioSocketServer(int listenPort, int poolSize) {
		this.listenPort = listenPort;
		this.poolSize = poolSize;
		this.defaultProxy = new DefaultProxy();
	}

	public void acceptConnections() {
		if(serverSocketChannel.isOpen()){
			ServerSession session = new ServerSession();
			session.setDefaultProxy(defaultProxy);
			session.setSessionFilterList(sessionFilterList);
			AioSocketHandler socketHandler = new AioSocketHandler(session);
			serverSocketChannel.accept(this, socketHandler);
		}else{
			throw new IllegalStateException("Server Socket Channel has been closed"); 
		}
	}

	public void setUpHandlers() {
		try {
			AsynchronousChannelGroup asyncChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(poolSize, Executors.defaultThreadFactory());
			serverSocketChannel = AsynchronousServerSocketChannel.open(asyncChannelGroup).bind(new InetSocketAddress(listenPort));
			serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		} catch (IOException e) {
			log.error("setUpHandlers error: ", e);
		}
		log.info("** " + poolSize + " handler thread has been setup! **");
		log.info("** Socket Server has been startup, listen port is " + listenPort + "! **");
	}

	public void registerZooKeeper(){
		try {
			if(zkRegistryAddress != null){
				ServiceRegistry serviceRegistry = new ServiceRegistry(zkRegistryAddress);
				//serviceRegistry.register(((InetSocketAddress)serverSocketChannel.getLocalAddress()).toString());
				serviceRegistry.register(InetAddress.getLocalHost().getHostAddress()+":"+listenPort);
			}
		} catch (IOException e) {
			log.error("registerZooKeeper error: ", e);
		}
	}
	
	public void start() {
		setUpHandlers();
		acceptConnections();
		registerZooKeeper();
	}

	public void registerProxy(DefaultProxy defaultProxy) {
		this.defaultProxy = defaultProxy;
	}

	public void registerSessionFilter(SessionFilter sessionFilter){
		sessionFilterList.add(sessionFilter);
	}
	
	public static Map<Long, ServerSession> getSessionMap() {
		return sessionMap;
	}
	
	public void setZKRegistryAddress(String zkRegistryAddress){
		this.zkRegistryAddress = zkRegistryAddress;
	}
}
