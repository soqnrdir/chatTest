package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//�Ѹ��� Ŭ���̾�Ʈ�� ����ϰ� ���ִ� Ŭ���� �Դϴ�.
public class Client {
	
		//������ �־���� Ŭ���̾�Ʈ�� ��Ʈ��ũ�󿡼� ����� �� ����.
		Socket socket;
		
		//������ ����
		public Client(Socket socket) {
			this.socket = socket;
			
			receive();
		}
		
		//Ŭ���̾�Ʈ�κ��� �޽����� ���޹޴� �޼ҵ�.
		public void receive() {
			Runnable thread = new Runnable() {
				@Override
				public void run() {
					try {
						while(true) {
							InputStream in = socket.getInputStream();
							byte[] buffer = new byte[512];
							int length = in.read(buffer);
							while(length == -1) throw new IOException();
							System.out.println("[�޽��� ���� ����]"
									+ socket.getRemoteSocketAddress()
									+ ":" + Thread.currentThread().getName());
							String message = new String(buffer, 0, length, "UTF-8");
							for(Client client : Main.clients) {
								client.send(message);
							}
						}
						
					}catch(Exception e){
						try {
							System.out.println(" [�޽��� ���� ����]"
							+ socket.getRemoteSocketAddress()
							+ " : " + Thread.currentThread().getName());
						}catch(Exception e2){
							e2.printStackTrace();
						}
					}
				}
				
			};
			Main.threadPool.submit(thread);
		}
		
		//Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ�.
		public void send(String message) {
			Runnable thread = new Runnable() {

				@Override
				public void run() {
					try {
						OutputStream out = socket.getOutputStream();
						byte[] buffer = message.getBytes("UTF-8");
						//���ۿ� ��� ������ �������� Ŭ���̾�Ʈ���� ����
						out.write(buffer);
						out.flush();
					}catch(Exception e){
						try {
							System.out.println("[�޽��� �ۼ��� ����]" 
									+ socket.getRemoteSocketAddress()
									+ " :" + Thread.currentThread().getName());
								Main.clients.remove(Client.this);
								socket.close();
						}catch(Exception e2) {
							e2.printStackTrace();
						}
					}
					
				}
				
			};
			Main.threadPool.submit(thread);
		}
		
}