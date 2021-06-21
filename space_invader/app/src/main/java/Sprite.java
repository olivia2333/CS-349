import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Sprite {
    protected double x, y;
    protected Image image;
    protected ImageView view = new ImageView();

    public double get_x(){
        return x;
    }

    public double get_y() {
        return y;
    }

    public void set_x(double x){ this.x = x; }

    public void draw(){
        try {
            view.setX(x);
            view.setY(y);
            view.setFitWidth(image.getWidth() / 2.5);
            view.setFitHeight(image.getHeight() / 2.5);
        } catch (Exception e){
            System.out.println("fail to set view: " + e);
        }
    }

    public boolean contains(Point2D point){
        try {
            return point.getX() >= x && point.getX() <= x + image.getWidth() / 2.5
                    && point.getY() >= y && point.getY() <= y + image.getHeight() / 2.5;
        } catch (Exception e){
            System.out.println("contains fail to get position: " + e);
        }
        return false;
    }
}
