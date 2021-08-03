package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static net.codebot.pdfviewer.R.id.redo;

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provide this code.

public class MainActivity extends AppCompatActivity {

    final String LOGNAME = "pdf_viewer";
    final String FILENAME = "personal_reflection_final.pdf";
    final int FILERESID = R.raw.personal_reflection_final;
    final static UndoManager manager = new UndoManager();
    static ImageButton pageUp, pageDown;
    static RadioButton Undo, Redo;

    // manage the pages of the PDF, see below
    PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer.Page currentPage;
    public static boolean undo, redo, draw, highlight, erase, touch = false;
    public TextView page;
    public static int curr_page;

    // custom ImageView class that captures strokes and draws them over the image
    PDFimage pageImage;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        touch = true;

        LinearLayout layout = findViewById(R.id.pdfLayout);
        pageImage = new PDFimage(this);
        layout.addView(pageImage);
        TextView pdf = findViewById(R.id.pdfName);
        pdf.setText("Personal Reflection Final Draft");
        page = findViewById(R.id.page);
        page.setText("current page: 1 / 3");
        curr_page = 1;
        layout.setEnabled(true);
        pageImage.setMinimumWidth(1000);
        pageImage.setMinimumHeight(2000);

        // open page 0 of the PDF
        // it will be displayed as an image in the pageImage (above)
        try {
            openRenderer(this);
            showPage(0);
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening PDF");
        }

        pageUp = findViewById(R.id.pageUp);
        pageDown = findViewById(R.id.pageDown);
        Undo = findViewById(R.id.undo);
        Redo = findViewById(R.id.redo);
        update();

        pageUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curr_page > 1){
                    System.out.println("checked");
                    curr_page--;
                    showPage(curr_page - 1);
                }
                update();
            }
        });
        pageDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curr_page < 3){
                    System.out.println("checked down");
                    curr_page++;
                    showPage(curr_page - 1);
                }
                update();
            }
        });
    }

    public static void update(){
        if (curr_page == 1){
            pageUp.setEnabled(false);
        } else {
            pageUp.setEnabled(true);
        }

        if (curr_page == 3){
            pageDown.setEnabled(false);
        } else {
            pageDown.setEnabled(true);
        }

        if (manager.canUndo()){
            Undo.setEnabled(true);
        } else {
            Undo.setEnabled(false);
        }

        if (manager.canRedo()){
            Redo.setEnabled(true);
        } else {
            Redo.setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeRenderer();
        } catch (IOException ex) {
            Log.d(LOGNAME, "Unable to close PDF renderer");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            InputStream asset = this.getResources().openRawResource(FILERESID);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    // do this before you quit!
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            currentPage.close();
        }
        pdfRenderer.close();
        parcelFileDescriptor.close();
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) {
            return;
        }
        // Close the current page before opening another one.
        if (null != currentPage) {
            currentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index);
        curr_page = index + 1;
        page.setText("current page: " + curr_page + " / 3");
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);

        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // Display the page
        pageImage.setImage(bitmap);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.touch:
                if (checked) {
                    touch = true;
                    System.out.println("touch is true");
                    draw = redo = erase = highlight = undo = false;
                }
                break;
            case R.id.undo:
                if (checked) {
                    undo = true;
                    draw = redo = erase = highlight = touch = false;
                    if (manager.canUndo()){
                        Pair<Pair<Path, Integer>, String> action = manager.setUndo();
                        pageImage.undo_manager(action);
                        System.out.println(action.first.second);
                        if (action.first.second < 1){
                            showPage(0);
                        } else if (action.first.second > 3){
                            showPage(2);
                        } else {
                            showPage(action.first.second - 1);
                        }

                    }
                    System.out.println("undo");
                }
                break;
            case R.id.redo:
                if (checked) {
                    redo = true;
                    undo = draw = erase = highlight = touch = false;
                    if (manager.canRedo()){
                        Pair<Pair<Path, Integer>, String> action = manager.setRedo();
                        pageImage.undo_manager(action);
                        if (action.first.second < 1){
                            showPage(0);
                        } else if (action.first.second > 3){
                            showPage(2);
                        } else {
                            showPage(action.first.second - 1);
                        }
                    }
                    System.out.println("redo");
                }
                break;
            case R.id.draw:
                if (checked) {
                    draw = true;
                    undo = redo = erase = highlight = touch = false;
                    System.out.println("draw");
                }
                break;
            case R.id.erase:
                if (checked) {
                    erase = true;
                    undo = redo = draw = highlight = touch = false;
                    System.out.println("erase");
                }
                break;
            case R.id.highlight:
                if (checked) {
                    highlight = true;
                    undo = redo = erase = draw = touch = false;
                    System.out.println("highlight");
                }
                break;
        }
        update();
    }

}
