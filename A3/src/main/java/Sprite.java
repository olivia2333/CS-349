import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import java.util.Vector;

/**
 * A building block for creating your own shapes
 * These explicitly support parent-child relationships between nodes
 */

public abstract class Sprite {
    protected double x, y, w, h, sx, sy, curr_height;
    static int spriteID = 0;
    final String localID;
    protected double degree = 0;
    protected double scale_factor = 1;
    protected Image image;
    protected boolean left;

    protected Sprite parent = null;
    public Affine smatrix = new Affine();
    public Affine rmatrix = new Affine();
    public Affine tmatrix = new Affine();

    protected Vector<Sprite> children = new Vector<>();
    protected Point2D initial_pivot, pivot;

    public Sprite() {
        localID = String.valueOf(++spriteID);
    }

    // maintain hierarchy
    public void addChild(Sprite s) {
        children.add(s);
        s.setParent(this);
    }

    private void setParent(Sprite s) {
        this.parent = s;
    }

    public abstract Point2D get_pivot();

    public double get_relative_x(Point2D p){
        try {
            Point2D pointAtOrigin = getFullMatrix().createInverse().transform(p);
            return pointAtOrigin.getX() - pivot.getX();
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void reset() throws NonInvertibleTransformException {
        smatrix = new Affine();
        tmatrix = new Affine();
        rmatrix = new Affine();
        for (Sprite child: children){
            child.reset();
        }

        translate(sx, sy);
    }

    public double get_relative_y(Point2D p){
        try {
            Point2D pointAtOrigin = getFullMatrix().createInverse().transform(p);
            return pointAtOrigin.getY() - pivot.getY();
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected abstract boolean valid_rotation();

    // transformations
    // these will pre-concat to the sprite's affine matrix
    protected void translate(double dx, double dy) {
        tmatrix.appendTranslation(dx, dy);
    }

    protected abstract String get_type();

    void rotate(double theta) throws NonInvertibleTransformException {
        Point2D p1 = getFullMatrix().createInverse().transform(get_pivot());
        degree += theta;

        if (valid_rotation()) {
            rmatrix.prependRotation(theta, p1.getX(), p1.getY());
            rotate_children(theta, get_pivot(), p1);

        } else {
            degree -= theta;
        }
    }

    void rotate_children(double theta, Point2D p, Point2D p1) throws NonInvertibleTransformException {
        for (Sprite sprite:children){
            sprite.rmatrix.prependRotation(-theta, p1.getX(), p1.getY());
            Point2D temp = sprite.getFullMatrix().createInverse().transform(p);
            sprite.rmatrix.appendRotation(theta, temp.getX(), temp.getY());
            sprite.rotate_children(theta, p, temp);
        }
    }

    void scale(double sy) throws NonInvertibleTransformException {
        Affine fullMatrix = getSmatrix();
        Affine inverse = fullMatrix.createInverse();
        scale_factor *= sy;
        Point2D p = get_pivot();
        // move to the origin, rotate and move back
        if (scale_factor <= 1.75 && scale_factor >= 0.75) {
            smatrix.prepend(inverse);
            Point2D temp = getFullMatrix().createInverse().transform(p);
            smatrix.prependScale(1, 1/sy, temp.getX(), temp.getY());
            p = getFullMatrix().transform(temp.add(0, h/4));
            smatrix.prepend(fullMatrix);
            scale_children(sy, p, temp);
            unscale_feet(sy);
        } else {
            scale_factor/=sy;
        }

    }

    protected void scale_children(double sy, Point2D p, Point2D p1) throws NonInvertibleTransformException {
        for (Sprite sprite:children){
            sprite.smatrix.prependScale(1, sy, p1.getX(), p1.getY());
            Point2D temp = sprite.getFullMatrix().createInverse().transform(p);
            sprite.smatrix.appendScale(1, 1/sy, temp.getX(), temp.getY());
            if (sprite.get_type().equals("feet")){
                sprite.smatrix.appendScale(1, sy, temp.getX(), temp.getY());
            }
            p = getFullMatrix().transform(temp.add(0, 0));
            sprite.scale_children(sy, p, temp);
        }
    }

    protected void unscale_feet(double sy) {

       /* for (Sprite sprite : children) {
            if (sprite.get_type().equals("feet")) {
                sprite.smatrix.prependScale(1, sy, 0, h/4);
            } else {
                sprite.unscale_feet(sy);
            }
        }*/
    }

    Affine getSmatrix(){
        Affine s = smatrix.clone();
        if (parent != null){
            s.prepend(parent.getSmatrix());
        }
        return s;
    }
    Affine getRmatrix(){
        Affine r = rmatrix.clone();
        if (parent != null){
            r.prepend(parent.getRmatrix());
        }
        return r;
    }
    Affine getTmatrix(){
        Affine t = tmatrix.clone();
        if (parent != null){
            t.prepend(parent.getTmatrix());
        }
        return t;
    }

    Affine getFullMatrix() {
        Affine smatrix = getSmatrix().clone();
        Affine rmatrix = getRmatrix().clone();
        Affine tmatrix = getTmatrix().clone();
        smatrix.prepend(rmatrix);
        smatrix.prepend(tmatrix);
        return smatrix;
    }

    Affine getSRMatrix(){
        Affine smatrix = getSmatrix().clone();
        Affine rmatrix = getRmatrix().clone();
        smatrix.prepend(rmatrix);
        return smatrix;
    }

    // hit tests
    // these cannot be handled in the base class, since the actual hit tests are dependent on the type of shape
    protected abstract boolean contains(Point2D p);
    protected boolean contains(double x, double y) {
        return contains(new Point2D(x, y));
    }

    // we can walk the tree from the base class, since we rely on the specific sprites to check containment
    protected Sprite getSpriteHit(double x, double y) {

        // if no match above, recurse through children and return the first hit
        // assumes no overlapping shapes
        for (Sprite sprite : children) {
            Sprite hit = sprite.getSpriteHit(x, y);
            if (hit != null) return hit;
        }
        // check me first...
        if (this.contains(x, y)) {
            return this;
        }

        return null;
    }

    // drawing method
    protected abstract void draw(GraphicsContext gc);

    protected void rotate(GraphicsContext gc){}

    // debugging
    public String toString() { return "Sprite " + localID; }
}
