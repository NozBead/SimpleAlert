package fr.funetdelire.SimpleAlert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@Configuration
public class SimpleAlertApplication {
	
	Charset charset = Charset.defaultCharset();
	
	@Bean
	public LinkedList<String> alertQueue() {
		return new LinkedList<>();
	}
	
	@Bean
	public Selector publisherServer() throws IOException {
		ServerSocketChannel server = ServerSocketChannel.open();
		server.bind(new InetSocketAddress(6969));
		Selector selector = Selector.open();
		server.configureBlocking(false);
		server.register(selector, SelectionKey.OP_ACCEPT);
		return selector;
	}
	
	public void registerClient(ServerSocketChannel server) throws ClosedChannelException, IOException {
		SocketChannel newClient = server.accept();
		newClient.configureBlocking(false);
		newClient.register(publisherServer(), SelectionKey.OP_READ, ByteBuffer.allocate(1024));
	}
	
	public void readClient(SocketChannel client, SelectionKey key) throws IOException {
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		if (client.read(buffer) == -1) {
			client.close();
			key.cancel();
			buffer.flip();
			alertQueue().offer(charset.decode(buffer).toString());
			buffer.clear();
		}
	}
	
	@Scheduled(fixedDelay = 100)
	public void manageQueue() throws IOException {
		if (publisherServer().selectNow() != 0) {
			Iterator<SelectionKey> keyIterator = publisherServer().selectedKeys().iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				switch (key.interestOps()) {
					case SelectionKey.OP_ACCEPT:
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						registerClient(server);
					break;
					case SelectionKey.OP_READ:
						SocketChannel client = (SocketChannel) key.channel();
						readClient(client, key);
					break;
				}
				keyIterator.remove();
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SimpleAlertApplication.class, args);
	}
}
