import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Objects;

public class EnemyBullet extends Bullet {

    double speed;

    public EnemyBullet(double x, double y, int type, double speed){
        this.x = x;
        this.y = y;
        try {
            switch (type) {
                case 1 -> {
                    image = new Image("images/bullet1.png");
                    sound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("sounds/fastinvader1.wav")).toExternalForm()));
                }
                case 2 -> {
                    image = new Image("images/bullet2.png");
                    sound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("sounds/fastinvader2.wav")).toExternalForm()));
                }
                case 3 -> {
                    image = new Image("images/bullet3.png");
                    sound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("sounds/fastinvader3.wav")).toExternalForm()));
                }
            }
            sound.setCycleCount(1);
            view = new ImageView(image);
        } catch (Exception e){
            System.out.println("fail to load media components of enemy bullet: " + e);
        }
        this.speed = speed;
    }

    public boolean move(){
        if (y + speed < 600){
            try {
                view.setY(y+speed);
            } catch (Exception e){
                System.out.println("fail to set enemy bullet position: " + e);
            }
            y += speed;
            return true;
        } else{
            return false;
        }
    }
}
