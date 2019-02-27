package cn.intellimuyan.bardclient.player;

import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.model.msg.JoinMsg;
import cn.intellimuyan.bardclient.nettyclient.ISymphonyClient;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

@Slf4j
public class PlayerWindow extends Application {

    public static ApplicationContext ctx;

    private static TextField nameField;
    private ISymphonyClient client;

    @Override
    public void init() throws Exception {
        this.client = ctx.getBean(ISymphonyClient.class);
    }

    @Override
    public void start(Stage stage) {
        GridPane parent = new GridPane();
        parent.setAlignment(Pos.CENTER);
        parent.setPadding(new Insets(5, 10, 5, 10));
        parent.setHgap(10);
        parent.setVgap(10);
        nameField = new TextField("我的名字");
        parent.add(nameField, 0, 0);
        Button saveButton = new Button("Save");
        saveButton.setOnMouseClicked(event -> sendName());
        parent.add(saveButton, 1, 0);
        CheckBox checkbox = new CheckBox("连接");
        checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                client.connect();
                sendName();
                log.info("[远程指挥]连接成功");
            } else {
                client.close();
                log.info("[远程指挥]连接关闭");
            }
        });
        parent.add(checkbox, 2, 0);


        stage.setTitle("盗宝贼乐团乐手");
        stage.setScene(new Scene(parent));
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setWidth(300);
        stage.setHeight(100);
        stage.show();
    }

    private void sendName() {
        String text = nameField.getText();
        JoinMsg joinMsg = new JoinMsg();
        joinMsg.setName(Optional.ofNullable(text).filter(s -> !s.isEmpty()).orElse("unknown"));
        client.sendCmd(CmdType.JOIN, joinMsg);
    }

}
