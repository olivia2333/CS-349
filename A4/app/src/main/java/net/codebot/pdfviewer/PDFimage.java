package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class PDFimage extends ImageView {

    final String LOGNAME = "pdf_image";

    // drawing path
    Path path = null;
    ArrayList<Pair<Float, Float>> p = null;
    ArrayList<Pair<Path, Integer>> paths1 = new ArrayList();
    ArrayList<Pair<Path, Integer>> paths2 = new ArrayList();
    ArrayList<Pair<Path, Integer>> paths = paths1;
    ArrayList<Pair<Path, Integer>> Overall  = new ArrayList<>();

    // image to display
    Bitmap bitmap;
    Paint paint = new Paint(Color.BLUE);
    Paint paintBrush = new Paint(Color.YELLOW);

    // we save a lot of points because they need to be processed
    // during touch events e.g. ACTION_MOVE
    float x1, x2, y1, y2, old_x1, old_y1, old_x2, old_y2;
    float mid_x = -1f, mid_y = -1f, old_mid_x = -1f, old_mid_y = -1f;
    int p1_id, p1_index, p2_id, p2_index;
    float total_dx = 0, total_dy = 0, total_sx = 1, total_sy = 1;

    // store cumulative transformations
    // the inverse matrix is used to align points with the transformations - see below
    Matrix matrix = new Matrix();
    Matrix inverse = new Matrix();

    boolean erase = false;

    // constructor
    public PDFimage(Context context) {
        super(context);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paintBrush.setColor(Color.YELLOW);
        paintBrush.setAntiAlias(true);
        paintBrush.setStrokeWidth(30);
        paintBrush.setStyle(Paint.Style.STROKE);
        paintBrush.setStrokeJoin(Paint.Join.ROUND);
        paintBrush.setStrokeCap(Paint.Cap.ROUND);
        paintBrush.setAlpha(45);
    }

    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MainActivity.erase){
            erase = false;
        }
        if (MainActivity.draw || MainActivity.highlight || MainActivity.erase || MainActivity.touch) {
            if (MainActivity.draw){
                paths = paths1;
            } else {
                paths = paths2;
            }
            switch(event.getPointerCount()) {
                // 1 point is drawing or erasing
                case 1:
                    p1_id = event.getPointerId(0);
                    p1_index = event.findPointerIndex(p1_id);

                    // invert using the current matrix to account for pan/scale
                    // inverts in-place and returns boolean
                    inverse = new Matrix();
                    matrix.invert(inverse);

                    // mapPoints returns values in-place
                    float[] inverted = new float[] { event.getX(p1_index), event.getY(p1_index) };
                    inverse.mapPoints(inverted);
                    x1 = inverted[0];
                    y1 = inverted[1];

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(LOGNAME, "Action down");
                            if (MainActivity.draw || MainActivity.highlight) {
                                path = new Path();
                                Pair<Path, Integer> p = new Pair<>(path, MainActivity.curr_page);
                                paths.add(p);
                                Overall.add(p);
                                path.moveTo(x1, y1);
                            } else if (MainActivity.erase) {
                                erase = Remove(x1, y1);
                            } else if (MainActivity.touch){
                                p = new ArrayList<>();
                                p.add(new Pair<>(x1, y1));
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (MainActivity.draw || MainActivity.highlight) {
                                Log.d(LOGNAME, "Action move");
                                path.lineTo(x1, y1);
                            } else if (MainActivity.erase && !erase) {
                                erase = Remove(x1, y1);
                            } else if (MainActivity.touch){
                                float dx = x1 - p.get(0).first;
                                float dy = y1 - p.get(0).second;
                                Log.d(LOGNAME, "translate: " + dx + "," + dy + " " + (total_dx + dx) + " " + (total_dy + dy) );
                                if (Math.abs(dx / total_sx) >= 15 && Math.abs(dy / total_sy) >= 15 &&
                                Math.abs(total_dx + dx) <= 800 && Math.abs(total_dy + dy) <= 1000) {
                                    total_dx += dx;
                                    total_dy += dy;
                                    matrix.preTranslate(dx, dy);
                                    Log.d(LOGNAME, "translate: " + dx + "," + dy + " total: " + total_dx + " " + total_dy);
                                    p.remove(0);
                                    p.add(new Pair<>(x1, y1));
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!MainActivity.erase){
                                Log.d(LOGNAME, "Action up");
                            }
                            if (MainActivity.draw || MainActivity.highlight){
                                String type;
                                if (MainActivity.draw){
                                    type = "erase_draw";
                                } else {
                                    type = "erase_highlight";
                                }
                                MainActivity.manager.new_event(new Pair<>(new Pair<>(path, MainActivity.curr_page),
                                        type));
                            }
                            break;
                    }
                    break;
                // 2 points is zoom/pan
                case 2:
                    // point 1
                    p1_id = event.getPointerId(0);
                    p1_index = event.findPointerIndex(p1_id);

                    // mapPoints returns values in-place
                    inverted = new float[] { event.getX(p1_index), event.getY(p1_index) };
                    inverse.mapPoints(inverted);

                    // first pass, initialize the old == current value
                    if (old_x1 < 0 || old_y1 < 0) {
                        old_x1 = x1 = inverted[0];
                        old_y1 = y1 = inverted[1];
                    } else {
                        old_x1 = x1;
                        old_y1 = y1;
                        x1 = inverted[0];
                        y1 = inverted[1];
                    }

                    // point 2
                    p2_id = event.getPointerId(1);
                    p2_index = event.findPointerIndex(p2_id);

                    // mapPoints returns values in-place
                    inverted = new float[] { event.getX(p2_index), event.getY(p2_index) };
                    inverse.mapPoints(inverted);

                    // first pass, initialize the old == current value
                    if (old_x2 < 0 || old_y2 < 0) {
                        old_x2 = x2 = inverted[0];
                        old_y2 = y2 = inverted[1];
                    } else {
                        old_x2 = x2;
                        old_y2 = y2;
                        x2 = inverted[0];
                        y2 = inverted[1];
                    }

                    // midpoint
                    mid_x = (x1 + x2) / 2;
                    mid_y = (y1 + y2) / 2;
                    old_mid_x = (old_x1 + old_x2) / 2;
                    old_mid_y = (old_y1 + old_y2) / 2;

                    // distance
                    float d_old = (float) Math.sqrt(Math.pow((old_x1 - old_x2), 2) + Math.pow((old_y1 - old_y2), 2));
                    float d = (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));

                    // pan and zoom during MOVE event
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        Log.d(LOGNAME, "Multitouch move");
                        // pan == translate of midpoint
                        float dx = mid_x - old_mid_x;
                        float dy = mid_y - old_mid_y;
                        matrix.preTranslate(dx, dy);
                        Log.d(LOGNAME, "translate: " + dx + "," + dy);

                        // zoom == change of spread between p1 and p2
                        float scale = d/d_old;
                        scale = Math.max(0, scale);
                        if (scale * total_sx <= 3 && scale * total_sy <= 3 &&
                        scale * total_sx >= 0.5 && scale * total_sy >= 0.5) {
                            matrix.preScale(scale, scale, mid_x, mid_y);
                            total_sx *= scale;
                            total_sy *= scale;
                            Log.d(LOGNAME, "scale: " + scale + " total: " + total_sx + " " + total_sy);
                        }

                        // reset on up
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        old_x1 = -1f;
                        old_y1 = -1f;
                        old_x2 = -1f;
                        old_y2 = -1f;
                        old_mid_x = -1f;
                        old_mid_y = -1f;
                    }
                    break;
                // I have no idea what the user is doing for 3+ points
                default:
                    break;
            }
            return true;
        }
        return true;
    }

    // set image as background
    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // apply transformations from the event handler above
        canvas.concat(matrix);
        super.onDraw(canvas);

        // draw background
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
        }
        // draw lines over it
        for (Pair<Path, Integer> p : paths2) {
            if (p.second == MainActivity.curr_page) {
                canvas.drawPath(p.first, paintBrush);
            }
        }
        for (Pair<Path, Integer> p : paths1) {
            if (p.second == MainActivity.curr_page) {
                canvas.drawPath(p.first, paint);
            }
        }
    }

    public boolean Remove(float x, float y){
        for (int i = Overall.size() - 1; i >= 0; i--){
            if (Overall.get(i).second == MainActivity.curr_page){
                RectF rectF = new RectF();
                Overall.get(i).first.computeBounds(rectF, true);
                Region r = new Region();
                r.setPath(Overall.get(i).first, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                if (r.contains((int)x, (int)y)){
                    if (paths1.contains(Overall.get(i))){
                        MainActivity.manager.new_event(new Pair<>(Overall.get(i), "draw"));
                        paths1.remove(Overall.get(i));
                    } else {
                        MainActivity.manager.new_event(new Pair<>(Overall.get(i), "highlight"));
                        paths2.remove(Overall.get(i));
                    }
                    Overall.remove(i);
                    return true;
                }
            }

        }
        return false;
    }

    public void undo_manager(Pair<Pair<Path, Integer>, String> action){
        if (action.second.equals("erase_draw")){
            Pair<Path, Integer> path = paths1.get(paths1.size() - 1);
            Overall.remove(path);
            paths1.remove(path);
        } if (action.second.equals("erase_highlight")){
            Pair<Path, Integer> path = paths2.get(paths2.size() - 1);
            Overall.remove(path);
            paths2.remove(path);
        } if (action.second.equals("draw")){
            paths1.add(action.first);
            Overall.add(action.first);
        } else if (action.second.equals("highlight")){
            paths2.add(action.first);
            Overall.add(action.first);
        }
    }
}
