package cn.intellimuyan.bardclient.director;

import cn.intellimuyan.bardclient.base.MidiParser;
import cn.intellimuyan.bardclient.base.PlayCommand;
import cn.intellimuyan.bardclient.director.model.ChannelInfo;
import cn.intellimuyan.bardclient.director.model.PlayConfig;
import cn.intellimuyan.bardclient.model.Player;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class DirectorWindow extends Application {

    public static ApplicationContext ctx;

    private static MidiParser midiParser;

    private static Set<Player> players = Collections.synchronizedSet(new HashSet<>());
    private static Text fileNameText;
    private static ProgressBar pb;

    private static ObservableList<Node> instrumentItems;
    private static ObservableList<Node> commandItems;
    private static ListView<Node> instrumentView;
    private static ListView<Node> commandView;

    private static String recentPath;
    private static DirectorExecutor executor;
    private static TextField delayField;
    private static TextField waitField;

    private static Text pastTime;
    private static Text totalTime;

    @Override
    public void init() throws Exception {
        DirectorWindow.executor = ctx.getBean(DirectorExecutor.class);
    }

    @Override
    public void start(Stage stage) {
        GridPane parent = new GridPane();
        parent.setAlignment(Pos.TOP_CENTER);
        parent.setPadding(new Insets(5, 10, 5, 10));
        parent.setHgap(10);
        parent.setVgap(10);
        GridPane topGrid = new GridPane();
        GridPane footGrid = new GridPane();
        parent.add(topGrid, 0, 0);
        parent.add(footGrid, 0, 1);
        setGridAttr(footGrid);
        setGridAttr(topGrid);
        setTitle(topGrid);
        setParameter(topGrid);
        setStatusComponents(topGrid);
        setTrackersView(footGrid);


        Button openButton = new Button("OpenFile");

        openButton.setOnAction(
                event -> {
                    FileChooser fileChooser = new FileChooser();
                    if (recentPath != null) {
                        fileChooser.setInitialDirectory(new File(recentPath));
                    }
                    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("MIDI Files (.mid)", ".mid"));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        recentPath = file.getParent();
                        commandItems.clear();
                        try {
                            midiParser = new MidiParser(file);
                            setTotalTime(midiParser.getMills());
                            setPastTime(0, 0);
                            instrumentItems.remove(0, instrumentItems.size());
                            resetInstrumentsAndPlayer();
                            fileNameText.setText(file.getName().replace(".mid", ""));
                        } catch (Exception e) {
                            log.error("[Midi解析]异常,file:{}", file.getPath(), e);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("解析错误");
                            alert.setContentText(e.getClass().getSimpleName() + ":" + e.getMessage());
                            alert.show();
                        }
                    }
                });

        topGrid.add(openButton, 3, 2);

        Button playButton = new Button("Play");
        playButton.setOnMouseClicked(event -> {
            if (instrumentItems.isEmpty() || executor.stat == DirectorExecutor.Stat.RUNNING) {
                return;
            }
            List<ChannelInfo> list = new ArrayList<>(instrumentItems.size());
            for (Node instrumentItem : instrumentItems) {
                HBox box = (HBox) instrumentItem;
                Text text = (Text) box.getChildren().get(0);
                ComboBox<Player> playerChoose = (ComboBox<Player>) box.getChildren().get(1);
                Player selectedPlayer = playerChoose.getSelectionModel().getSelectedItem();
                if (selectedPlayer == null || selectedPlayer == Player.EMPTY) {
                    continue;
                }
                String channel = text.getText();
                ChannelInfo info = new ChannelInfo();
                info.setChannel(channel);
                info.setCommands(midiParser.getSheet(channel));
                info.setPlayer(selectedPlayer);
                list.add(info);
            }
            if (list.isEmpty()) {
                return;
            }
            PlayConfig config = DirectorWindow.getConfig();
            executor.play(list, config);
        });
        Button stopButton = new Button("Stop");
        stopButton.setOnMouseClicked(event -> executor.stop());
        footGrid.add(playButton, 0, 1);
        footGrid.add(stopButton, 1, 1);

        stage.setTitle("盗宝贼乐团指挥家");
        stage.setScene(new Scene(parent));
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setWidth(600);
        stage.setHeight(500);
        stage.show();
    }

    private void setTrackersView(GridPane footGrid) {
        commandView = new ListView<>();
        commandView.setPlaceholder(new Text("乐谱"));
        commandView.setPrefSize(280, 320);
        commandItems = commandView.getItems();
        footGrid.add(commandView, 0, 0);

        instrumentView = new ListView<>();
        instrumentView.setPlaceholder(new Text("音轨"));
        instrumentView.setPrefSize(280, 320);
        instrumentView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            HBox box = (HBox) newValue;
            Text text = (Text) box.getChildren().get(0);
            commandItems.clear();
            commandItems.addAll(midiParser.getSheet(text.getText())
                    .stream().map(PlayCommand::toString)
                    .map(Text::new)
                    .collect(Collectors.toList()));
        });
        instrumentItems = instrumentView.getItems();
        footGrid.add(instrumentView, 1, 0);
    }

    private void setStatusComponents(GridPane topGrid) {
        int size = 10;
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.LIGHTGREY);
        gc.fillOval(0, 0, size, size);
        topGrid.add(canvas, 0, 2);
        executor.addListeners(stat -> {
            switch (stat) {
                case RUNNING:
                    gc.setFill(Color.LIGHTGREEN);
                    break;
                case IDLE:
                    gc.setFill(Color.LIGHTGRAY);
                    break;
            }
            gc.fillOval(0, 0, size, size);
        });
        fileNameText = new Text("");
        topGrid.add(fileNameText, 1, 2);
        pb = new ProgressBar(0);
        pb.setMinWidth(400);
        topGrid.add(pb, 0, 3, 3, 3);
        pastTime = new Text("00:00:00");
        Text slash = new Text("/");
        totalTime = new Text("00:00:00");
        HBox timeBox = new HBox(pastTime, slash, totalTime);
        topGrid.add(timeBox, 3, 3);
    }

    private void setParameter(GridPane topGrid) {
        topGrid.add(new Label("WaitMultiplier:"), 0, 1);
        topGrid.add(new Label("StartDelay:"), 2, 1);
        waitField = new TextField();
        waitField.setTextFormatter(new TextFormatter<>(new NumberStringConverter("#.##")));
        waitField.setText("1.00");
        topGrid.add(waitField, 1, 1);

        delayField = new TextField();
        delayField.setTextFormatter(new TextFormatter<>(new NumberStringConverter("#")));
        delayField.setText("3");
        topGrid.add(delayField, 3, 1);
    }

    private void setTitle(GridPane topGrid) {
        Text sceneTitle = new Text("Setting");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        topGrid.add(sceneTitle, 0, 0);
    }

    private static PlayConfig getConfig() {
        String text = delayField.getText();
        int startDelay = text.isEmpty() ? 0 : Integer.parseInt(text);
        String waitText = waitField.getText();
        double waitMultiplier = waitText.isEmpty() ? 0 : Double.parseDouble(waitText);
        return PlayConfig.builder().startDelay(startDelay).waitMultiplier(waitMultiplier).midiParser(midiParser).build();
    }

    @Synchronized("instrumentItems")
    private static void resetInstrumentsAndPlayer() {
        List<String> instruments = midiParser.getShownInstruments();
        List<Player> preparedPlayers = new ArrayList<>(players.size() + 1);
        preparedPlayers.addAll(players);
        preparedPlayers.add(0, Player.EMPTY);
        if (!instrumentItems.isEmpty()) {
            for (Node node : instrumentItems) {
                HBox box = (HBox) node;
                ComboBox<Player> comboBox = (ComboBox<Player>) box.getChildren().get(1);
                ObservableList<Player> items = comboBox.getItems();
                items.remove(0, items.size());
                items.addAll(preparedPlayers);
            }
        } else {
            instrumentItems.addAll(instruments.stream().map(i -> buildInstrumentBox(i, preparedPlayers)).collect(toList()));
        }
        instrumentView.edit(0);
    }

    private void setGridAttr(GridPane grid) {
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
    }

    private static HBox buildInstrumentBox(String instrument, List<Player> players) {
        ComboBox<Player> playerChoose = new ComboBox<>();
        playerChoose.getItems().addAll(players);
        return new HBox(10, new Text(instrument), playerChoose);
    }

    public static void setPastTime(double mills, double progress) {
        pb.setProgress(progress);
        pastTime.setText(millToTime(mills));
    }

    public static void setTotalTime(double mills) {
        totalTime.setText(millToTime(mills));
    }

    private static String millToTime(double mills) {
        return LocalTime.ofSecondOfDay((long) Math.ceil(mills / 1000)).toString();
    }

    @Synchronized("players")
    public static void notifyPlayersRefresh(List<Player> newPlayers) {
        Map<String, Player> map = newPlayers.stream().collect(toMap(Player::getId, Function.identity()));
        boolean flagRemove = players.removeIf(player -> !map.containsKey(player.getId()));
        boolean flagAdd = players.addAll(newPlayers);
        if ((flagRemove || flagAdd)
                && executor != null && executor.stat != DirectorExecutor.Stat.RUNNING
                && instrumentItems != null && !instrumentItems.isEmpty()) {
            Platform.runLater(DirectorWindow::resetInstrumentsAndPlayer);
        }
    }
}