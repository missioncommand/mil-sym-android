package armyc2.c2sd.renderer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;

import android.util.Log;
import android.util.SparseArray;

import com.caverock.androidsvg.RenderOptions;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.ErrorLogger;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifierInfo;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SVGInfo;
import armyc2.c2sd.renderer.utilities.SVGLookup;
import armyc2.c2sd.renderer.utilities.SVGPath;
import armyc2.c2sd.renderer.utilities.SinglePointLookup;
import armyc2.c2sd.renderer.utilities.SinglePointLookupInfo;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDimensions;
import armyc2.c2sd.renderer.utilities.SymbolID;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.SymbolUtilitiesD;
import armyc2.c2sd.renderer.utilities.UnitFontLookup;
import armyc2.c2sd.renderer.utilities.UnitFontLookupInfo;
import armyc2.c2sd.renderer.utilities.UnitSVGTable;

public class SinglePointSVGRenderer
{

	private String TAG = "armyc2.c2sd.singlepointrenderer.SinglePointRenderer";
	private static SinglePointSVGRenderer _instance = null;
	// private static Bitmap dimensionsBMP = Bitmap.createBitmap(10, 10,
	// Config.ARGB_8888);

	private Typeface _tfUnits = null;
	private Typeface _tfSP = null;
	private Typeface _tfTG = null;

	private SinglePointSVGRenderer()
	{

	}

	public static synchronized SinglePointSVGRenderer getInstance()
	{
		if (_instance == null)
		{
			_instance = new SinglePointSVGRenderer();
		}

		return _instance;
	}

