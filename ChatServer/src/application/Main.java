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
	
	//스레드 풀 사용
	public static ExecutorService threadPool;
	//접속한 클라이언트들을 관리 할수 있도록 만듬.
	public static Vector<Client> clients = new Vector<Client>();
	ServerSocket serverSocket;
	//서버를 구동시켜 클라이언트의 연결을 기다리는 메소드.
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			//특정한 ip번호와 port번호로 특정한 클라이언트에게 접속을 기다리게 해줌
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
				//계속해서 새로운 클라이언트가 접속 할 수 있도록 해줌
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						// 클라이언트를 추가
						clients.add(new Client(socket));
						System.out.println(" [클라이언트 접속] "
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
		//스레드 풀을 초기화
		threadPool = Executors.newCachedThreadPool();
		//클라이언트에 접속을 원하는 스레드를 넣어줍니다. 
		threadPool.submit(thread);
	}
	//서버의 작동을 중지시켜주는 메소드
	public void stopServer() {
		try {
			//현재 작업중인 모든 소켓 닫기
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
	
	
	// 서버 시작
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		//자신의 로컬 서버
		String IP = "127.0.0.1";
		int port = 2845;
		
		toggleButton.setOnAction(event -> {
			// 시작하기 버튼 누르면 서버시작
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				//자바 fx같은 경우는 바로 textArea에 쓰면 안되고 runLator와 같은 함수를 이용하여 어떠한 gui요소를 출력할 수 있도록 해야함.
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			// 접속종료 버튼 누르면 서버종료
			}else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});
		//크기
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("[ 채팅 서버 ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}