package com.eastearly.richedittextview;
import android.text.style.AbsoluteSizeSpan;

public class HtmlAbsoluteSizeSpan extends AbsoluteSizeSpan{
    public static final int STANDARD_FONT_SIZE = 3;
	private float _basicSize;
	//private static String _fontSizeScaleSample;
	private static String[]scales = null;
	public HtmlAbsoluteSizeSpan(int size,float bkSize) {
		super(size);		
		_basicSize = bkSize;
		// TODO Auto-generated constructor stub
	}
	public static void Init(String fontSizeSampleString)
	{
		scales = fontSizeSampleString.split(",");
	}
	int getHtmlFontSize()
    	{
			for(int i = 0; i < scales.length;i++)
			if(Math.abs(_basicSize * Float.parseFloat(scales[i]) - super.getSize()) < 0.1)
            {
                if(i==0)
                    return 2;
                else if(i==1)
                    return STANDARD_FONT_SIZE;
                else
                    return 5;
            }
    		return STANDARD_FONT_SIZE;
    	}
	public static int switchFontSize(float bkSize,float lastUpdateSize) 
	{
		
		float MIN = STANDARD_FONT_SIZE;
		int index = 0;
		if(scales == null) return -1;
		for(int i =0; i < scales.length;i++)
		{
			float absDiff = (float) Math.abs(Float.parseFloat(scales[i]) - lastUpdateSize/ bkSize);
			if(MIN > absDiff)
			{
				MIN = absDiff;
				index = i;
			}
		}
		if(index==scales.length-1)index=-1;
		return (int) (Float.parseFloat(scales[index+1]) * bkSize + 0.5);
	}
  }

