package armyc2.c5isr.renderer.test3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import armyc2.c5isr.renderer.test3.R;
import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.utilities.RendererSettings;

public class MainActivity extends Activity {

    /**
     * Called when the activity is first created.
     */

    public static String lineType = "";
    public static String fillColor = "";
    public static String lineColor = "";
    public static String textColor = "";
    public static String T = "";
    public static String T1 = "";
    public static String H = "";
    public static String H1 = "";
    public static String W = "";
    public static String W1 = "";
    public static String V = "";
    public static String Y = "";
    public static String AM = "";
    public static String AN = "";
    public static String X = "";
    public static String extents = "";
    public static String lineWidth = "";
    private MilStdIconRenderer mir = null;
    private String TAG = "armyc2.c5isr.MainActivity";
    private boolean populateModifiers = false;
    private boolean svg = false;
    private EditText editText = null;
    private MyView myView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editText = (EditText) findViewById(R.id.edit_message);
        loadRenderer();

        // Load values
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        lineType = sharedPref.getString("lineType", "");
        fillColor = sharedPref.getString("fillColor", "");
        lineColor = sharedPref.getString("lineColor", "");
        textColor = sharedPref.getString("textColor", "");
        T = sharedPref.getString("T", "");
        T1 = sharedPref.getString("T1", "");
        H = sharedPref.getString("H", "");
        H1 = sharedPref.getString("H1", "");
        W = sharedPref.getString("W", "");
        W1 = sharedPref.getString("W1", "");
        V = sharedPref.getString("V", "");
        Y = sharedPref.getString("Y", "");
        AM = sharedPref.getString("AM", "");
        AN = sharedPref.getString("AN", "");
        X = sharedPref.getString("X", "");
        extents = sharedPref.getString("extents", "");
        lineWidth = sharedPref.getString("lineWidth", "");

