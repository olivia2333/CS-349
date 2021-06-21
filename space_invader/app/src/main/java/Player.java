import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player extends Sprite {
    public static final double PLAYER_SPEED = 7.0;

    long last_fire_time = 0;
    long last_2_fire_time = 0;

    public Player(int x, int y){
        this.x = x;
        this.y = y;
        try {
            image = new Image("images/player.png");
            view = new ImageView(image);
        } catch (Exception e){
            System.out.println("cannot load image \"player.png\" " + e);
        }
    }

    public void move(boolean left){
        double direction;
        if (left) direction = -PLAYER_SPEED;
        else direction = PLAYER_SPEED;
        if (x + direction> 8 && x + direction < 738) {
            try {
                view.setX(x+direction);
            } catch (Exception e){
                System.out.println("fail to set player x position: " + e);
            }
            x+=direction;
        }
    }

    public PlayerBullet fire(){
        boolean canFire = false;
        if (last_fire_time == 0){
            last_fire_time = System.currentTimeMillis();
            canFire = true;
        } else if (last_2_fire_time == 0) {
            last_2_fire_time = last_fire_time;
            last_fire_time = System.currentTimeMillis();
            canFire = true;
        } else {
            long now = System.currentTimeMillis();
            if ((now - last_fire_time) / 1000F >= 0.1 && (now - last_2_fire_time) / 1000F >= 1){
                canFire = true;
                last_2_fire_time = last_fire_time;
                last_fire_time = now;
            }
        }
        if (canFire) {
            PlayerBullet bu = new PlayerBullet(x + 25, y);
            bu.draw();
            return bu;
        } else {
            return null;
        }
    }
}
