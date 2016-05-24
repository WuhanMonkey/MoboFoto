package com.example.imageprocess;

public class rgbValue {
	public int red;
	public int green;
	public int blue;
	
	public rgbValue(int red, int green, int blue){
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public static hsbValue rgb2hsb(rgbValue rgb){
	   float hue, saturation, brightness;
 	   int r=0,g=0,b=0;
 	   
 	   r = rgb.red;
 	   g = rgb.green;
 	   b = rgb.blue;
 	   int cmax = (r > g) ? r : g;
 	   if (b > cmax) cmax = b;
 	   int cmin = (r < g) ? r : g;
 	   if (b < cmin) cmin = b;
 	   	   brightness = ((float) cmax) / 255.0f;
 	   if (cmax != 0)
 	       saturation = ((float) (cmax - cmin)) / ((float) cmax);
 	   else
 	       saturation = 0;
 	   if (saturation == 0)
 	       hue = 0;
 	   else {
 	       float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
 	       float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
 	       float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
 	       if (r == cmax)
 	          hue = bluec - greenc;
 	       else if (g == cmax)
 	          hue = 2.0f + redc - bluec;
 	       else
 	          hue = 4.0f + greenc - redc;
 	          hue = hue / 6.0f;
 	          if (hue < 0)
 	             hue = hue + 1.0f;
 	        }
 	   
		hsbValue hsb = new hsbValue(hue, saturation, brightness);
		return hsb;
	}
	
	public static cieValue rgb2cie(rgbValue rgb){
    	
    	float x=0, y=0, z=0;
    	float X=0, Y=0;
    	x = (float) (0.4124 * rgb.red + 0.3576*rgb.green + 0.1805 * rgb.blue);
    	y = (float) (0.2126 * rgb.red + 0.7152*rgb.green + 0.0722 * rgb.blue);
    	z = (float) (0.0193 *rgb.red + 0.1192*rgb.green + 0.9505 *rgb.blue);
    	X = x/(x+y+z);
    	Y = y/(x+y+z);

		cieValue cie = new cieValue(X, Y);
		return cie;
	}
	
	public static cmykValue rgb2cmyk(rgbValue rgb){
    	
    	float c = 1 - (float)rgb.red / 255;
        float m = 1 - (float)rgb.green / 255;
        float y = 1 - (float)rgb.blue / 255;
        float k = Math.min(Math.min(c,y), m);
        if ( k == 1 ){
        c = m = y = 0;
        }
        else{
        float s = 1 - k;
        c = ( c - k ) / s;
        m = ( m - k ) / s;
        y = ( y - k ) / s;
        }
		cmykValue cmyk = new cmykValue(c,m,y,k);
		return cmyk;
	}
}
