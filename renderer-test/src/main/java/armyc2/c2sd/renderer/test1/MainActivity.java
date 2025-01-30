package armyc2.c2sd.renderer.test1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import armyc2.c2sd.graphics2d.Font;
import armyc2.c2sd.renderer.test1.RenderSPThreadTest;

import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import armyc2.c2sd.renderer.test1.R;
import armyc2.c2sd.renderer.IconRenderer;
import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.SinglePointRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;


public class MainActivity extends Activity {

	SinglePointRenderer spr = null;
	IconRenderer icr = null;
	MilStdIconRenderer mir = null;
	private String TAG = "armyc2.c2sd.MainActivity";
	private boolean populateModifiers = false;
        private boolean svg = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadRenderer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    

  
    
    
	public void logError(String tag, Throwable thrown)
	{
		if(tag == null || tag == "")
			tag = "singlePointRenderer";
		
		String message = thrown.getMessage();
		String stack = getStackTrace(thrown);
		if(message != null)
			Log.e(tag,message);
		if(stack != null)
			Log.e(tag,stack);
	}
	
	public void loadRenderer()
	{
            //disable svg engine
            ((CheckBox)findViewById(R.id.cbSVG)).setActivated(false);
            
            TextView t = (TextView)findViewById(R.id.tvStatus);
            t.setText("Initializing Renderer");

            //depending on screen size and DPI you may want to change the font size.
            RendererSettings rs = RendererSettings.getInstance();
            rs.setModifierFont("Arial", Typeface.BOLD, 18);
            rs.setMPModifierFont("Arial", Typeface.BOLD, 18);
            rs.setSymbologyStandard(RendererSettings.Symbology_2525C);
			//rs.setSymbologyStandard(RendererSettings.Symbology_2525B);
            //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);


            rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
            //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_COLORFILL);
			//rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
            //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_NONE);
    	
    	
    	
            mir = MilStdIconRenderer.getInstance();
            String cacheDir = getApplicationContext().getCacheDir().getAbsoluteFile().getAbsolutePath();
            mir.init(this, cacheDir);
            DisplayMetrics metrics = new DisplayMetrics();
            //getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int dpi = metrics.densityDpi;
            //RendererSettings.getInstance().setDeviceDPI(dpi);
    	
            t.setText("Renderer Initialized");
            
		
	}
	
