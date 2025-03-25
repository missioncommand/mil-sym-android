package armyc2.c5isr.renderer.utilities;

/**
 * Integer based Color class with utility functions
 */
public class Color {
	
    /**
     * The color white.  In the default sRGB space.
     */
    public final static Color white = new Color(255, 255, 255);

    /**
     * The color white.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color WHITE = white;

    /**
     * The color light gray.  In the default sRGB space.
     */
    public final static Color lightGray = new Color(192, 192, 192);

    /**
     * The color light gray.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color LIGHT_GRAY = lightGray;

    /**
     * The color gray.  In the default sRGB space.
     */
    public final static Color gray      = new Color(128, 128, 128);

    /**
     * The color gray.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color GRAY = gray;

    /**
     * The color dark gray.  In the default sRGB space.
     */
    public final static Color darkGray  = new Color(64, 64, 64);

    /**
     * The color dark gray.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color DARK_GRAY = darkGray;

    /**
     * The color black.  In the default sRGB space.
     */
    public final static Color black 	= new Color(0, 0, 0);
    
    /**
     * The color black.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color BLACK = black;
    
    /**
     * The color red.  In the default sRGB space.
     */
    public final static Color red       = new Color(255, 0, 0);

    /**
     * The color red.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color RED = red;

    /**
     * The color pink.  In the default sRGB space.
     */
    public final static Color pink      = new Color(255, 175, 175);

    /**
     * The color pink.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color PINK = pink;

    /**
     * The color orange.  In the default sRGB space.
     */
    public final static Color orange 	= new Color(255, 128, 0);

    /**
     * The color orange.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color ORANGE = orange;

    /**
     * The color yellow.  In the default sRGB space.
     */
    public final static Color yellow 	= new Color(255, 255, 0);

    /**
     * The color yellow.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color YELLOW = yellow;

    /**
     * The color green.  In the default sRGB space.
     */
    public final static Color green 	= new Color(0, 255, 0);

    /**
     * The color green.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color GREEN = green;

    /**
     * The color magenta.  In the default sRGB space.
     */
    public final static Color magenta	= new Color(255, 0, 255);

    /**
     * The color magenta.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color MAGENTA = magenta;

    /**
     * The color cyan.  In the default sRGB space.
     */
    public final static Color cyan 	= new Color(0, 255, 255);

    /**
     * The color cyan.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color CYAN = cyan;

    /**
     * The color blue.  In the default sRGB space.
     */
    public final static Color blue 	= new Color(0, 0, 255);

    /**
     * The color blue.  In the default sRGB space.
     * @since 1.4
     */
    public final static Color BLUE = blue;
    
    private int _A=255;
	private int _R=0;
	private int _G=0;
	private int _B=0;//default to black
    
    public Color(int R, int G, int B)
    {
    	_A = 255;
		_R = R;
		_G = G;
		_B = B;
    }
    
    public Color(int R, int G, int B, int A)
    {
    	_A = A;
		_R = R;
		_G = G;
		_B = B;
    }
    
    public Color(Color color)
    {
    	if(color != null)
    	{
        	_A = color.getAlpha();
        	_R = color.getRed();
        	_G = color.getGreen();
        	_B = color.getBlue();
    	}
    	else
    	{
    		_A = 255;
    		_R = 0;
	    	_G = 0;
	    	_B = 0;
    	}
    }
    
    public Color(int color)
    {
		_A = getAlphaFromColor(color);
		_R = getRedFromColor(color);
		_G = getGreenFromColor(color);
		_B = getBlueFromColor(color);
    }

    public Color(String hexString)
    {
    	Color temp = RendererUtilities.getColorFromHexString(hexString);
    	if(temp != null)
    	{
	    	_A = temp.getAlpha();
	    	_R = temp.getRed();
	    	_G = temp.getGreen();
	    	_B = temp.getBlue();
    	}
    	else
    	{
    		_A = 255;
    		_R = 0;
	    	_G = 0;
	    	_B = 0;
    	}
    }
    
    public Color(Object color)
    {
    	
    	Color cTemp;
    	int iTemp;
    	try
    	{
	    	if(color instanceof String)
	    	{
	    		cTemp = RendererUtilities.getColorFromHexString((String)color);
	    		if(cTemp != null)
	    		{
		        	_A = cTemp.getAlpha();
		        	_R = cTemp.getRed();
		        	_G = cTemp.getGreen();
		        	_B = cTemp.getBlue();
	    		}
	    	}
	    	else if(color instanceof Integer)
	    	{
	    		iTemp = (Integer)color;
	    		_A = getAlphaFromColor(iTemp);
	    		_R = getRedFromColor(iTemp);
	    		_G = getGreenFromColor(iTemp);
	    		_B = getBlueFromColor(iTemp);
	    	}
	    	else
	    	{
	    		_A=255;
	    		_R=0;
	    		_G=0;
	    		_B=0;
	    	}
    	}
    	catch(Exception exc)
    	{
    		_A=255;
    		_R=0;
    		_G=0;
    		_B=0;
    	}
    }
    
    public int toARGB()
    {
    	int returnVal = 0;
		returnVal = (_A << 24) + ((_R & 0xFF) << 16) + ((_G & 0xFF) << 8) + (_B & 0xFF);
		return returnVal;
    }

	/**
	 * Converts color object to hex string.  You may have to prepend with '#' or "0x"
	 * depending on how you need to use the string.
	 * @return 8 character hex string
	 */
	public String toHexString()
    {
		//return Integer.toHexString(this.toARGB());
		return String.format("%08x", this.toInt());
		//return String.format("0x%08x", this.toInt());
    }
    
    @Override
    public String toString()
    {
    	return "Color{A=" + String.valueOf(_A) + ",R=" + String.valueOf(_R) + 
				",G=" + String.valueOf(_G) + ",B=" + String.valueOf(_B) + "}";
    }
    
    public int getRed()
    {
    	return _R;
    }
    
    public int getGreen()
    {
    	return _G;
    }
    
    public int getBlue()
    {
    	return _B;
    }
    
    public int getAlpha()
    {
    	return _A;
    }

	/**
	 *
	 * @param alpha 0-255
	 */
	public void setAlpha(int alpha)
	{
		_A = alpha;
	}
    
    public int toInt()
    {
    	return android.graphics.Color.argb(_A, _R, _G, _B);
    }
    
    /**
	 * get alpha value from uint
	 * */
	private int getAlphaFromColor(int color)
	{
		int alpha = 255;
		if(color > 16777215)
			alpha = (color >>> 24);
		return alpha;
	}
	/**
	 * get red value from uint
	 * */
	private int getRedFromColor(int color)
	{
		int red = 255;
		red = (color >> 16) & 0xFF;
		return red;
	}
	/**
	 * get green value from uint
	 * */
	private int getGreenFromColor(int color)
	{
		int green = 255;
		green = (color >> 8) & 0xFF;
		return green;
	}
	/**
	 * get blue value from uint
	 * */
	private int getBlueFromColor(int color)
	{
		int blue = 255;
		if(color > 16777215)
			blue = color & 0x000000FF;
		else
			blue = color & 0x0000FF;
		return blue;
	}
	
	

}