	/**
	 * 
	 * @param symbolID
	 * @param modifiers
	 * @return
	 */
	public ImageInfo RenderUnit(String symbolID, SparseArray<String> modifiers,
			SparseArray<String> attributes)
	{
		// L 1.5 = 2650 pixel units in the svg font file
		//100& normal font size
                //double L1_5 = 2650;
                //50% normal font size
                double L1_5 = 1325;
		Bitmap finalBmp = null;

		Bitmap coreBMP = null;
		try
		{
			Canvas g = null;// new Canvas(dimensionsBMP);

			// get font character indexes
			int fillIndex = -1;
			int frameIndex = -1;
			int symbol1Index = -1;
			int symbol2Index = -1;
			int frameAssumeIndex = -1;
			SVGPath svgFill = null;
			SVGPath svgFrame = null;
			SVGPath svgSymbol1 = null;
			SVGPath svgSymbol2 = null;

			// get attributes
			int alpha = 255;
			Boolean drawAsIcon = false;
			Boolean keepUnitRatio = true;
			int pixelSize = 0;
			Color fillColor = SymbolUtilities
					.getFillColorOfAffiliation(symbolID);
			Color lineColor = SymbolUtilities
					.getLineColorOfAffiliation(symbolID);

			boolean hasDisplayModifiers = false;
			boolean hasTextModifiers = false;
			boolean icon = false;
			int symStd = RendererSettings.getInstance().getSymbologyStandard();

			try
			{

				// get MilStdAttributes
				if (attributes.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
				{
					symStd = Integer.parseInt(attributes
							.get(MilStdAttributes.SymbologyStandard));
				}

				if (symStd > RendererSettings.Symbology_2525B)
				{
					char affiliation = symbolID.charAt(1);
					switch (affiliation)
					{
					case 'P':
					case 'A':
					case 'S':
					case 'G':
					case 'M':
						frameAssumeIndex = fillIndex + 2;
						break;
					}
					/*
					 * if (frameAssumeIndex > 0) { frameAssume = (char)
					 * (frameAssumeIndex); }
					 */
				}

				if (attributes.indexOfKey(MilStdAttributes.PixelSize) >= 0)
				{
					pixelSize = Integer.parseInt(attributes
							.get(MilStdAttributes.PixelSize));
				}
				else
				{
					pixelSize = RendererSettings.getInstance()
							.getDefaultPixelSize();
				}

				if (attributes.indexOfKey(MilStdAttributes.KeepUnitRatio) >= 0)
				{
					keepUnitRatio = Boolean.parseBoolean(attributes
							.get(MilStdAttributes.KeepUnitRatio));
				}

				if (attributes.indexOfKey(MilStdAttributes.DrawAsIcon) >= 0)
				{
					icon = Boolean.parseBoolean(attributes
							.get(MilStdAttributes.DrawAsIcon));
				}

				if (icon)// icon won't show modifiers or display icons
				{
					keepUnitRatio = false;
					hasDisplayModifiers = false;
					hasTextModifiers = false;
					symbolID = symbolID.substring(0, 10) + "-----";
				}
				else
				{
					hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(
							symbolID, modifiers);
					hasTextModifiers = ModifierRenderer.hasTextModifiers(
							symbolID, modifiers, attributes);
				}

				if (attributes.indexOfKey(MilStdAttributes.LineColor) >= 0)
				{
					lineColor = new Color(
							attributes.get(MilStdAttributes.LineColor));
				}
				if (attributes.indexOfKey(MilStdAttributes.FillColor) >= 0)
				{
					fillColor = new Color(
							attributes.get(MilStdAttributes.FillColor));
				}

			}
			catch (Exception excModifiers)
			{
				ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit",
						excModifiers);
			}

			UnitFontLookupInfo ufli = UnitFontLookup.getInstance()
					.getLookupInfo(symbolID, 0);
			fillIndex = UnitFontLookup.getFillCode(symbolID,symStd);
			frameIndex = UnitFontLookup.getFrameCode(symbolID, fillIndex);
			if (ufli != null)
			{
				symbol1Index = ufli.getMapping1(symbolID);
				symbol2Index = ufli.getMapping2();
			}

			if (fillIndex > 0)
			{
				svgFill = UnitSVGTable.getInstance().getSVGPath(
						String.valueOf(fillIndex));
			}
			if (frameIndex > 0)
			{
				svgFrame = UnitSVGTable.getInstance().getSVGPath(
						String.valueOf(frameIndex));
			}
			if (symbol1Index > 0)
			{
				svgSymbol1 = UnitSVGTable.getInstance().getSVGPath(
						String.valueOf(symbol1Index));
			}
			if (symbol2Index > 0)
			{
				svgSymbol2 = UnitSVGTable.getInstance().getSVGPath(
						String.valueOf(symbol2Index));
			}

			// get dimensions for this symbol given the font size & fill index
			Matrix matrix = null;
			double heightL = 1;
			double widthL = 1;

			if (keepUnitRatio)
			{
				RectF rectFrame = svgFrame.getBounds();
				double ratio = pixelSize / L1_5 / 1.5;
				widthL = UnitFontLookup.getUnitRatioWidth(fillIndex);
				heightL = UnitFontLookup.getUnitRatioHeight(fillIndex);
				if (widthL > heightL)
				{
					ratio = ratio * widthL;
				}
				else
				{
					ratio = ratio * heightL;
				}
				pixelSize = (int) ((ratio * L1_5) + 0.5);

			}

			matrix = svgFrame.TransformToFitDimensions(pixelSize, pixelSize);

			RectF rectF = svgFrame.getBounds();

			int w = (int) (rectF.width() + 1.5f);
			int h = (int) (rectF.height() + 1.5f);
			coreBMP = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Point centerPoint = new Point(w / 2, h / 2);

			// draw location
			PointF location = new PointF(0, 0);
			location.x = (rectF.width() / 2.0f);// +0.5f;//use 0.5f to round up
			location.y = -(rectF.height() / 2.0f);

			// get & setup graphics object for destination BMP
			g = new Canvas(coreBMP);

			// draw symbol to BMP
			if (svgFill != null)
			{
				svgFill.Transform(matrix);
				svgFill.Draw(g, null, 0, fillColor, null);
			}
			if (svgFrame != null)
			{
				svgFrame.Draw(g, null, 0, lineColor, null);
			}
			if (svgSymbol2 != null)
			{
				svgSymbol2.Draw(g, null, 0, ufli.getColor2(), matrix);
			}
			if (svgSymbol1 != null)
			{
				svgSymbol1.Draw(g, null, 0, ufli.getColor1(), matrix);
			}

			RectF coreDimensions = new RectF(0, 0, w, h);
			Rect finalDimensions = new Rect(0, 0, w, h);

			// adjust centerpoint for HQStaff if present
			if (SymbolUtilities.isHQ(symbolID))
			{
				Point point1 = new Point();
				Point point2 = new Point();
				String affiliation = symbolID.substring(1, 2);
				if (affiliation == ("F") || affiliation == ("A")
						|| affiliation == ("D") || affiliation == ("M")
						|| affiliation == ("J") || affiliation == ("K")
						|| affiliation == ("N") || affiliation == ("L"))
				{
					point1.x = 0;
					point1.y = (coreBMP.getHeight());
					point2.x = point1.x;
					point2.y = point1.y + coreBMP.getHeight();
				}
				else
				{
					point1.x = 1;
					point1.y = (coreBMP.getHeight() / 2);
					point2.x = point1.x;
					point2.y = point1.y + coreBMP.getHeight();
				}
				centerPoint = point2;
			}

			// process display modifiers
			// List<PathInfo> shapes = null;// new
			// List<SECRendererUtils.PathInfo>();
			// shapes = ProcessUnitDisplayModifiers(symbolID, bmp, modifiers,
			// attributes);
			// process label modifiers
			finalBmp = Bitmap.createBitmap(finalDimensions.right,
					finalDimensions.bottom, Bitmap.Config.ARGB_8888);
			Canvas dest = new Canvas(finalBmp);

			dest.drawBitmap(coreBMP, new Matrix(), null);
			// bool success = GDI.CopyImage(coreBMP, finalBmp,
			// (int)(coreDimensions.X), (int)(coreDimensions.Y));

			Rect symbolBounds = new Rect(0, 0, finalBmp.getWidth(),
					finalBmp.getHeight());
			ImageInfo ii = new ImageInfo(finalBmp, new Point(centerPoint.x,
					centerPoint.y), symbolBounds);

			// test
			// ////
			// return ii;

			// //////////////////////////////////////////////////////////////////
			ImageInfo iinew = null;
			// process display modifiers
			if (hasDisplayModifiers)
			{
				iinew = ModifierRenderer.processUnitDisplayModifiers(ii,
						symbolID, modifiers, hasTextModifiers, attributes);
			}

			if (iinew != null)
			{
				ii = iinew;
			}
			iinew = null;

			// process text modifiers
			if (hasTextModifiers)
			{
				iinew = ModifierRenderer.processUnitTextModifiers(ii, symbolID,modifiers, attributes);
			}

			if (iinew != null)
			{
				ii = iinew;
			}
			iinew = null;

			// cleanup///////////////////////////////////////////////////////////
			// bmp.recycle();

			// //////////////////////////////////////////////////////////////////

			if (icon == true)
			{
				return ii.getSquareImageInfo();
			}
			else
			{
				return ii;
			}
		}
		catch (Exception exc)
		{
			ErrorLogger.LogException("SinglePointSVGRenderer", "RenderUnit",
					exc);
			return null;
		}
	}

