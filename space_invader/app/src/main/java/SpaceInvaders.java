import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SpaceInvaders extends Application {
    Scene main, game;
    enum SCENES {GAME1, GAME2, GAME3}
    Stage Menu, Game;
    Group game_root;

    MediaView mediaView;
    Background back;
    AnimationTimer timer;

    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<EnemyBullet> en_bullets = new ArrayList<>();
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<PlayerBullet> pl_bullets = new ArrayList<>();
    Player player;

    int curr_level, curr_score = 0;
    int curr_live = 2;
    Label lives, score, level;

    @Override
    public void start(Stage primaryStage) {
        Menu = new Stage();
        Menu.setTitle("Space Invaders main menu");

        Group main_root = new Group();
        VBox pane = new VBox(30);
        pane.prefWidthProperty().bind(Menu.widthProperty().multiply(1));
        pane.prefHeightProperty().bind(Menu.heightProperty().multiply(1));
        pane.setPadding(new Insets(30,0,0,0));
        pane.setAlignment(Pos.TOP_CENTER);

        // add background
        try {
            Image background = new Image("images/main_background.jpg");
            BackgroundSize backgroundSize = new BackgroundSize(800, 600, true, true, true, false);
            BackgroundImage backgroundImage = new BackgroundImage(background, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                    backgroundSize);
            back = new Background(backgroundImage);
            pane.setBackground(back);
        } catch (Exception e){
            System.out.println("fail to load background \"main_background.jpg\" " + e);
        }

        // add background music
        try {
            Media media = new Media(Objects.requireNonNull(getClass().getResource("sounds/spaceinvaders1.mp3")).toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaView = new MediaView(mediaPlayer);
            main_root.getChildren().add(mediaView);
        } catch (Exception e){
            System.out.println("fail to load media \"spaceinvaders1.mp3\" " + e);
        }

        // add logo
        try {
            Image logo = new Image("images/logo.png");
            ImageView iv1 = new ImageView();
            iv1.setImage(logo);
            iv1.setPreserveRatio(true);
            pane.getChildren().add(iv1);
        } catch (Exception e){
            System.out.println("fail to load image \"logo.png\" " + e);
        }

        // add instructions and names
        VBox ins_box = new VBox(5);
        ins_box.setPadding(new Insets(110,0,0,0));
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.8, 0.8, 0.7));
        Text t1 = new Text("Instructions");
        t1.setEffect(ds);
        t1.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        t1.setFill(Color.OLIVE);
        Text ins = new Text("ENTER - Start Game\nA or <, D or > - Move ship left or right\nSPACE - Fire!\nQ - Quit Game\n1 or 2 or 3 - Start Game at a specific level");
        ins.setEffect(ds);
        ins.setFont(Font.font("Verdana", 17));
        ins.setFill(Color.OLIVE);
        ins_box.getChildren().addAll(t1, ins);
        ins_box.setAlignment(Pos.BOTTOM_CENTER);
        VBox name_box = new VBox();
        name_box.setAlignment(Pos.BOTTOM_LEFT);
        name_box.setPadding(new Insets(15,0,0,20));
        Text name = new Text("Shiqi Ma 20786132");
        name.setEffect(ds);
        name.setFont(Font.font("Verdana", 18));
        name.setFill(Color.OLIVEDRAB);
        name_box.getChildren().add(name);
        pane.getChildren().addAll(ins_box, name_box);
        main_root.getChildren().addAll(pane);

        // set menu input
        main = new Scene(main_root, 800, 600);
        main.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Q -> {
                    Menu.close();
                    System.exit(0);
                }
                case DIGIT1, ENTER -> {
                    curr_level = 1;
                    setScene(Menu, SCENES.GAME1);
                }
                case DIGIT2 -> {
                    curr_level = 2;
                    setScene(Menu, SCENES.GAME2);
                }
                case DIGIT3 -> {
                    curr_level = 3;
                    setScene(Menu, SCENES.GAME3);
                }
            }
        });

        Menu.setScene(main);
        Menu.setResizable(false);
        Menu.show();
    }

    void setLevels(){
        game_root = new Group();
        if (mediaView != null) {
            game_root.getChildren().add(mediaView);
        }

        try {
            Canvas canvas = new Canvas(800, 600);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.drawImage(new Image("images/main_background.jpg"), 0, 0);
            game_root.getChildren().add(canvas);
        } catch (Exception e){
            System.out.println("fail to add image \"main_background.jpg\" " + e);
        }

        // add enemies
        enemies = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            for (int j = 0; j < 10; j++){
                int type = new Random().nextInt(3) + 1;
                Enemy en = new Enemy(50+55*j, 30+40*i, type, curr_level);
                enemies.add(en);
                en.draw();
                game_root.getChildren().add(en.view);
            }
        }

        // add 3 players
        player = new Player(350, 550);
        players.add(player);
        players.add(new Player(new Random().nextInt(550), 550));
        players.add(new Player(new Random().nextInt(550), 550));
        try {
            player.draw();
            game_root.getChildren().add(player.view);
        } catch(Exception e){
            System.out.println("failed to add player view: " + e);
        }

        // display information
        HBox texts = new HBox();
        texts.setSpacing(185);
        lives = new Label("LIVES: " + curr_live);
        score = new Label("SCORE: " + curr_score);
        level = new Label("LEVEL: " + curr_level);
        lives.setTextFill(Color.WHITE);
        score.setTextFill(Color.WHITE);
        level.setTextFill(Color.WHITE);
        Font font = Font.font("Verdana", FontWeight.BOLD, 22);
        lives.setFont(font);
        score.setFont(font);
        level.setFont(font);
        texts.getChildren().addAll(score, lives, level);
        game_root.getChildren().add(texts);

        game = new Scene(game_root, 800, 600);
    }

    void setScene(Stage stage, SCENES scene) {
        Game = new Stage();
        Game.setResizable(false);
        setLevels();
        switch (scene) {
            case GAME1 -> {
                Game.setTitle("level 1");
                Game.setScene(game);
            }
            case GAME2 -> {
                Game.setTitle("level 2");
                Game.setScene(game);
            }
            case GAME3 -> {
                Game.setTitle("level 3");
                Game.setScene(game);
            }
        }
        stage.close();
        Game.show();
        level_indicator();

        game_progress();
    }

    void game_progress(){
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                redraw_enemy();
                shot(true);
                update_status();
            }
        };
        timer.start();

        game.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case A, LEFT -> player.move(true);
                case D, RIGHT -> player.move(false);
                case SPACE -> {
                    PlayerBullet pl_bullet = player.fire();
                    if (pl_bullet != null) {
                        try {
                            pl_bullets.add(pl_bullet);
                            game_root.getChildren().add(pl_bullet.view);
                        } catch (Exception e){
                            System.out.println("failed to add player bullet view: " + e);
                        }
                    }
                }
                case Q -> {
                    Game.close();
                    System.exit(0);
                }
            }
        });
    }

    void redraw_enemy(){
        double x, y;
        double max_right = 0;
        double min_left = 900;
        for (Enemy en : enemies){
            if (en.get_x() > max_right) max_right = en.get_x();
            if (en.get_x() < min_left) min_left = en.get_x();
        }

        if (max_right > 740 || min_left < 5) {x=-1.0f; y=1.0f; shot(false);}
        else {x = 1.0f; y = 0;}

        for (Enemy en : enemies){
            if (!en.move(x, y)) lose();
        }
    }

    void shot(boolean random){
        int curr_size = enemies.size();
        int rand, fps_per_bullet = 0;
        switch (curr_level){
            case 1 -> fps_per_bullet = 240;
            case 2 -> fps_per_bullet = 210;
            case 3 -> fps_per_bullet = 180;
        }
        if (random) curr_size *= fps_per_bullet;
        rand = new Random().nextInt(curr_size);
        if (rand < enemies.size()){
            EnemyBullet en_bullet = enemies.get(rand).shot();
            try {
                en_bullets.add(en_bullet);
                game_root.getChildren().add(en_bullet.view);
            } catch (Exception e){
                System.out.println("failed to add enemy bullet view " + e);
            }
        }
    }

    void update_status(){
        check_hit_enemy();
        check_hit_player();
        redraw_bullet();
    }

    void check_hit_enemy(){
        Iterator<Enemy> ite_en = enemies.iterator();

        while (ite_en.hasNext()){
            Enemy en = ite_en.next();
            boolean destroy = false;
            Iterator<PlayerBullet> ite_pl_bu = pl_bullets.iterator();
            while (ite_pl_bu.hasNext()){
                PlayerBullet pl_bu = ite_pl_bu.next();
                if (en.contains(new Point2D(pl_bu.get_x(), pl_bu.get_y()))){
                    destroy = true;
                    switch (en.getType()) {
                        case 1 -> curr_score += 10;
                        case 2 -> curr_score += 20;
                        case 3 -> curr_score += 30;
                    }
                    score.setText("SCORE: " + curr_score);
                    game_root.getChildren().remove(pl_bu.view);
                    ite_pl_bu.remove();
                    break;
                }
            }
            if (destroy){
                game_root.getChildren().remove(en.view);
                ite_en.remove();
                en.die();
                if (enemies.size() < 1){
                    timer.stop();
                    victory();
                } else {
                    Enemy.accelerate();
                }
            }
        }
    }

    void check_hit_player(){
        Iterator<Enemy> ite_en = enemies.iterator();
        while (ite_en.hasNext()){
            Enemy en = ite_en.next();
            if (player.contains(new Point2D(en.get_x(), en.get_y()))
            || player.contains(new Point2D(en.get_x(), en.get_lower_y()))
            || player.contains(new Point2D(en.get_lower_x(), en.get_y()))
            || player.contains(new Point2D(en.get_lower_x(), en.get_lower_y()))){
                curr_live--;
                life_indicator();
                game_root.getChildren().remove(player.view);
                Iterator<PlayerBullet> ite_pl_bu = pl_bullets.iterator();
                while (ite_en.hasNext()){
                    PlayerBullet pl_bu = ite_pl_bu.next();
                    game_root.getChildren().remove(pl_bu.view);
                    ite_en.remove();
                }
                players.remove(0);
                if (players.size() < 1 || curr_live == -1){
                    timer.stop();
                    lose();
                } else {
                    lives.setText("LIVES: " + curr_live);
                    player = players.get(0);
                    random_player_position();
                    player.draw();
                    game_root.getChildren().add(player.view);
                }
            }
        }

        for (EnemyBullet en_bullet : en_bullets) {
            if (player.contains(new Point2D(en_bullet.get_x(), en_bullet.get_y()))) {
                curr_live--;
                life_indicator();
                try {
                    game_root.getChildren().remove(player.view);
                    for (PlayerBullet pl_bu : pl_bullets) {
                        game_root.getChildren().remove(pl_bu.view);
                    }
                } catch (Exception e){
                    System.out.println("fail to remove view: " + e);
                }
                players.remove(0);
                if (players.size() < 1 || curr_live == -1) {
                    timer.stop();
                    lose();
                } else {
                    lives.setText("LIVES: " + curr_live);
                    player = players.get(0);
                    random_player_position();
                    try {
                        player.draw();
                        game_root.getChildren().add(player.view);
                    } catch (Exception e){
                        System.out.println("failed to add player view: " + e);
                    }
                }
            }
        }
    }

    void redraw_bullet(){
        Iterator<EnemyBullet> ite_en_bu = en_bullets.iterator();
        while (ite_en_bu.hasNext()){
            EnemyBullet en_bu = ite_en_bu.next();
            if (!en_bu.move()){
                try {
                    game_root.getChildren().remove(en_bu.view);
                } catch (Exception e){
                    System.out.println("failed to remove enemy bullet view: " + e);
                }
                ite_en_bu.remove();
            }
        }

        Iterator<PlayerBullet> ite_pl_bu = pl_bullets.iterator();
        while (ite_pl_bu.hasNext()){
            PlayerBullet pl_bu = ite_pl_bu.next();
            if (!pl_bu.move()){
                try {
                    game_root.getChildren().remove(pl_bu.view);
                } catch (Exception e){
                    System.out.println("failed to remove player bullet view: " + e);
                }
                ite_pl_bu.remove();
            }
        }
    }

    void random_player_position(){
        double pl_x = 0;
        boolean set = false;
        while (!set) {
            set = true;
            pl_x = new Random().nextDouble() * 730 + 35;
            for (Enemy enemy : enemies) {
                if (enemy.contains(new Point2D(pl_x, player.get_y()))) {
                    set = false;
                    break;
                }
            }
        }
        player.set_x(pl_x);
    }

    void level_indicator(){
        Label level_indicator = new Label();
        level_indicator.setMinWidth(200);
        level_indicator.setMinHeight(200);
        level_indicator.setLayoutX(275);
        level_indicator.setLayoutY(200);
        level_indicator.setAlignment(Pos.CENTER);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.5, 0.5, 0.5));
        level_indicator.setEffect(ds);
        level_indicator.setText("Level " + curr_level);
        level_indicator.setTextFill(Color.WHITE);
        level_indicator.setFont(Font.font("Arial", FontWeight.BOLD,40));
        game_root.getChildren().add(level_indicator);

        FadeTransition ft = new FadeTransition(Duration.millis(1500), level_indicator);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        FadeTransition fd = new FadeTransition(Duration.millis(3000), level_indicator);
        fd.setFromValue(1.0);
        fd.setToValue(0.0);
        fd.play();
    }

    void life_indicator(){
        Label life_indicator = new Label();
        life_indicator.setMinWidth(200);
        life_indicator.setMinHeight(200);
        life_indicator.setLayoutX(245);
        life_indicator.setLayoutY(200);
        life_indicator.setAlignment(Pos.CENTER);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.5, 0.5, 0.5));
        life_indicator.setEffect(ds);
        life_indicator.setText("Lives remaining: " + curr_live);
        life_indicator.setTextFill(Color.WHITE);
        life_indicator.setFont(Font.font("Arial", FontWeight.BOLD,40));
        game_root.getChildren().add(life_indicator);

        FadeTransition ft = new FadeTransition(Duration.millis(1500), life_indicator);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        FadeTransition fd = new FadeTransition(Duration.millis(3000), life_indicator);
        fd.setFromValue(1.0);
        fd.setToValue(0.0);
        fd.play();
    }

    void game_end(){
        game.setOnKeyPressed(event -> {
            curr_score = 0;
            curr_live = 2;
            switch (event.getCode()) {
                case Q -> {
                    Game.close();
                    System.exit(0);
                }
                case DIGIT1, ENTER -> {
                    curr_level = 1;
                    setScene(Game, SCENES.GAME1);
                }
                case DIGIT2 -> {
                    curr_level = 2;
                    setScene(Game, SCENES.GAME2);
                }
                case DIGIT3 -> {
                    curr_level = 3;
                    setScene(Game, SCENES.GAME3);
                }
                case I -> {
                    Game.close();
                    Menu.show();
                }
            }
        });
    }

    void lose(){
        timer.stop();
        VBox lose = new VBox();
        lose.setMinWidth(500);
        lose.setMinHeight(300);
        lose.setLayoutX(150);
        lose.setLayoutY(150);
        lose.setAlignment(Pos.CENTER);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.8, 0.8, 0.7));
        lose.setEffect(ds);
        lose.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250), CornerRadii.EMPTY, Insets.EMPTY)));
        Text over = new Text("GAME OVER!");
        Text scores = new Text(score.getText());
        Text instruction = new Text("""


                ENTER - start a new game
                I - back to instructions
                Q - quit games
                1 or 2 or 3 - start new game at a specific level""");
        over.setFill(Color.BLACK);
        scores.setFill(Color.BLACK);
        instruction.setFill(Color.BLACK);
        over.setFont(Font.font("Arial", FontWeight.BOLD,40));
        scores.setFont(Font.font("Verdana", 20));
        instruction.setFont(Font.font("Verdana", 20));
        lose.getChildren().addAll(over, scores, instruction);
        game_root.getChildren().add(lose);
        game_end();
    }

    void victory(){
        if (curr_level == 3){
            VBox victory = new VBox();
            victory.setMinWidth(500);
            victory.setMinHeight(300);
            victory.setLayoutX(150);
            victory.setLayoutY(150);
            victory.setAlignment(Pos.CENTER);
            DropShadow ds = new DropShadow();
            ds.setOffsetY(3.0f);
            ds.setColor(Color.color(0.8, 0.8, 0.7));
            victory.setEffect(ds);
            victory.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250), CornerRadii.EMPTY, Insets.EMPTY)));
            Text over = new Text("VICTORY!!");
            Text scores = new Text(score.getText());
            Text instruction = new Text("""


                    ENTER - start a new game
                    I - back to instructions
                    Q - quit games
                    1 or 2 or 3 - start new game at a specific level""");
            over.setFill(Color.BLACK);
            scores.setFill(Color.BLACK);
            instruction.setFill(Color.BLACK);
            over.setFont(Font.font("Arial", FontWeight.BOLD,40));
            scores.setFont(Font.font("Verdana", 20));
            instruction.setFont(Font.font("Verdana", 20));
            victory.getChildren().addAll(over, scores, instruction);
            game_root.getChildren().add(victory);
            game_end();
        } else {
            Iterator<PlayerBullet> ite_pl_bu = pl_bullets.iterator();
            while (ite_pl_bu.hasNext()){
                PlayerBullet pl_bu = ite_pl_bu.next();
                game_root.getChildren().remove(pl_bu.view);
                ite_pl_bu.remove();
            }
            curr_level++;
            if (curr_level == 2){
                setScene(Game, SCENES.GAME2);
            } else {
                setScene(Game, SCENES.GAME3);
            }
        }
    }
}
