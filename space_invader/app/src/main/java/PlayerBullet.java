import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Objects;

public class PlayerBullet extends Bullet {
    public static final double PLAYER_BULLET_SPEED = 6.0;

    public PlayerBullet(double x, double y){
        this.x = x;
        this.y = y;

        try {
            image = new Image("images/player_bullet.png");
            view = new ImageView(image);
        } catch (Exception e){
            System.out.println("cannot load image \"player_bullet.png\" " + e);
        }

        try {
            sound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("sounds/shoot.wav")).toExternalForm()));
            sound.setCycleCount(1);
        } catch (Exception e){
            System.out.println("cannot load music \"shoot.wav\" " + e);
        }
    }

    public boolean move(){
        if (y - PLAYER_BULLET_SPEED > 0){
            try {
                view.setY(y-PLAYER_BULLET_SPEED);
            } catch (Exception e){
                System.out.println("fail to set player bullet position: " + e);
            }
            y-=PLAYER_BULLET_SPEED;
            return true;
        } else{
            return false;
        }
    }
}