	/**
	 * 
	 * @param symbolID
	 * @param modifiers
	 * @return
	 */
	@SuppressWarnings("unused")
	public ImageInfo RenderSP(String symbolID, SparseArray<String> modifiers)
	{
		ImageInfo temp = null;
		String basicSymbolID = null;
		float fontSize = RendererSettings.getInstance().getSPFontSize();
		Color lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
		Color fillColor = null;// SymbolUtilities.getFillColorOfAffiliation(symbolID);
		int alpha = -1;

		int symStd = RendererSettings.getInstance().getSymbologyStandard();
		// fill character
		int charFillIndex = -1;
		// frame character
		int charFrameIndex = -1;

		SymbolDef sd = null;

		Paint fillPaint = null;
		Paint framePaint = null;

		SinglePointLookupInfo lookup = null;

		Rect symbolBounds = null;
		RectF fullBounds = null;
		Bitmap fullBMP = null;

		try
		{
			if (modifiers == null)
			{
				modifiers = new SparseArray<String>();
			}
			// get MilStdAttributes
			if (modifiers.indexOfKey(MilStdAttributes.SymbologyStandard) >= 0)
			{
				symStd = Integer.parseInt(modifiers
						.get(MilStdAttributes.SymbologyStandard));
			}

			// get symbol info
			basicSymbolID = SymbolUtilities.getBasicSymbolID(symbolID);
			lookup = SinglePointLookup.getInstance().getSPLookupInfo(
					basicSymbolID, symStd);
			if (lookup == null)// if lookup fails, fix code/use unknown symbol
								// code.
			{
				// if symbolID bad, do best to find a workable code
				if (modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1) != null)
				{
					modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2,
							modifiers.get(ModifiersTG.H_ADDITIONAL_INFO_1));
				}
				modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1,
						symbolID.substring(0, 10));

