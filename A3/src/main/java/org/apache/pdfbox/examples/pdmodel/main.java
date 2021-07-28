package org.apache.pdfbox.examples.pdmodel;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

public class main extends Application {
    final int screen_width = 800;
    final int screen_height = 800;
    double previous_x, previous_y;
    Sprite selectedSprite;

    @Override
    public void start(Stage primaryStage) {
        MenuBar menubar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem reset = new MenuItem("Reset");
        MenuItem quit = new MenuItem("Quit");
        MenuItem save = new MenuItem("Save");
        fileMenu.getItems().addAll(reset, quit, save);

        quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        reset.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        quit.setOnAction(actionEvent -> {
            System.exit(0);
        });

        menubar.getMenus().add(fileMenu);

        Canvas canvas = new Canvas(screen_width, screen_height);
        Group g_root = new Group();
        Scene scene = new Scene(g_root, screen_width, screen_height);

        Sprite root = createSprites();
        g_root.getChildren().addAll(canvas, menubar);

        reset.setOnAction(actionEvent -> {
            try {
                root.reset();
                draw(canvas, root);
            } catch (NonInvertibleTransformException e) {
                e.printStackTrace();
            }
        });
        save.setOnAction(actionEvent -> {
            WritableImage i = canvas.snapshot(new SnapshotParameters(), null);
            BufferedImage bi = SwingFXUtils.fromFXImage((Image) i, null);
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                try{
                    ImageIO.write(bi, "txt", file);
                    System.out.println("save");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("no file chosen");
            }
        });

        canvas.setOnMousePressed(mouseEvent ->{
            Sprite hit = root.getSpriteHit(mouseEvent.getX(), mouseEvent.getY());
            if (hit != null) {
                selectedSprite = hit;
                System.out.println("Selected " + selectedSprite);
                previous_x = mouseEvent.getX();
                previous_y = mouseEvent.getY();
            }
        });

        canvas.setOnMouseDragged(mouseEvent -> {
            if (selectedSprite != null) {
                double x = mouseEvent.getX();
                double y = mouseEvent.getY();
                double dx = x - previous_x;
                double dy = y - previous_y;
                if (selectedSprite.get_type().equals("torso")) {
                    // translate shape to follow the mouse cursor
                    selectedSprite.translate(dx, dy);

                    // draw tree in new position
                    draw(canvas, root);
                    System.out.println(".. moved "
                            + selectedSprite.toString()
                            + " from (" + previous_x + "," + previous_y + ")"
                            + " to (" + mouseEvent.getX() + "," + mouseEvent.getY() + ")"
                            + " -- dx: " + dx + ", dy: " + dy);

                    // save coordinates for next event
                } else if (selectedSprite.get_type().equals("feet") || selectedSprite.get_type().equals("hand") ||
                        selectedSprite.get_type().equals("head") || selectedSprite.get_type().equals("lower_leg")) {
                    System.out.println(dx);
                    System.out.println(dy);

                    double offsetX = selectedSprite.get_relative_x(new Point2D(x, y));
                    double offsetY = selectedSprite.get_relative_y(new Point2D(x, y));

                    double offsetPrevX = selectedSprite.get_relative_x(new Point2D(previous_x, previous_y));
                    double offsetPrevY = selectedSprite.get_relative_y(new Point2D(previous_x, previous_y));

                    double curr_degree = Math.atan2(offsetY, offsetX);
                    double prev_degree = Math.atan2(offsetPrevY, offsetPrevX);

                    double theta = curr_degree - prev_degree;
                    theta = Math.toDegrees(theta);

                    try {
                        selectedSprite.rotate(theta, selectedSprite.get_pivot());
                    } catch (NonInvertibleTransformException e) {
                        e.printStackTrace();
                    }
                    draw(canvas, root);
                }
                /*if (selectedSprite.get_type().equals("upper_leg") ||selectedSprite.get_type().equals("lower_leg")){
                    double x_var = -dx/(selectedSprite.get_relative_x(new Point2D(previous_x, previous_y)));
                    double y_var = -dy/(selectedSprite.get_relative_y(new Point2D(previous_x, previous_y)));
                    double sx = Math.abs(x_var + 1);
                    double sy = Math.abs(y_var + 1);
                    if (sx <= 0) sx = 1;
                    if (sy <= 0) sy = 1;
                    try {
                        selectedSprite.scale(sx, sy);
                    } catch (NonInvertibleTransformException e) {
                        e.printStackTrace();
                    }
                    draw(canvas, root);
                }*/
                previous_x = x;
                previous_y = y;
            }
        });

        // un-selects any selected shape
        canvas.setOnMouseReleased( mouseEvent -> selectedSprite = null);

        // draw the sprites on the canvas
        draw(canvas, root);

        // show the scene including the canvas
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void draw(Canvas canvas, Sprite root) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        root.draw(gc);
    }

    private Sprite createSprites(){
        Sprite torso = new Torso(250, 200,250, 250);
        Sprite left_upper_arm = new UpperArm(26, 18,68, 80, true);
        Sprite right_upper_arm = new UpperArm(156, 12,67, 80, false);
        Sprite left_upper_leg = new UpperLeg(78, 238,45, 105, true);
        Sprite right_upper_leg = new UpperLeg(130, 240,45, 100, false);
        Sprite head = new Head(90, -63, 65, 65);
        Sprite left_lower_arm = new LowerArm(-20, 58,35, 50, true);
        Sprite right_lower_arm = new LowerArm(53, 55,52, 52, false);
        Sprite left_lower_leg = new LowerLeg(10, 98,30, 100, true);
        Sprite right_lower_leg = new LowerLeg(0, 96,28, 100, false);
        Sprite left_feet = new Feet(0, 98,28, 50);
        Sprite right_feet = new Feet(0,98,30, 50);
        Sprite left_hand = new Hand(-13, 27,22, 22, true);
        Sprite right_hand = new Hand(40, 35,22,22, false);

        torso.translate(250, 200);
        head.translate(90, -63);
        left_upper_arm.translate(26, 18);
        right_upper_arm.translate(156, 12);
        right_upper_leg.translate(130, 240);
        left_upper_leg.translate(78, 238);

        left_lower_arm.translate(-20,58);
        right_lower_arm.translate(53,55);
        left_lower_leg.translate(10, 98);
        right_lower_leg.translate(0, 96);

        left_hand.translate(-13, 27);
        right_hand.translate(40, 35);
        left_feet.translate(0,98);
        right_feet.translate(0,98);

        torso.addChild(left_upper_arm);
        torso.addChild(right_upper_arm);
        torso.addChild(left_upper_leg);
        torso.addChild(right_upper_leg);
        torso.addChild(head);

        left_upper_arm.addChild(left_lower_arm);
        right_upper_arm.addChild(right_lower_arm);
        left_upper_leg.addChild(left_lower_leg);
        right_upper_leg.addChild(right_lower_leg);

        left_lower_arm.addChild(left_hand);
        right_lower_arm.addChild(right_hand);
        right_lower_leg.addChild(right_feet);
        left_lower_leg.addChild(left_feet);

        return torso;
    }
}
