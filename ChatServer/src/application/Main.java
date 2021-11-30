package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	//������ Ǯ ���
	public static ExecutorService threadPool;
	//������ Ŭ���̾�Ʈ���� ���� �Ҽ� �ֵ��� ����.
	public static Vector<Client> clients = new Vector<Client>();
	ServerSocket serverSocket;
	//������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�.
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			//Ư���� ip��ȣ�� port��ȣ�� Ư���� Ŭ���̾�Ʈ���� ������ ��ٸ��� ����
			serverSocket.bind(new InetSocketAddress(IP, port));
		}catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				//����ؼ� ���ο� Ŭ���̾�Ʈ�� ���� �� �� �ֵ��� ����
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						// Ŭ���̾�Ʈ�� �߰�
						clients.add(new Client(socket));
						System.out.println(" [Ŭ���̾�Ʈ ����] "
								+ socket.getRemoteSocketAddress()
								+ " :" + Thread.currentThread().getName());
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
			
		};
		//������ Ǯ�� �ʱ�ȭ
		threadPool = Executors.newCachedThreadPool();
		//Ŭ���̾�Ʈ�� ������ ���ϴ� �����带 �־��ݴϴ�. 
		threadPool.submit(thread);
	}
	//������ �۵��� ���������ִ� �޼ҵ�
	public void stopServer() {
		try {
			//���� �۾����� ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();	
			}
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	// ���� ����
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("�������", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		//�ڽ��� ���� ����
		String IP = "127.0.0.1";
		int port = 2845;
		
		toggleButton.setOnAction(event -> {
			// �����ϱ� ��ư ������ ��������
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				//�ڹ� fx���� ���� �ٷ� textArea�� ���� �ȵǰ� runLator�� ���� �Լ��� �̿��Ͽ� ��� gui��Ҹ� ����� �� �ֵ��� �ؾ���.
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			// �������� ��ư ������ ��������
			}else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});
		//ũ��
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("[ ä�� ���� ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}