    /**
     * Gets an icon from the single point renderer and puts it on the page
     */
    public void drawSymbol(View view)
    {
    	try
    	{
    		if(mir == null)
    		{
    			loadRenderer();
    		}
    		
	    	//Intent intent = new Intent(this, DisplayMessageActivity.class);
	    	EditText editText = (EditText) findViewById(R.id.etSymbolID);
	    	EditText etPixelSize = (EditText) findViewById(R.id.etPixelSize);
	    	ImageView imageView = (ImageView) findViewById(R.id.imageView1);
	    	imageView.clearAnimation();//helps with memory?
	    	imageView.setBackgroundColor(Color.LTGRAY);
	    	
	    	
	    	///////////////////
	    	String symbolID = editText.getText().toString();
	    	if(symbolID == null || symbolID.contentEquals(""))
	    		symbolID = "SFAPMFH---*****";
	    	
	    	SparseArray<String> modifiers = new SparseArray<String>();
	    	SparseArray<String> attributes = new SparseArray<String>();
	    	
	    	String strPixelSize =  etPixelSize.getText().toString();
	    	
	    	if(strPixelSize != null && strPixelSize.equals("") == false)
	    		attributes.put(MilStdAttributes.PixelSize,strPixelSize);
	    	else
	    		attributes.put(MilStdAttributes.PixelSize,"240");//*/

			//attributes.put(MilStdAttributes.SymbologyStandard,String.valueOf(RendererSettings.Symbology_2525B));
	    	//attributes.put(MilStdAttributes.LineColor, "cyan");
			//attributes.put(MilStdAttributes.FillColor, "00FF0000");
	    	
                //RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_NONE);
	    		//RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_COLORFILL);
                //RendererSettings.getInstance().setTextOutlineWidth(0);
            //RendererSettings.getInstance().setSinglePointSymbolOutlineWidth(0);
                //attributes.put(MilStdAttributes.TextColor, "FF0000");
                //attributes.put(MilStdAttributes.TextBackgroundColor, "000000");
			//attributes.put(MilStdAttributes.Alpha, "150");

			//attributes.put(MilStdAttributes.FillColor,"00000000");
			//attributes.put(MilStdAttributes.LineColor,"0000FF");
			//attributes.put(MilStdAttributes.IconColor,"0000FF");

			//attributes.put(MilStdAttributes.PixelSize,"100");
			//RendererSettings.getInstance().setModifierFont("serif", Font.PLAIN,32);

			//RendererSettings.getInstance().setOperationalConditionModifierType(RendererSettings.OperationalConditionModifierType_SLASH);
			//attributes.put(MilStdAttributes.KeepUnitRatio,"false");
	    	
	    	populateModifiers = ((CheckBox)findViewById(R.id.cbModifiers)).isChecked();
	    	
	    	if(populateModifiers==true)
	    	{
		    	if(symbolID.charAt(0)=='G' || symbolID.charAt(0)=='W')
		    	{
		    		populateModifiersForTGs(modifiers);
		    	}
		    	else
		    	{
		    		populateModifiersForUnits(modifiers);
		    	}
	    	}
                
                svg = ((CheckBox)findViewById(R.id.cbSVG)).isChecked();
	    	
	    	/*if(svg==true)
	    	{
		    //set renderer engine to SVG
                    RendererSettings.getInstance().setIconEngine(RendererSettings.IconEngine_SVG);
	    	}
                else
                {
                    RendererSettings.getInstance().setIconEngine(RendererSettings.IconEngine_FONT);
                }//*/

	    	boolean canRender = mir.CanRender(symbolID, modifiers, attributes);
	    	
	    	if(canRender)
	    		Log.i("DrawSymbol", "CanRender: True");
	    	else
	    		Log.i("DrawSymbol", "CanRender: False");
	    	
	    	SymbolDef test = SymbolDefTable.getInstance().getSymbolDef("S*G*UCI---*****", 0);
	    	test = SymbolDefTable.getInstance().getSymbolDef("S*G*UCI---*****", 1);
	    		
	    	//ImageInfo ii = mir.RenderUnit(symbolID, modifiers);
			//attributes.put(MilStdAttributes.IconColor,"#00FF00");
	    	ImageInfo ii = mir.RenderIcon(symbolID, modifiers, attributes);
	    	
	    	if(ii != null)
	    	{
				//Save to SD card for manual inspection/////////////////////////////////////////////////
				OutputStream output;
				//find sd card path
				File savePath = Environment.getExternalStorageDirectory();
				//Create folder in SD card Path
				File dir = new File(savePath.getAbsolutePath() + "/icons/");
				dir.mkdirs();

				//create name for the saved image
				File file = new File(dir, symbolID.replace('*','-') + ".png");
				try {

					output = new FileOutputStream(file);
					ii.getImage().compress(Bitmap.CompressFormat.PNG, 100, output);
					output.flush();
					output.close();
				}
				catch (Exception e){
					e.printStackTrace();
				}//*/
				////////////////////////////////////////////////////////////////////////////////////////
		    	Bitmap msBmp = ii.getImage();
		    	
		    	//int bytes = msBmp.getAllocationByteCount();
		    	int bytes = 0;
		    	
		    	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1)
		    	{
		    		bytes = msBmp.getRowBytes() * msBmp.getHeight();
		    	}
		    	/*else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
		    	{
		    		bytes = msBmp.getByteCount();
		    	}*/
		    	else
		    	{
		    		bytes = msBmp.getByteCount();
		    		//bytes = msBmp.getAllocationByteCount();
		    	}
		    	float megaBytes = bytes / 1000000.0f;
		        float kb = bytes / 1000.0f;
		    	String msg = "Image size: " + String.valueOf(bytes) + "bytes, " + String.valueOf(kb) + "kilobytes, " + String.valueOf(megaBytes) + "MB"; 
		    	Log.i("drawSymbol", msg);
		    	
		    	//test ImageInfo values
		    	Canvas msCanvas = new Canvas(msBmp);
		    	Paint outline = new Paint();
		    	outline.setColor(Color.RED);
		    	outline.setStyle(Style.STROKE);
		    	outline.setStrokeWidth(1);
                //draw outline around symbol////////////////////////////////////////////////////////
		    	//msCanvas.drawRect(ii.getSymbolBounds(), outline);
		    	msCanvas.drawRect(ii.getCenterPoint().x - 1, ii.getCenterPoint().y - 1, ii.getCenterPoint().x + 1, ii.getCenterPoint().y + 1, outline);//*/


				Log.i(TAG, "SymbolBounds: " + ii.getSymbolBounds().toString());
				Log.i(TAG, "Image Width: " + String.valueOf(ii.getImage().getWidth()));
				Log.i(TAG, "Image Height: " + String.valueOf(ii.getImage().getHeight()));

				//test getBasicSymbolID()
				//String stemp = SymbolUtilities.getBasicSymbolID("EUF-HB----H----");
		    	
		    	if(msBmp != null)
		    		imageView.setImageBitmap(msBmp);
	    	}
	    	
	    	//test font file
	    	/*TextView t = (TextView)findViewById(R.id.tvStatus);
	    	t.setText("Testing font files");
	    	FontManager.getInstance().testFontFiles();
	    	t.setText("Done");//*/
 	
    	}
    	catch(Exception exc)
    	{
    		Log.e(TAG, exc.getMessage());
    		String stackTrace = MainActivity.getStackTrace(exc);
    		Log.e(TAG, stackTrace);
    	}

    }
    
    private void populateModifiersForUnits(SparseArray<String> modifiers)
    {
    	modifiers.put(ModifiersUnits.C_QUANTITY,"10");
        modifiers.put(ModifiersUnits.H_ADDITIONAL_INFO_1,"Hj");
        modifiers.put(ModifiersUnits.H1_ADDITIONAL_INFO_2,"H1");
        modifiers.put(ModifiersUnits.H2_ADDITIONAL_INFO_3,"H2");
        modifiers.put(ModifiersUnits.X_ALTITUDE_DEPTH,"X");//X
        modifiers.put(ModifiersUnits.K_COMBAT_EFFECTIVENESS,"K");//K
        modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT,"45");//Q

        modifiers.put(ModifiersUnits.W_DTG_1, armyc2.c2sd.renderer.utilities.SymbolUtilities.getDateLabel(new Date()));//W
        modifiers.put(ModifiersUnits.W1_DTG_2,armyc2.c2sd.renderer.utilities.SymbolUtilities.getDateLabel(new Date()));//W1
        modifiers.put(ModifiersUnits.J_EVALUATION_RATING,"J");
        modifiers.put(ModifiersUnits.M_HIGHER_FORMATION,"Mj");
        modifiers.put(ModifiersUnits.N_HOSTILE,"ENY");
        modifiers.put(ModifiersUnits.P_IFF_SIF,"P");
        modifiers.put(ModifiersUnits.Y_LOCATION,"Yj");
        
        //if(symbol.getSymbolID().startsWith("G"))
        modifiers.put(ModifiersUnits.C_QUANTITY,"C");
        
        modifiers.put(ModifiersUnits.F_REINFORCED_REDUCED,"RD");
        
        modifiers.put(ModifiersUnits.L_SIGNATURE_EQUIP,"!");
        modifiers.put(ModifiersUnits.AA_SPECIAL_C2_HQ, "AA");
        modifiers.put(ModifiersUnits.G_STAFF_COMMENTS,"Gj");
        //symbol.symbolicon A
        modifiers.put(ModifiersUnits.V_EQUIP_TYPE,"Vj");
        modifiers.put(ModifiersUnits.T_UNIQUE_DESIGNATION_1,"Tj");
        modifiers.put(ModifiersUnits.T1_UNIQUE_DESIGNATION_2,"T1");
        modifiers.put(ModifiersUnits.Z_SPEED,"999");//Z

		//sigint //TODO
		modifiers.put(ModifiersUnits.R2_SIGNIT_MOBILITY_INDICATOR, "2");
		modifiers.put(ModifiersUnits.AD_PLATFORM_TYPE, "AD");
		modifiers.put(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME, "AE");
		modifiers.put(ModifiersUnits.AF_COMMON_IDENTIFIER, "AF");
		//TODO
		modifiers.put(ModifiersUnits.AO_ENGAGEMENT_BAR, "AO");
    }
    
    private void populateModifiersForTGs(SparseArray<String> modifiers)
    {
    	modifiers.put(ModifiersTG.C_QUANTITY,"10");
        modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1,"H");
        modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2,"H1");
        modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3,"H2");
        modifiers.put(ModifiersTG.X_ALTITUDE_DEPTH,"X");//X
        modifiers.put(ModifiersTG.Q_DIRECTION_OF_MOVEMENT,"45");//Q

        modifiers.put(ModifiersTG.W_DTG_1, armyc2.c2sd.renderer.utilities.SymbolUtilities.getDateLabel(new Date()));//W
        modifiers.put(ModifiersTG.W1_DTG_2,armyc2.c2sd.renderer.utilities.SymbolUtilities.getDateLabel(new Date()));//W1
        modifiers.put(ModifiersTG.N_HOSTILE,"ENY");
        modifiers.put(ModifiersTG.Y_LOCATION,"Y");
        
        //if(symbol.getSymbolID().startsWith("G"))
        modifiers.put(ModifiersTG.C_QUANTITY,"C");
        
        modifiers.put(ModifiersUnits.L_SIGNATURE_EQUIP,"!");

        //symbol.symbolicon A
        modifiers.put(ModifiersTG.V_EQUIP_TYPE,"V");
        modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1,"T");
        modifiers.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2,"T1");
        
    }
    
    public void speedTest(View view)
    {
    	
    	try
    	{
    		if(mir == null)
    		{
    			loadRenderer();
    		}

			if(((CheckBox)findViewById(R.id.cbSVG)).isChecked())
				RendererSettings.getInstance().setCacheEnabled(true);
			else
				RendererSettings.getInstance().setCacheEnabled(false);
    		
	    	//Intent intent = new Intent(this, DisplayMessageActivity.class);
	    	EditText editText = (EditText) findViewById(R.id.etSymbolID);
	    	EditText etPixelSize = (EditText) findViewById(R.id.etPixelSize);
	    	ImageView imageView = (ImageView) findViewById(R.id.imageView1);
	    	imageView.clearAnimation();//helps with memory?
	    	imageView.setBackgroundColor(Color.GREEN);
	    	
	    	

	    	String symbolID = editText.getText().toString();
	    	if(symbolID == null || symbolID.contentEquals(""))
	    		symbolID = "SFAPMFH---*****";
	    	
	    	SparseArray<String> modifiers = new SparseArray<String>();
	    	SparseArray<String> attributes = new SparseArray<String>();
	    	
    		String strPixelSize =  etPixelSize.getText().toString();
	    	
	    	if(strPixelSize != null && strPixelSize.equals("") == false)
	    		attributes.put(MilStdAttributes.PixelSize,strPixelSize);
	    	else
	    		attributes.put(MilStdAttributes.PixelSize,"60");
	    	
	    	
	    	//RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
	    	//RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
	    	populateModifiers = ((CheckBox)findViewById(R.id.cbModifiers)).isChecked();
	    	
	    	if(populateModifiers==true)
	    	{
		    	if(symbolID.charAt(0)=='G' || symbolID.charAt(0)=='W')
		    	{
		    		populateModifiersForTGs(modifiers);
		    	}
		    	else
		    	{
		    		populateModifiersForUnits(modifiers);
		    	}
	    	}
                
                svg = ((CheckBox)findViewById(R.id.cbSVG)).isChecked();
	    	
	    	/*if(svg==true)
	    	{
		    //set renderer engine to SVG
                    RendererSettings.getInstance().setIconEngine(RendererSettings.IconEngine_SVG);
	    	}
                else
                {
                    RendererSettings.getInstance().setIconEngine(RendererSettings.IconEngine_FONT);
                }//*/
	    	//ImageInfo ii = mir.RenderUnit(symbolID, modifiers);
	    	
	    	long start = System.currentTimeMillis();//java.lang.System.nanoTime();
	    	Date dStart = new Date();
	    	ImageInfo ii = null;
	    	
	    	boolean trace = false;
	    	if(trace == true)
	    		android.os.Debug.startMethodTracing("SPSPEEDTEST");
	    	for(int i = 0; i < 1000; i++)
	    	{
	    		ii = mir.RenderIcon(symbolID, modifiers, attributes);
	    	}
	    	if(trace == true)
	    		android.os.Debug.stopMethodTracing();
	    	
	    	long stop = System.currentTimeMillis();// java.lang.System.nanoTime();
	    	Date dStop = new Date();
	    	double elapsed = (stop - start) / 1000;
	    	elapsed = (dStop.getTime() - dStart.getTime()) / 1000.0;
	    	
	    	TextView t = (TextView)findViewById(R.id.tvStatus);
	    	t.setText("1k symbols generated in: " + String.valueOf(elapsed) + " seconds.");
	    	
	    	Bitmap msBmp = ii.getImage();
	    	
	    	/*Canvas msCanvas = new Canvas(msBmp);
	    	Paint outline = new Paint();
	    	outline.setColor(Color.RED);
	    	outline.setStyle(Style.STROKE);
	    	outline.setStrokeWidth(1);
	    	msCanvas.drawRect(0, 0, msBmp.getWidth()-1, msBmp.getHeight()-1, outline);//*/
	    	
	    	if(msBmp != null)
	    		imageView.setImageBitmap(msBmp);
	    	
	    	/*String symbolID = editText.getText().toString();
	    	Map<String,Object> modifiers = new HashMap<String, Object>();
	    	
	    	ImageInfo ii = mir.RenderUnit(symbolID, modifiers);
	    	
	    	Bitmap msBmp = ii.getImage();
	    	Canvas msCanvas = new Canvas(msBmp);
	    	Paint outline = new Paint();
	    	outline.setColor(Color.RED);
	    	msCanvas.drawRect(0, 0, msBmp.getWidth(), msBmp.getHeight(), outline);
	    	imageView.setImageBitmap(msBmp);//*/

			//RendererSettings.getInstance().setCacheEnabled(false);
	    	
    	}
    	catch(Exception exc)
    	{
    		Log.e(TAG, exc.getMessage());
    		Log.e(TAG, MainActivity.getStackTrace(exc));
    	}
    }
    

    
    public void threadTest(View view)
    {
    	try
    	{

            try{
                RenderSPThreadTest r1 = new RenderSPThreadTest();
                r1.testSecRenderer();
                TextView t = (TextView)findViewById(R.id.tvStatus);
                t.setText("Thread Fail Count: " + String.valueOf(r1._failCount) + ", Thread Success Count: " + String.valueOf(r1._successCount));
                for (String error : r1._errors){
                    Log.i("threadTest", error);
                }
            }
            catch(Exception exc) {
                Log.e("threadTest",exc.getMessage(),exc);
            }
            //SPTestThread
    		//thread test for single points///////////////////////////////////
           /*
            //reusing to test random symbols
            boolean ra = ((CheckBox)findViewById(R.id.cbModifiers)).isChecked();
            RenderSPThreadTest r1 = new RenderSPThreadTest();
            r1._name = "r1";
            r1._randomAffiliation = ra;
            r1._symbolID = "SUPPT----------";
        
            Thread t1 = new Thread(r1);
    		
            RenderSPThreadTest r2 = new RenderSPThreadTest();
            r2._name = "r2";
            //r2._randomAffiliation = ra;
            r2._symbolID = "SUPPT----------";
    		Thread t2 = new Thread(r2);
                
            RenderSPThreadTest r3 = new RenderSPThreadTest();
            r3._name = "r3";
            //r3._randomAffiliation = ra;
            r3._symbolID = "SUPPT----------";
    		Thread t3 = new Thread(r3);
    		
    		
    		long start = System.currentTimeMillis();//java.lang.System.nanoTime();
	    	Date dStart = new Date();
    		t1.start();
    		t2.start();
                t3.start();
    		
    		int sleepCount = 0;
    		while(t1.isAlive() || t2.isAlive() || t3.isAlive())
    		{
    			sleepCount++;
    			Thread.sleep(100);
    		}
    		
    		long stop = System.currentTimeMillis();// java.lang.System.nanoTime();
	    	Date dStop = new Date();
	    	double elapsed = (stop - start) / 1000;
	    	elapsed = (dStop.getTime() - dStart.getTime()) / 1000.0;
	    	
	    	try
	    	{
                    String status = "Threads done in: " + String.valueOf(elapsed) + " seconds.  SleepCount: " + String.valueOf(sleepCount);
		    	TextView t = (TextView)findViewById(R.id.tvStatus);
                        
                        if(r1._done && r1._result)
                            status += " T1 success";
                        else
                            status += " T1 fail";
                        if(r2._done && r2._result)
                            status += ", T2 success";
                        else
                            status += ", T2 fail";
                        if(r3._done && r3._result)
                            status += ", T3 success.";
                        else
                            status += ", T3 fail.";
                                    
		    	t.setText(status);
                        
	    	}
	    	catch(Exception exc)
	    	{
	    		Log.e(TAG, exc.getMessage(), exc);
	    	}

    		//*///End thread test for singlepoints//////////////////////////////
    		
    		//FLOT TEST/////////////////////////////////////////////////////////
    		/*
    		StringBuilder message = new StringBuilder();
    		String id = "id";
    		String name = "name";
    		String description = "description";
    		String symbolCode = "GFGPGLF---****X"; 
    		String controlPoints = "8.40185525443334,38.95854638813517 15.124217101733166,36.694658205882995 18.49694847529253,40.113591379080155 8.725267851897936,42.44678226078903 8.217048055882143,40.76041657400935";
    		String altitudeMode = "absolute";
    		double scale = 5869879.2;
    		String bbox = "5.76417051405295,34.86552015439102,20.291017309471272,45.188646318100695";
    		//String modifiers = "{\"lineColor\":\"ffff0000\"}}";
    		SparseArray<String> modifiers = new SparseArray<String>();
    		SparseArray<String> attributes = new SparseArray<String>();
    		attributes.put(MilStdAttributes.LineColor, "ffff0000");
    		
    		int symStd = 0;
    		MilStdSymbol flot = null;
    		
    		long start = System.currentTimeMillis();//java.lang.System.nanoTime();
	    	Date dStart = new Date();
	    	ImageInfo ii = null;
	    	
    		for(int i = 0; i < 1; i++)
	    	{
    			flot = SECWebRenderer.RenderMultiPointAsMilStdSymbol(id, name, description, symbolCode, controlPoints, altitudeMode, scale, bbox, modifiers, attributes, symStd);
	    	}
    		
    		long stop = System.currentTimeMillis();// java.lang.System.nanoTime();
	    	Date dStop = new Date();
	    	double elapsed = (stop - start) / 1000;
	    	elapsed = (dStop.getTime() - dStart.getTime()) / 1000.0;
	    	
	    	TextView t = (TextView)findViewById(R.id.tvStatus);
	    	t.setText("1k FLOT symbols generated in: " + String.valueOf(elapsed) + " seconds.");
	    	
    		ArrayList<ShapeInfo> sShapes = flot.getSymbolShapes();
    		ArrayList<ShapeInfo> mShapes = flot.getModifierShapes();
    		
    		message.append("Symbol Shape Count: " + String.valueOf(sShapes.size()));
    		message.append(" Modifier Shape Count: " + String.valueOf(mShapes.size()));
    		
    		//Info needed for rendering/////////////////
    		//sShapes.get(0).getPolylines();//Arraylist<Point2D>
    		//sShapes.get(0).getLineColor();
    		//sShapes.get(0).getFillColor();
    		//((BasicStroke)sShapes.get(0).getStroke()).getLineWidth();
                //((BasicStroke)sShapes.get(0).getStroke()).getDashArray();

    		//mShapes.get(0).getModifierString();
    		//mShapes.get(0).getModifierStringPosition();
    		//mShapes.get(0).getModifierStringAngle();
    		////////////////////////////////////////////
    		
    		Log.i(TAG, message.toString());
    		
    		String kml = SECWebRenderer.RenderSymbol(id, name, description, symbolCode, controlPoints, altitudeMode, scale, bbox, modifiers, attributes, 0, symStd);
    		
    		Log.i(TAG, kml);//*/
    		
    	}
    	catch(Exception exc)
    	{
    		Log.e(TAG, exc.getMessage());
    		Log.e(TAG, MainActivity.getStackTrace(exc));
    	}
    }
    
    public void cbModifiersChanged(View view)
    {
    	
    }
    
    public void sPixelSizeChanged(View view)
    {
    	
    }
    
    public static String getStackTrace(Throwable thrown)
    {
        try
        {

            String eol = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder();
            sb.append(thrown.toString());
            sb.append(eol);
            for(StackTraceElement element : thrown.getStackTrace())
            {
                sb.append("        at ");
                sb.append(element);
                sb.append(eol);
            }
            return sb.toString();
        }
        catch(Exception exc)
        {
        	System.err.println(exc.getMessage());
			exc.printStackTrace();
        }
        return "";
    }//*/
    

}
