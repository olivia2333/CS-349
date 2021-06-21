import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Objects;

public class Enemy extends Sprite {
    public static final double ENEMY_SPEED = 0.5;
    public static final double ENEMY_VERTICAL_SPEED = 33.0;
    public static final double ENEMY1_BULLET_SPEED = 4.0;
    public static final double ENEMY2_BULLET_SPEED = 5.0;
    public static final double ENEMY3_BULLET_SPEED = 6.0;
    private final int type;

    public static double x_speed;
    private double dx, bullet_speed;

    MediaPlayer die_sound;

    public Enemy(int x, int y, int type, int level){
        this.x = x;
        this.y = y;
        this.type = type;
        try {
            switch (type) {
                case 1 -> {
                    this.image = new Image("images/enemy1.png");
                    bullet_speed = ENEMY1_BULLET_SPEED;
                }
                case 2 -> {
                    this.image = new Image("images/enemy2.png");
                    bullet_speed = ENEMY2_BULLET_SPEED;
                }
                case 3 -> {
                    this.image = new Image("images/enemy3.png");
                    bullet_speed = ENEMY3_BULLET_SPEED;
                }
            }
            view = new ImageView(this.image);
            die_sound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("sounds/explosion.wav")).toExternalForm()));
        } catch (Exception e){
            System.out.println("cannot load image " + e);
        }

        switch (level) {
            case 1 -> {
                dx = ENEMY_SPEED / 5;
                x_speed = ENEMY_SPEED / 5;
            }
            case 2 -> {
                dx = ENEMY_SPEED / 2.5;
                x_speed = ENEMY_SPEED / 2.5;
            }
            case 3 -> {
                dx = ENEMY_SPEED / 1.25;
                x_speed = ENEMY_SPEED / 1.25;
            }
        }

        die_sound.setCycleCount(1);
    }

    public double get_lower_x(){
        try{
            return x + image.getWidth()/2.5;
        } catch (Exception e){
            System.out.println("fail to get width: " + e);
        }
        return 0;
    }

    public double get_lower_y(){
        try{
            return y + image.getHeight()/2.5;
        } catch (Exception e){
            System.out.println("fail to get width: " + e);
        }
        return 0;
    }

    public void die(){
        try {
            die_sound.play();
        } catch (Exception e){
            System.out.println("fail to play enemy die sound: " + e);
        }
    }

    public boolean move(double x_direction, double y_direction){
        if (dx < 0){
            dx = -x_speed;
        } else {
            dx = x_speed;
        }
        dx *= x_direction;
        try {
            view.setX(x + dx);
        } catch (Exception e){
            System.out.println("fail to set enemy position: " + e);
        }
        x += dx;
        if (y_direction != 0){
            if (y + ENEMY_VERTICAL_SPEED < 580){
                view.setY(y + ENEMY_VERTICAL_SPEED);
                y += ENEMY_VERTICAL_SPEED;
            } else {
                return false;
            }
        }
        return true;
    }

    public static void accelerate(){
        x_speed *= 1.05;
    }

    public int getType(){ return type;  }

    public EnemyBullet shot(){
        EnemyBullet bullet;
        if (type == 1){
            bullet = new EnemyBullet(x + 24, y + 33, type, bullet_speed);
        } else if (type == 2){
            bullet = new EnemyBullet(x + 22, y + 33, type, bullet_speed);
        } else {
            bullet = new EnemyBullet(x + 18, y + 33, type, bullet_speed);
        }
        bullet.draw();
        return bullet;
    }
}
