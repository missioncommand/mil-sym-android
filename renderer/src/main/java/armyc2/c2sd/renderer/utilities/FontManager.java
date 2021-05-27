package armyc2.c2sd.renderer.utilities;

import armyc2.c2sd.singlepointrenderer.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.Log;



public class FontManager {
	
	public static String FONT_UNIT = "UF";
	public static String FONT_SPTG = "SF";
	public static String FONT_MPTG = "MF";
	
	private static FontManager _instance = null;
	private static Boolean _initSuccess = false;
	
	private Typeface _tfUnits = null;
	private Typeface _tfSP = null;
	private Typeface _tfTG = null;
	private String _cacheDir = "";
	
	private FontManager()
	{
		
	}
	
	public static synchronized FontManager getInstance()
    {
      if(_instance == null)
          _instance = new FontManager();

      return _instance;
    }
	
	public synchronized void init(Context context, String cacheDir)
	{
		if(!_initSuccess)
		{
			_cacheDir = cacheDir;
			_tfUnits = loadFont(context, R.raw.unitfont);
			_tfSP = loadFont(context, R.raw.singlepointfont);
			_tfTG = loadFont(context, R.raw.tacticalgraphicsfont);
			if( _tfUnits != null && _tfSP != null && _tfTG != null)
				_initSuccess = true;
			else
				throw new Error("FontManager:  failed to load font files using " + cacheDir + " as the cache directory.");
		}
	}
	
	public Typeface getTypeface(String fontName)
	{
		if(_initSuccess)
		{
			if(fontName == FONT_UNIT)
			{
				return _tfUnits;
			}
			else if(fontName == FONT_SPTG)
			{
				return _tfSP;
			}
			else if(fontName == FONT_MPTG)
			{
				return _tfTG;
			}
			else
				return null;
		}
		throw new Error("FontManager:  Must call \".init(String cacheDir)\" before using");
	}
	
	private Typeface loadFont(Context context, int fontName)
	{
		Typeface tf = null;
		InputStream is = null;
		try
		{
			
			//InputStream fontStream = this.getClass().getClassLoader().getResourceAsStream("assets/fonts/unitfonts.ttf");
			is = context.getResources().openRawResource(fontName);
			
			if(is != null)
			{
				//Log.wtf("SPR.getFont", "we have input stream");
			}
			else
			{
				//Log.wtf(TAG, "Fail to load font file at: " + fontFolder + fontName);
				return null;
			}
					
			/////////////////////
			
			//String sdState = Environment.getExternalStorageState();
			//String cacheDir = Environment.getDownloadCacheDirectory().getAbsolutePath();
			String cacheDir = _cacheDir;
			//Log.wtf("SPR.getFont", "Cache Directory: " + cacheDir);


			String path = cacheDir + "/secrenderer/" + UUID.randomUUID().toString();
			//Log.wtf("SPR.getFont", "Cache Directory: " + path);
			File f = new File(path);
			
			if(f.exists()==false)
			{
				//make directory
				if(f.mkdirs()==false)
				{
					Log.wtf("SPR.getFont", "make temp SD dir \"" + path + "\" fail");
				}
				else
				{
					//Log.wtf("SPR.getFont", "make temp SD dir success");
				}
			}
			else
			{
				//Log.wtf("SPR.getFont", "temp SD dir exists");
			}
			
			String outPath = path + "/secraw.dat";
			try
			{
				byte[] buffer = new byte[is.available()];
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outPath));
				
				int l = 0;
				while((l=is.read(buffer))>0)
				{
					bos.write(buffer,0,l);
				}
				bos.close();
				//Log.wtf("SPR.getFont", "saved temp file");
				bos = null;
				tf = Typeface.createFromFile(outPath);
				if(tf!=null)
				{
					//Log.wtf("SPR.getFont", "created TF from temp file");
				}
				File f2 = new File(outPath);
				if(f2.delete())
				{
					//Log.wtf("SPR.getFont", "deleted temp file");
				}
				File f3 = new File(path);
				if(f3.delete())
				{
					//Log.wtf("SPR.getFont", "deleted temp folder");
				}
			}
			catch(IOException ioe)
			{
				return null;
			}
			catch(Exception exc)
			{
				return null;
			}
		
			/*
			//tf = Typeface.createFromFile("assets/fonts/unitfonts.ttf");
			tf = Typeface.createFromFile("res/raw/unitfont.ttf");
			//AssetManager am = _context.getAssets();
			//Typeface tf = Typeface.createFromAsset(am, "fonts/unitfonts.ttf");
			*/
			
			if(tf != null)
			{
				//Log.wtf("SPR.getFont", "we have typeface");
			}
			else
				Log.wtf("SPR.getFont", "NO TYPEFACE");
			//*/
			
			return tf;
		}
		catch(Exception exc)
		{
			Log.wtf("SPR.getFont", exc.getMessage(),exc);
			if(exc != null)
				ErrorLogger.LogException("SPR", "getFont", exc);
		}
		finally
		{
			silentClose(is);
		}
		return tf;
	}//*/

	private void silentClose(InputStream is){
		if(is == null)
		{
			return;
		}
		try
		{
			is.close();
		}
		catch (IOException ignore)
		{
			// Ignore
		}
	}

	public void testFontFiles()
	{
		Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.black.toARGB());
        fillPaint.setTextSize(48);
        fillPaint.setAntiAlias(true);
        fillPaint.setTextAlign(Align.CENTER);
        fillPaint.setTypeface(_tfUnits);
        
        //for(int i = 999; i < 9132; i++)
        for(int i = 57999; i < 63132; i++)
        {
        	Rect bounds = new Rect();
        	String strFill = String.valueOf((char) i);
            fillPaint.getTextBounds(strFill, 0, 1, bounds);
            
            if(bounds.left < 0)
            {
            	//System.err.println("Char: " + String.valueOf(i) + " has tiny width: " + String.valueOf(bounds.toString()) + " W: " + String.valueOf(bounds.width()) + " H: " + String.valueOf(bounds.width()) + ", valid");
            }
            else
            {
            	System.err.println("Char: " + String.valueOf(i) + " has normal width: " + String.valueOf(bounds.toString()) + " W: " + String.valueOf(bounds.width()) + " H: " + String.valueOf(bounds.width()) + ", invalid");
            }
            
            /*switch(i)
            {
            	case 2166:
            		i=3000;
            		break;
            	case 3084:
            		i=3999;
            		break;
            	case 4085:
            		i=4500;
            		break;
            	case 4533:
            		i=5000;
            		break;
            	case 5261:
            		i=9000;
            		break;
            	case 9131:
            		i=9500;
            		break;
            	default:
            		break;
            }//*/
            
            switch(i)
            {
            	case 59166:
            		i=60000;
            		break;
            	case 60084:
            		i=60999;
            		break;
            	case 61085:
            		i=61500;
            		break;
            	case 61533:
            		i=62000;
            		break;
            	case 62261:
            		i=63000;
            		break;
            	case 63131:
            		i=63500;
            		break;
            	default:
            		break;
            }
        }
        
        
        
	}

}
