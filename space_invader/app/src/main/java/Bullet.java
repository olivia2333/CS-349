import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;

public abstract class Bullet {
    protected double x, y;
    Image image;
    ImageView view = new ImageView();
    MediaPlayer sound;

    public double get_x(){ return x; }

    public double get_y() {
        return y;
    }

    void draw(){
        try {
            view.setX(x);
            view.setY(y);
            view.setFitWidth(image.getWidth() / 3);
            view.setFitHeight(image.getHeight() / 3);
            sound.play();
        } catch (Exception e){
            System.out.println("fail to draw bullet: " + e);
        }
    }
}