        // Fill text fields
        editText = (EditText) findViewById(R.id.edit_message);
        editText.setText(lineType);
        editText = (EditText) findViewById(R.id.edit_T);
        editText.setText(T);
        editText = (EditText) findViewById(R.id.edit_T1);
        editText.setText(T1);
        editText = (EditText) findViewById(R.id.edit_H);
        editText.setText(H);
        editText = (EditText) findViewById(R.id.edit_H1);
        editText.setText(H1);
        editText = (EditText) findViewById(R.id.edit_W);
        editText.setText(W);
        editText = (EditText) findViewById(R.id.edit_W1);
        editText.setText(W1);
        editText = (EditText) findViewById(R.id.edit_V);
        editText.setText(V);
        editText = (EditText) findViewById(R.id.edit_Y);
        editText.setText(Y);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflator = new MenuInflater(this);
        inflator.inflate(R.layout.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
    private static String lastContext = "modifiers";

    //this method automatically called when user select menu items

    public boolean onOptionsItemSelected(MenuItem item) {

        if (myView == null) {
            myView = new MyView(this);
        }
        if (lastContext.equals("attributes")) {
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            editText = (EditText) findViewById(R.id.edit_LineColor);
            lineColor = editText.getText().toString();
            editor.putString("lineColor", lineColor);
            editText = (EditText) findViewById(R.id.edit_TextColor);
            textColor = editText.getText().toString();
            editor.putString("textColor", textColor);
            editText = (EditText) findViewById(R.id.edit_FillColor);
            fillColor = editText.getText().toString();
            editor.putString("fillColor", fillColor);
            editText = (EditText) findViewById(R.id.edit_LineWidth);
            lineWidth = editText.getText().toString();
            editor.putString("lineWidth", lineWidth);
            editText = (EditText) findViewById(R.id.edit_AM);
            AM = editText.getText().toString();
            editor.putString("AM",AM );
            editText = (EditText) findViewById(R.id.edit_AN);
            AN = editText.getText().toString();
            editor.putString("AN", AN);
            editText = (EditText) findViewById(R.id.edit_X);
            X = editText.getText().toString();
            editor.putString("X", X);
            editText = (EditText) findViewById(R.id.edit_Extents);
            extents = editText.getText().toString();
            editor.putString("extents", extents);
            editor.apply();
        } else if (lastContext.equals("modifiers")) {
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            editText = (EditText) findViewById(R.id.edit_message);
            lineType = editText.getText().toString();
            editor.putString("lineType", lineType);
            editText = (EditText) findViewById(R.id.edit_T);
            T = editText.getText().toString();
            editor.putString("T", T);
            editText = (EditText) findViewById(R.id.edit_T1);
            T1 = editText.getText().toString();
            editor.putString("T1", T1);
            editText = (EditText) findViewById(R.id.edit_H);
            H = editText.getText().toString();
            editor.putString("H", H);
            editText = (EditText) findViewById(R.id.edit_H1);
            H1 = editText.getText().toString();
            editor.putString("H1", H1);
            editText = (EditText) findViewById(R.id.edit_W);
            W = editText.getText().toString();
            editor.putString("W", W);
            editText = (EditText) findViewById(R.id.edit_W1);
            W1 = editText.getText().toString();
            editor.putString("W1", W1);
            editText = (EditText) findViewById(R.id.edit_V);
            V = editText.getText().toString();
            editor.putString("V", V);
            editText = (EditText) findViewById(R.id.edit_Y);
            Y = editText.getText().toString();
            editor.putString("Y", Y);
            editor.apply();
        }
        switch (item.getItemId()) {
            case R.id.DRAW:
                lastContext = "draw";
                setContentView(myView);
                break;
            case R.id.MODIFIERS:
                lastContext = "modifiers";
                setContentView(R.layout.main);
                editText = (EditText) findViewById(R.id.edit_message);
                editText.setText(lineType);
                editText = (EditText) findViewById(R.id.edit_T);
                editText.setText(T);
                editText = (EditText) findViewById(R.id.edit_T1);
                editText.setText(T1);
                editText = (EditText) findViewById(R.id.edit_H);
                editText.setText(H);
                editText = (EditText) findViewById(R.id.edit_H1);
                editText.setText(H1);
                editText = (EditText) findViewById(R.id.edit_W);
                editText.setText(W);
                editText = (EditText) findViewById(R.id.edit_W1);
                editText.setText(W1);
                editText = (EditText) findViewById(R.id.edit_V);
                editText.setText(V);
                editText = (EditText) findViewById(R.id.edit_Y);
                editText.setText(Y);
                break;
            case R.id.ATTRIBUTES:
                lastContext = "attributes";
                setContentView(R.layout.attributes);
                editText = (EditText) findViewById(R.id.edit_LineColor);
                editText.setText(lineColor);
                editText = (EditText) findViewById(R.id.edit_TextColor);
                editText.setText(textColor);
                editText = (EditText) findViewById(R.id.edit_FillColor);
                editText.setText(fillColor);
                editText = (EditText) findViewById(R.id.edit_LineWidth);
                editText.setText(lineWidth);
                editText = (EditText) findViewById(R.id.edit_AM);
                editText.setText(AM);
                editText = (EditText) findViewById(R.id.edit_AN);
                editText.setText(AN);
                editText = (EditText) findViewById(R.id.edit_X);
                editText.setText(X);
                editText = (EditText) findViewById(R.id.edit_Extents);
                editText.setText(extents);
                break;
            case R.id.CLEAR_FIELDS:
                lineType = "";
                fillColor = "";
                lineColor = "";
                textColor = "";
                T = "";
                T1 = "";
                H = "";
                H1 = "";
                W = "";
                W1 = "";
                V = "";
                Y = "";
                AM = "";
                AN = "";
                X = "";
                extents = "";
                lineWidth = "";
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString("lineType", "");
                editor.putString("fillColor", "");
                editor.putString("lineColor", "");
                editor.putString("textColor", "");
                editor.putString("T", "");
                editor.putString("T1", "");
                editor.putString("H", "");
                editor.putString("H1", "");
                editor.putString("W", "");
                editor.putString("W1", "");
                editor.putString("V", "");
                editor.putString("Y", "");
                editor.putString("AM", "");
                editor.putString("AN", "");
                editor.putString("X", "");
                editor.putString("extents", "");
                editor.putString("lineWidth", "");
                editor.apply();
                if (lastContext.equals("modifiers")) {
                    editText = (EditText) findViewById(R.id.edit_message);
                    editText.setText(lineType);
                    editText = (EditText) findViewById(R.id.edit_T);
                    editText.setText(T);
                    editText = (EditText) findViewById(R.id.edit_T1);
                    editText.setText(T1);
                    editText = (EditText) findViewById(R.id.edit_H);
                    editText.setText(H);
                    editText = (EditText) findViewById(R.id.edit_H1);
                    editText.setText(H1);
                    editText = (EditText) findViewById(R.id.edit_W);
                    editText.setText(W);
                    editText = (EditText) findViewById(R.id.edit_W1);
                    editText.setText(W1);
                    editText = (EditText) findViewById(R.id.edit_V);
                    editText.setText(V);
                    editText = (EditText) findViewById(R.id.edit_Y);
                    editText.setText(Y);
                } else if (lastContext.equals("attributes")) {
                    editText = (EditText) findViewById(R.id.edit_LineColor);
                    editText.setText(lineColor);
                    editText = (EditText) findViewById(R.id.edit_TextColor);
                    editText.setText(textColor);
                    editText = (EditText) findViewById(R.id.edit_FillColor);
                    editText.setText(fillColor);
                    editText = (EditText) findViewById(R.id.edit_LineWidth);
                    editText.setText(lineWidth);
                    editText = (EditText) findViewById(R.id.edit_AM);
                    editText.setText(AM);
                    editText = (EditText) findViewById(R.id.edit_AN);
                    editText.setText(AN);
                    editText = (EditText) findViewById(R.id.edit_X);
                    editText.setText(X);
                    editText = (EditText) findViewById(R.id.edit_Extents);
                    editText.setText(extents);
                }
            default:
                break;
        }
        return true;
    }

    public void loadRenderer() {
            //disable svg engine
        //((CheckBox)findViewById(R.id.cbSVG)).setActivated(false);

            //TextView t = (TextView)findViewById(R.id.tvStatus);
        //t.setText("Initializing Renderer");
        //depending on screen size and DPI you may want to change the font size.
        RendererSettings rs = RendererSettings.getInstance();
        rs.setModifierFont("Arial", Typeface.BOLD, 18);
        rs.setMPLabelFont("Arial", Typeface.BOLD, 18);

        //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_COLORFILL);

        mir = MilStdIconRenderer.getInstance();
        String cacheDir = getApplicationContext().getCacheDir().getAbsoluteFile().getAbsolutePath();
        mir.init(this);
        DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int dpi = metrics.densityDpi;

            //t.setText("Renderer Initialized");
    }
}