				symbolID = "G" + SymbolUtilities.getAffiliation(symbolID) + "G"
						+ SymbolUtilities.getStatus(symbolID) + "GPP---****X";
				lookup = SinglePointLookup.getInstance().getSPLookupInfo(
						basicSymbolID, symStd);
				lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolID);
				fillColor = null;// SymbolUtilities.getFillColorOfAffiliation(symbolID);
			}

			if (modifiers.indexOfKey(MilStdAttributes.LineColor) >= 0)
			{
				lineColor = new Color(modifiers.get(MilStdAttributes.LineColor));
			}
			if (modifiers.indexOfKey(MilStdAttributes.FillColor) >= 0)
			{
				lineColor = new Color(modifiers.get(MilStdAttributes.FillColor));
			}

			if (SymbolUtilities.isTGSPWithFill(symbolID) && fillColor != null)
			{
				fillPaint = new Paint();
				fillPaint.setStyle(Paint.Style.FILL);
				fillPaint.setColor(fillColor.toARGB());
				fillPaint.setTextSize(fontSize);
				fillPaint.setAntiAlias(true);
				fillPaint.setTextAlign(Align.CENTER);
				fillPaint.setTypeface(_tfSP);
			}

			framePaint = new Paint();
			framePaint.setStyle(Paint.Style.FILL);
			framePaint.setColor(lineColor.toARGB());
			framePaint.setTextSize(fontSize);
			framePaint.setAntiAlias(true);
			framePaint.setTextAlign(Align.CENTER);
			framePaint.setTypeface(_tfSP);

			// Check if we need to set 'N' to "ENY"
			if (symbolID.charAt(1) == 'H'
					&& modifiers.indexOfKey(MilStdAttributes.DrawAsIcon) >= 0
					&& (Boolean.parseBoolean(modifiers
							.get(MilStdAttributes.DrawAsIcon)) == false))
			{
				modifiers.put(ModifiersTG.N_HOSTILE, "ENY");
			}

		}
		catch (Exception excModifiers)
		{
			ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit",
					excModifiers);
		}

		try
		{
			// get fill character
			// get frame character
			// get symbol info
			charFrameIndex = -1;// SinglePointLookup.instance.getCharCodeFromSymbol(symbolID);
			charFillIndex = -1;

			if (SymbolUtilities.getStatus(symbolID).equals("A"))
			{
				charFrameIndex = lookup.getMappingA();
			}
			else
			{
				charFrameIndex = lookup.getMappingP();
			}

			if (SymbolUtilities.isTGSPWithFill(symbolID) && fillColor != null)
			{
				String fillID = SymbolUtilities.getTGFillSymbolCode(symbolID);
				if (fillID != null)
				{
					charFillIndex = SinglePointLookup.getInstance()
							.getCharCodeFromSymbol(fillID, symStd);
				}
			}

			// dimensions of the unit at specified font size
			RectF rect = new RectF(0, 0, lookup.getWidth(), lookup.getHeight());

			if (fontSize != 60.0)// adjust boundaries ratio if font size is not
									// at the default setting.
			{
				double ratio = fontSize / 60;

				rect = new RectF(0, 0, Math.round(rect.width() * ratio),
						Math.round(rect.height() * ratio));
			}

			boolean symbolOutlineEnabled = false;
			int symbolOutlineSize = RendererSettings.getInstance()
					.getSinglePointSymbolOutlineWidth();
			if (symbolOutlineEnabled == false)
			{
				symbolOutlineSize = 0;
			}

			// matrix to place the symbol centered in the MilStdBmp
			Matrix matrix = new Matrix();
			Point centerPoint = null;
			centerPoint = SymbolDimensions.getSymbolCenter(
					lookup.getBasicSymbolID(), rect);
			matrix.postTranslate(centerPoint.x, centerPoint.y);

			if (symbolOutlineEnabled)
			{ // adjust matrix and centerpoint to account for outline if present
				matrix.postTranslate(symbolOutlineSize, symbolOutlineSize);
				centerPoint.offset(symbolOutlineSize, symbolOutlineSize);
				rect = new RectF(0, 0,
						(rect.width() + (symbolOutlineSize * 2)),
						(rect.height() + (symbolOutlineSize * 2)));
			}

			// Draw glyphs to bitmap
			Bitmap bmp = Bitmap.createBitmap((int) (rect.width() + 0.5),
					(int) (rect.height() + 0.5), Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);

			symbolBounds = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

			String strFill = null;
			String strFrame = null;
			if (charFillIndex > 0)
			{
				strFill = String.valueOf((char) charFillIndex);
			}
			if (charFrameIndex > 0)
			{
				strFrame = String.valueOf((char) charFrameIndex);
			}

			canvas.setMatrix(matrix);
			if (strFill != null)
			{
				canvas.drawText(strFill, 0, 0, fillPaint);
			}
			if (strFrame != null)
			{
				canvas.drawText(strFrame, 0, 0, framePaint);
			}
			ImageInfo ii = new ImageInfo(bmp, centerPoint, symbolBounds);

			List<ModifierInfo> shapes = new ArrayList<ModifierInfo>();
			ModifierInfo miTemp = null;

			// process integral text
			if (modifiers != null
					&& modifiers.indexOfKey(MilStdAttributes.DrawAsIcon) >= 0
					&& (Boolean.parseBoolean(modifiers
							.get(MilStdAttributes.DrawAsIcon)) == false))// if
																			// drawAsIcon,
																			// don't
																			// draw.
			{
				/*
				 * miTemp = CreateTGSPIntegralText(symbolID, symbolBounds,
				 * lineColor); if(miTemp != null) shapes.addAll(miTemp); miTemp
				 * = null//
				 */
			}

			// draw modifiers
			/*
			 * if(SymbolUtilities.isTGSPWithSpecialModifierLayout(symbolID)) {
			 * miTemp = ProcessSPTGSpecialModifiers(symbolID, symbolBounds,
			 * modifiers,lineColor);
			 * 
			 * } else { miTemp = ProcessSPTGModifiers(symbolID, symbolBounds,
			 * modifiers,lineColor); } if(miTemp != null) shapes.addAll(miTemp);
			 * miTemp = null
			 * 
			 * //draw direction of movement arrow if(modifiers != null &&
			 * modifiers[ModifiersUnits.Q_DIRECTION_OF_MOVEMENT] != null) {
			 * miTemp = CreateDirectionOfMovementArrow(symbolID, symbolBounds,
			 * modifiers); if(miTemp != null) shapes.addItem(miTemp); }//
			 */
			// get full bounds
			// draw modifiers
			// get full bounds
			float ULX = 0;
			float ULY = 0;
			float LRX = symbolBounds.width();
			float LRY = symbolBounds.height();
			int offset = 0;
			int outlineSize = RendererSettings.getInstance()
					.getTextOutlineWidth();
			int shapesLength = shapes.size();

			if (shapes != null && shapesLength > 0)
			{
				for (int i = 0; i < shapesLength; i++)
				{
					if (shapes.get(i).getText() != null)
					{
						miTemp = shapes.get(i);
						if (miTemp.getKey() == "shadow")
						{
							offset = outlineSize;
						}
						else
						{
							offset = 0;
						}

						RectF rTemp = miTemp.getBounds();
						Paint pTemp = miTemp.getPaint();

						if ((rTemp.left - offset) < ULX)
						{
							ULX = rTemp.left - offset;
						}
						if (rTemp.top - rTemp.height() - offset < ULY)
						{
							ULY = rTemp.top - rTemp.height() - offset;
						}
						if (rTemp.left + rTemp.width() + offset > LRX)
						{
							LRX = Math.round(rTemp.left) + rTemp.width()
									+ offset;
						}
						if (rTemp.top + offset > LRY)
						{
							LRY = Math.round(rTemp.top) + pTemp.descent()
									+ offset;
						}
					}
					else if (shapes.get(i).getShape() != null)
					{
						miTemp = shapes.get(i);
						RectF rTemp = miTemp.getBounds();
						if (rTemp.left < ULX)
						{
							ULX = Math.round(rTemp.left);
						}
						if (rTemp.top < ULY)
						{
							ULY = Math.round(rTemp.top);
						}
						if (rTemp.left + rTemp.width() > LRX)
						{
							LRX = Math.round(rTemp.left + rTemp.width());
						}
						if (rTemp.top + rTemp.height() > LRY)
						{
							LRY = Math.round(rTemp.top + rTemp.height());
						}
					}
				}
			}

			fullBounds = new RectF(0, 0, LRX - ULX, LRY - ULY);
			Matrix centerMatrix = new Matrix();

			int transX = 0;
			int transY = 0;
			if (ULX < 0)
			{
				transX = (int) (ULX * -1);
			}
			if (ULY < 0)
			{
				transY = (int) (ULY * -1);
			}
			centerMatrix = new Matrix();
			centerMatrix.reset();
			centerMatrix.postTranslate(transX, transY);

			fullBMP = Bitmap.createBitmap((int) fullBounds.width(),
					(int) fullBounds.height(), Config.ARGB_8888);
			Canvas fullCanvas = new Canvas(fullBMP);
			Matrix tempMatrix = null;

			if (shapes != null && shapesLength > 0)
			{
				for (int j = 0; j < shapesLength; j++)
				{
					tempMatrix = new Matrix();
					tempMatrix.reset();
					if (shapes.get(j).getText() != null)
					{

						miTemp = shapes.get(j);
						PointF drawPoint = miTemp.getDrawPoint();
						tempMatrix.postTranslate((int) drawPoint.x,
								(int) drawPoint.y);
						tempMatrix.postTranslate(transX, transY);
						if (miTemp.getKey().equals("shadow"))
						{
							// DrawOutline(fullBMP, miTemp, tempMatrix);
						}
						else
						{
							fullCanvas.setMatrix(tempMatrix);// matrix handles
																// positioning.
							fullCanvas.drawText(miTemp.getText(), 0, 0,
									miTemp.getPaint());
						}
					}
					else if (shapes.get(j).getShape() != null)
					{
						miTemp = shapes.get(j);

						tempMatrix.postTranslate((int) transX, (int) transY);

						fullCanvas.setMatrix(tempMatrix);// matrix handles
															// positioning.
						miTemp.getShape().draw(fullCanvas, miTemp.getPaint());
					}
				}
			}

			Paint ptCopy = new Paint();
			ptCopy.setAntiAlias(false);
			fullCanvas.drawBitmap(bmp, transX, transY, ptCopy);

			centerPoint.x = centerPoint.x + transX;
			centerPoint.y = centerPoint.y + transY;
			symbolBounds = new Rect(transX, transY, symbolBounds.width(),
					symbolBounds.height());

			ii = new ImageInfo(fullBMP, centerPoint, symbolBounds);

			// cleanup
			bmp.recycle();
			bmp = null;
			canvas = null;
			symbolBounds = null;
			fullBMP = null;
			fullBounds = null;
			fullCanvas = null;
			centerPoint = null;
			fillPaint = null;
			framePaint = null;

			lookup = null;

			return ii;

		}
		catch (Exception exc)
		{
			ErrorLogger.LogException("MilStdIconRenderer", "RenderSP", exc);
		}
		return temp;
	}

	/**
	 * Tries to get a valid UnitFontLookupInfo object when the symbolID is
	 * poorly formed or there's no match in the lookup. Use this if you get a
	 * null return value from:
	 * "UnitFontLookupC.getInstance().getLookupInfo(symbolID)" or "CanRender"
	 * returns false.
	 * 
	 * @param symbolID
	 * @return
	 */
	private UnitFontLookupInfo ResolveUnitFontLookupInfo(String symbolID,
			int symStd)
	{
		String id = symbolID;
		UnitFontLookupInfo lookup = null;
		String affiliation = "";
		String status = "";
		if (id != null && id.length() >= 10)// if lookup fails, fix code/use
											// unknown symbol code.
		{
			StringBuilder sb = new StringBuilder("");
			sb.append(id.charAt(0));

			if (SymbolUtilities.hasValidAffiliation(id) == false)
			{
				sb.append('U');
				affiliation = "U";
			}
			else
			{
				sb.append(id.charAt(1));
				affiliation = id.substring(1, 2);
			}

			if (SymbolUtilities.hasValidBattleDimension(id) == false)
			{
				sb.append('Z');
				sb.replace(0, 1, "S");
			}
			else
			{
				sb.append(id.charAt(2));
			}

			if (SymbolUtilities.hasValidStatus(id) == false)
			{
				sb.append('P');
				status = "P";
			}
			else
			{
				sb.append(id.charAt(3));
				status = id.substring(3, 4);
			}

			sb.append("------");
			if (id.length() >= 15)
			{
				sb.append(id.substring(10, 15));
			}
			else
			{
				sb.append("*****");
			}
			id = sb.toString();

			lookup = UnitFontLookup.getInstance().getLookupInfo(id, symStd);
		}
		else if (symbolID == null || symbolID.equals(""))
		{
			lookup = UnitFontLookup.getInstance().getLookupInfo(
					"SUZP------*****", symStd);
		}
		return lookup;
	}

	public Bitmap getTestSymbol()
	{
		Bitmap temp = null;
		try
		{
			temp = Bitmap.createBitmap(70, 70, Config.ARGB_8888);

			Canvas canvas = new Canvas(temp);

			if (canvas.isHardwareAccelerated())
			{
				System.out.println("HW acceleration supported");
			}
			// canvas.drawColor(Color.WHITE);

			// Typeface tf = Typeface.createFromAsset(_am,
			// "fonts/unitfonts.ttf");
			Typeface tf = _tfUnits;

			Paint fillPaint = new Paint();
			fillPaint.setStyle(Paint.Style.FILL);
			fillPaint.setColor(Color.CYAN.toInt());
			fillPaint.setTextSize(50);
			fillPaint.setAntiAlias(true);
			fillPaint.setTextAlign(Align.CENTER);
			fillPaint.setTypeface(tf);

			Paint framePaint = new Paint();
			framePaint.setStyle(Paint.Style.FILL);
			framePaint.setColor(Color.BLACK.toInt());
			framePaint.setTextSize(50);
			framePaint.setAntiAlias(true);
			framePaint.setTextAlign(Align.CENTER);
			framePaint.setTypeface(tf);

			Paint symbolPaint = new Paint();
			symbolPaint.setStyle(Paint.Style.FILL);
			symbolPaint.setColor(Color.BLACK.toInt());
			symbolPaint.setTextSize(50);
			symbolPaint.setAntiAlias(true);
			symbolPaint.setTextAlign(Align.CENTER);
			symbolPaint.setTypeface(tf);

			String strFill = String.valueOf((char) 800);
			String strFrame = String.valueOf((char) 801);
			String strSymbol = String.valueOf((char) 1121);

			canvas.drawText(strFill, 35, 35, fillPaint);
			canvas.drawText(strFrame, 35, 35, framePaint);
			canvas.drawText(strSymbol, 35, 35, symbolPaint);

			FontMetrics mf = framePaint.getFontMetrics();
			float height = mf.bottom - mf.top;
			float width = fillPaint.measureText(strFrame);

			Log.i(TAG, "top: " + String.valueOf(mf.top));
			Log.i(TAG, "bottom: " + String.valueOf(mf.bottom));
			Log.i(TAG, "ascent: " + String.valueOf(mf.ascent));
			Log.i(TAG, "descent: " + String.valueOf(mf.descent));
			Log.i(TAG, "leading: " + String.valueOf(mf.leading));
			Log.i(TAG, "width: " + String.valueOf(width));
			Log.i(TAG, "height: " + String.valueOf(height));

		}
		catch (Exception exc)
		{
			Log.e(TAG, exc.getMessage());
			Log.e(TAG, getStackTrace(exc));
		}

		return temp;
	}// */

	public void logError(String tag, Throwable thrown)
	{
		if (tag == null || tag == "")
		{
			tag = "singlePointRenderer";
		}

		String message = thrown.getMessage();
		String stack = getStackTrace(thrown);
		if (message != null)
		{
			Log.e(tag, message);
		}
		if (stack != null)
		{
			Log.e(tag, stack);
		}
	}

	public String getStackTrace(Throwable thrown)
	{
		try
		{
			if (thrown != null)
			{
				if (thrown.getStackTrace() != null)
				{
					String eol = System.getProperty("line.separator");
					StringBuilder sb = new StringBuilder();
					sb.append(thrown.toString());
					sb.append(eol);
					for (StackTraceElement element : thrown.getStackTrace())
					{
						sb.append("        at ");
						sb.append(element);
						sb.append(eol);
					}
					return sb.toString();
				}
				else
				{
					return thrown.getMessage() + "- no stack trace";
				}
			}
			else
			{
				return "no stack trace";
			}
		}
		catch (Exception exc)
		{
			Log.e("getStackTrace", exc.getMessage());
		}
		return thrown.getMessage();
	}// */
	/*
	 * private static String PrintList(ArrayList list) { String message = "";
	 * for(Object item : list) {
	 * 
	 * message += item.toString() + "\n"; } return message; }//
	 */
	/*
	 * private static String PrintObjectMap(Map<String, Object> map) {
	 * Iterator<Object> itr = map.values().iterator(); String message = "";
	 * String temp = null; while(itr.hasNext()) { temp =
	 * String.valueOf(itr.next()); if(temp != null) message += temp + "\n"; }
	 * //ErrorLogger.LogMessage(message); return message; }//
	 */

	public Bitmap AndroidSVGTest()
	{
		String lawEnforcement = "30031000002003000000";
		String spaceStation = "30030500001207000000";
		String airShip = "30030100001105000000";
		String howitzerH = "30031500001109030000";
		String PatrolBoat = "30033000001205020000";
		String UXO = "30033600001200000000";
		String ActionPoint = "30032500001301000000";
		String Ambush = "30032500001301000000";
		String DecisionPoint = "30032500001307000000";
		String Drizzle = "30034500001605020000";
		String IED = "30034000001103000000";
		String DataManipulation = "30036000001604000000";
		String symbolID = null;
		String frameID = null;
		String iconID = null;
		SVGInfo siFrame = null;
		SVGInfo siIcon = null;
		SVG mySVG = null;
		Bitmap bmp = null;
		Canvas cv = null;
		Paint myPaint = new Paint();
		int top = 0;
		int left = 0;
		int width = 0;
		int height = 0;
		String svgStart = null;
		String strSVG = null;

		String strSVGFile = "<svg version=\"1.1\" id=\"Version_1.0\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\"" +
		"y=\"0px\" width=\"612px\" height=\"792px\" viewBox=\"0 0 612 792\" enable-background=\"new 0 0 612 792\" xml:space=\"preserve\">" +
		"<g id=\"frame\">" +
		"<ellipse id=\"_x3C_path_x3E_\" fill=\"#80E0FF\" stroke=\"#000000\" stroke-width=\"5\" cx=\"306\" cy=\"396\" rx=\"144\" ry=\"144\"/>" +
		"</g>" +
		"</svg>";
		String strSVGRed = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n" +
				"     viewBox=\"0 0 100 100\">\n" +
				"\n" +
				"   <circle cx=\"50\" cy=\"50\" r=\"50\" fill=\"red\" />\n" +
				"   \n" +
				"</svg>";
		try
		{
			symbolID = spaceStation;
			//symbolID = SymbolID.setAffiliation(symbolID, SymbolID.StandardIdentity_Affiliation_Hostile_Faker);
			frameID = SVGLookup.getFrameID(symbolID);
			iconID = SVGLookup.getMainIconID(symbolID);
			siFrame = SVGLookup.getInstance().getSVGLInfo(frameID);
			siIcon = SVGLookup.getInstance().getSVGLInfo(iconID);
			top = Math.round(siFrame.getBbox().top);
			left = Math.round(siFrame.getBbox().left);
			width = Math.round(siFrame.getBbox().width());
			height = Math.round(siFrame.getBbox().height());
			if(siFrame.getBbox().bottom > 400)
				svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
			else
				svgStart = "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";

			String strSVGFrame = siFrame.getSVG();
			//change fill color test
			//String strSVGFrame = SymbolUtilitiesD.setSVGFrameColors(symbolID,siFrame.getSVG(),null,"#FF0000");

			strSVG = svgStart + strSVGFrame + siIcon.getSVG() + "</svg>";
			Log.i(TAG, strSVG);

			//Render Friendly Frame
			/*mySVG = SVG.getFromString(strSVGFile);
			bmp = Bitmap.createBitmap(612, 792, Config.ARGB_8888);
			cv = new Canvas(bmp);
			myPaint.setColor(Color.LIGHT_GRAY.toInt());
			cv.drawRect(new RectF(0,0,612,792), myPaint);
			mySVG.renderToCanvas(cv);//*/

			/*//Render Red Square
			mySVG = SVG.getFromString(strSVGRed);
			bmp = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
			cv = new Canvas(bmp);
			myPaint.setColor(Color.LIGHT_GRAY.toInt());
			cv.drawRect(new RectF(0,0,100,100), myPaint);
			mySVG.renderToCanvas(cv);//*/

			//Render Symbol
			mySVG = SVG.getFromString(strSVG); //get svg string from lookup
			int pixelSize = 300;//if icon, same value for width and heigh so it looks nice on a form
			//for a map in needs to match the shape of the symbol.

			//get ratio to determine new bitmap size
			float ratio = 1;
			if (width >= height)
				ratio = (float)pixelSize / width;
			else
				ratio = (float)pixelSize / height;

			int bWidth = Math.round(ratio * width);
			int bHeight = Math.round(ratio * height);

			//make bitmap based on dimensions of scaled up/down symbol
			bmp = Bitmap.createBitmap(bWidth, bHeight, Config.ARGB_8888);

			//fill and outline imageView
			cv = new Canvas(bmp);
			myPaint.setColor(Color.LIGHT_GRAY.toInt());
			cv.drawRect(new RectF(0,0,bmp.getWidth(), bmp.getHeight()),myPaint);//fill target imageView

			//Draw SVG to Bitmap
			mySVG.setDocumentViewBox(left,top,width,height);
			mySVG.renderToCanvas(cv);//*/



			/*//Color change test
			String  hostile = "Satellite { fill: red; }";
			//String  hostile = "path { fill: red; }";
			RenderOptions renderOpts = RenderOptions.create().css(hostile);
			//renderOpts.viewPort(siFrame.getBbox().left, siFrame.getBbox().top, siFrame.getBbox().width(), siFrame.getBbox().height());
			mySVG.renderToCanvas(cv, renderOpts);//*/

			//draw outline
			myPaint.setStyle(Paint.Style.STROKE);
			myPaint.setColor(Color.RED.toInt());
			cv.drawRect(new RectF(0,0,bWidth-1,bHeight-1), myPaint);//*/



		}
		catch(SVGParseException spe)
		{
			Log.e("AndroidSVGTest",spe.getMessage());
		}
		catch(Exception exc)
		{
			Log.e("AndroidSVGTest",exc.getMessage());
		}
			return bmp;


	}

}
