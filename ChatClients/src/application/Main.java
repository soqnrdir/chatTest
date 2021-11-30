package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class Main extends Application {

	Socket socket;
	TextArea textArea;

	// 클라이언트 프로그램 동작 메소드 (어떤 IP로 , 어떤 port로 접속할지 정해줌)
	public void startClient(String IP, int port) {
		// 스레드 객체 생성!
		Thread thread = new Thread() {
			public void run() {
				try {
					// socket 초기화
					socket = new Socket(IP, port);
					receive();
				} catch (Exception e) {
					// 오류가 생긴다면
					if (!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}

	// 클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close(); 
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// 서버로부터 메시지를 전달받는 메소드
	public void receive() {
		// 서버 프로그램으로부터 메시지를 계속 전달 받을 수 있도록
		while (true) {
			try {
				// 서버로부터 메시지를 전달 받을 수 있도록
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if (length == -1)
					throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(() -> {
					// textArea는 GUI요소중 하나로 화면에 어떠한 메시지를 주고 받았는지 출력해 주는 요소.
					textArea.appendText(message);
				});

			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}

	// 서버로 메시지를 전송하는 메소드
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}

	// 실제로 프로그램을 동작시키는 메서드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		//서버에 ip주소가 들어갈 수 있도록.
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("2845");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event ->{
			send(userName.getText() + " : " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				}catch(Exception e) {
					e.printStackTrace();
				}
				//접속하기 누르면 서버에 IP, PORT를 매개변수로 넘겨줌
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					textArea.appendText("[채팅방접속]\n");
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				input.requestFocus();
				
			}else {
				//종료하기 버튼 누를 때
				stopClient();
				Platform.runLater(() ->{
					textArea.appendText("[채팅방 퇴장]");
				});
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle(" [채팅 클라이언트] ");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}