/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2009 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.engine.export;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;
import net.sf.jasperreports.engine.util.JRProperties;
import net.sf.jasperreports.engine.util.JRStringUtil;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.MaxFontSizeFinder;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id$
 */
public class TextRenderer
{
	public static final FontRenderContext LINE_BREAK_FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

	private Graphics2D grx;
	private int x;
	private int y;
	private int topPadding;
	private int leftPadding;
	private float formatWidth;
	private float verticalOffset;
	private int tabStop;
	private float lineSpacingFactor;
	private float leadingOffset;
	private float textHeight;
	private float drawPosY;
	private float drawPosX;
	private boolean isMaxHeightReached;
	private HorizontalAlignEnum horizontalAlignment;
	private int fontSize;
	
	/**
	 * 
	 */
	private MaxFontSizeFinder maxFontSizeFinder;
	
	/**
	 * 
	 */
	private boolean isMinimizePrinterJobSize = true;
	private boolean ignoreMissingFont;

	
	/**
	 * 
	 */
	public static TextRenderer getInstance()
	{
		return 
			new TextRenderer(
				JRProperties.getBooleanProperty(JRGraphics2DExporter.MINIMIZE_PRINTER_JOB_SIZE),
				JRProperties.getBooleanProperty(JRStyledText.PROPERTY_AWT_IGNORE_MISSING_FONT)
				);
	}
	
	
	/**
	 * 
	 */
	public TextRenderer(
		boolean isMinimizePrinterJobSize,
		boolean ignoreMissingFont
		)
	{
		this.isMinimizePrinterJobSize = isMinimizePrinterJobSize;
		this.ignoreMissingFont = ignoreMissingFont;
	}
	
	
	/**
	 * 
	 */
	public void render(
		Graphics2D initGrx,
		int initX,
		int initY,
		int initWidth,
		int initHeight,
		int initTopPadding,
		int initLeftPadding,
		int initBottomPadding,
		int initRightPadding,
		float initTextHeight,
		HorizontalAlignEnum initHorizontalAlignment,
		VerticalAlignEnum initVerticalAlignment,
		int initTabStop,
		float initLineSpacingFactor,
		float initLeadingOffset,
		int initFontSize,
		boolean isStyledText,
		JRStyledText styledText,
		String allText
		)
	{
		/*   */
		initialize(
			initGrx, 
			initX, 
			initY, 
			initWidth, 
			initHeight, 
			initTopPadding,
			initLeftPadding,
			initBottomPadding,
			initRightPadding,
			initTextHeight, 
			initHorizontalAlignment, 
			initVerticalAlignment, 
			initTabStop,
			initLineSpacingFactor,
			initLeadingOffset,
			initFontSize,
			isStyledText
			);
		
		AttributedCharacterIterator allParagraphs = 
			styledText.getAwtAttributedString(ignoreMissingFont).getIterator();

		int tokenPosition = 0;
		int lastParagraphStart = 0;
		String lastParagraphText = null;

		StringTokenizer tkzer = new StringTokenizer(allText, "\n", true);

		// text is split into paragraphs, using the newline character as delimiter
		while(tkzer.hasMoreTokens() && !isMaxHeightReached) 
		{
			String token = tkzer.nextToken();

			if ("\n".equals(token))
			{
				renderParagraph(allParagraphs, lastParagraphStart, lastParagraphText);

				lastParagraphStart = tokenPosition;
				lastParagraphText = null;
			}
			else
			{
				lastParagraphStart = tokenPosition;
				lastParagraphText = token;
			}

			tokenPosition += token.length();
		}

		if (!isMaxHeightReached && lastParagraphStart < allText.length())
		{
			renderParagraph(allParagraphs, lastParagraphStart, lastParagraphText);
		}
	}


	/**
	 * 
	 */
	private void initialize(
		Graphics2D initGrx,
		int initX,
		int initY,
		int initWidth,
		int initHeight,
		int initTopPadding,
		int initLeftPadding,
		int initBottomPadding,
		int initRightPadding,
		float initTextHeight,
		HorizontalAlignEnum initHorizontalAlignment,
		VerticalAlignEnum initVerticalAlignment,
		int initTabStop,
		float initLineSpacingFactor,
		float initLeadingOffset,
		int initFontSize,
		boolean isStyledText
		)
	{
		this.grx = initGrx;
		
		this.horizontalAlignment = initHorizontalAlignment;

		verticalOffset = 0f;
		switch (initVerticalAlignment)
		{
			case TOP :
			{
				verticalOffset = 0f;
				break;
			}
			case MIDDLE :
			{
				verticalOffset = (initHeight - initTopPadding - initBottomPadding - initTextHeight) / 2f;
				break;
			}
			case BOTTOM :
			{
				verticalOffset = initHeight - initTopPadding - initBottomPadding - initTextHeight;
				break;
			}
			default :
			{
				verticalOffset = 0f;
			}
		}

		this.tabStop = initTabStop;
		this.lineSpacingFactor = initLineSpacingFactor;
		this.leadingOffset = initLeadingOffset;

		this.x = initX;
		this.y = initY;
		this.topPadding = initTopPadding;
		this.leftPadding = initLeftPadding;
		formatWidth = initWidth - initLeftPadding - initRightPadding;
		formatWidth = formatWidth < 0 ? 0 : formatWidth;
		this.textHeight = initTextHeight;

		drawPosY = 0;
		drawPosX = 0;
	
		isMaxHeightReached = false;
		
		this.fontSize = initFontSize;
		maxFontSizeFinder = MaxFontSizeFinder.getInstance(isStyledText);
	}
	
	/**
	 * 
	 */
	private void renderParagraph(
		AttributedCharacterIterator allParagraphs,
		int lastParagraphStart,
		String lastParagraphText
		)
	{
		AttributedCharacterIterator paragraph = null;
		
		if (lastParagraphText == null)
		{
			paragraph = 
				new AttributedString(
					" ",
					new AttributedString(
						allParagraphs, 
						lastParagraphStart, 
						lastParagraphStart + 1
						).getIterator().getAttributes()
					).getIterator();
		}
		else
		{
			paragraph = 
				new AttributedString(
					allParagraphs, 
					lastParagraphStart, 
					lastParagraphStart + lastParagraphText.length()
					).getIterator();
		}

		List<Integer> tabIndexes = JRStringUtil.getTabIndexes(lastParagraphText);
		
		int currentTab = 0;
		
		int nextTabStop = 0;
		boolean requireNextWord = false;
	
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, LINE_BREAK_FONT_RENDER_CONTEXT);//grx.getFontRenderContext()

		// the paragraph is rendered one line at a time
		while (lineMeasurer.getPosition() < paragraph.getEndIndex() && !isMaxHeightReached)
		{
			boolean lineComplete = false;

			int maxFontSize = 0;
			
			// each line is split into segments, using the tab character as delimiter
			List<TabSegment> segments = new ArrayList<TabSegment>(1);

			TabSegment oldSegment = null;
			TabSegment crtSegment = null;

			// splitting the current line into tab segments
			while (!lineComplete)
			{
				// the current segment limit is either the next tab character or the paragraph end 
				int tabIndexOrEndIndex = (tabIndexes == null || currentTab >= tabIndexes.size() ? paragraph.getEndIndex() : tabIndexes.get(currentTab) + 1);
				
				int startIndex = lineMeasurer.getPosition();

				int rightX = 0;
				float availableWidth = 0;

				if (segments.size() == 0)
				{
					rightX = 0;
					//nextTabStop = nextTabStop;
				}
				else
				{
					rightX = oldSegment.rightX;
					nextTabStop = (rightX / tabStop + 1) * tabStop;
				}

				availableWidth = formatWidth - getAvailableWidth(horizontalAlignment, rightX, nextTabStop);
				
				// creating a text layout object for each tab segment 
				TextLayout layout = 
					lineMeasurer.nextLayout(
						availableWidth,
						tabIndexOrEndIndex,
						requireNextWord
						);
				
				if (layout != null)
				{
		 			AttributedString tmpText = 
						new AttributedString(
							paragraph, 
							startIndex, 
							startIndex + layout.getCharacterCount()
							);
		 			
					if (isMinimizePrinterJobSize)
					{
						//eugene fix - start
						layout = new TextLayout(tmpText.getIterator(), grx.getFontRenderContext());
						//eugene fix - end
					}
		
					maxFontSize = 
						Math.max(
							maxFontSize, 
							maxFontSizeFinder.findMaxFontSize(
								tmpText.getIterator(),
								fontSize
								)
							);

					//creating the current segment
					crtSegment = new TabSegment();
					crtSegment.layout = layout;

					int leftX = getLeftX(nextTabStop, layout.getAdvance(), horizontalAlignment);
					if (rightX > leftX)
					{
						crtSegment.leftX = rightX;
						crtSegment.rightX = (int)(rightX + layout.getAdvance());//FIXMETAB some rounding issues here
					}
					else
					{
						crtSegment.leftX = leftX;
						crtSegment.rightX = getRightX(nextTabStop, layout, horizontalAlignment);
					}

					segments.add(crtSegment);
				}
				
				requireNextWord = true;

				if (lineMeasurer.getPosition() == tabIndexOrEndIndex)
				{
					// the segment limit was a tab; going to the next tab
					currentTab++;
				}

				if (lineMeasurer.getPosition() == paragraph.getEndIndex())
				{
					// the segment limit was the paragraph end; line completed and next line should start at normal zero x offset
					lineComplete = true;
					nextTabStop = 0;
				}
				else
				{
					// there is paragraph text remaining 
					if (lineMeasurer.getPosition() == tabIndexOrEndIndex)
					{
						// the segment limit was a tab
						if (crtSegment.rightX >= tabStop * (int)(formatWidth / tabStop))
						{
							// current segment stretches out beyond the last tab stop; line complete
							lineComplete = true;
							// next line should should start at first tab stop indent
							nextTabStop = tabStop;
						}
						else
						{
							//nothing; this leaves lineComplete=false
						}
					}
					else
					{
						// the segment did not fit entirely
						lineComplete = true;
						if (layout == null)
						{
							// nothing fitted; next line should start at first tab stop indent
							if (nextTabStop == tabStop)//FIXMETAB check based on segments.size()
							{
								// at second attempt we give up to avoid infinite loop
								nextTabStop = 0;
								requireNextWord = false;
								
								//provide dummy maxFontSize because it is used for the line height of this empty line when attempting drawing below
					 			AttributedString tmpText = 
									new AttributedString(
										paragraph, 
										startIndex, 
										startIndex + 1
										);
					 			
								maxFontSize = 
									maxFontSizeFinder.findMaxFontSize(
										tmpText.getIterator(),
										fontSize
										);
							}
							else
							{
								nextTabStop = tabStop;
							}
						}
						else
						{
							// something fitted
							nextTabStop = 0;
							requireNextWord = false;
						}
					}
				}

				oldSegment = crtSegment;
			}

			float lineHeight = lineSpacingFactor * maxFontSize; 

			if (drawPosY + lineHeight <= textHeight)
			{
				drawPosY += lineHeight;
				
				// now iterate through segments and draw their layouts
				for (TabSegment segment : segments)
				{
					TextLayout layout = segment.layout;
//					switch (horizontalAlignment)
//					{
//						case JUSTIFIED :
//						{
//							if (layout.isLeftToRight())
//							{
//								drawPosX = segment.tabPos;
//							}
//							else
//							{
//								drawPosX = segment.tabPos + formatWidth - layout.getAdvance();
//							}
//							if (lineMeasurer.getPosition() < paragraph.getEndIndex())
//							{
//								layout = layout.getJustifiedLayout(formatWidth);
//							}
//
//							break;
//						}
//						case RIGHT ://FIXMETAB RTL writings
//						{
////							//drawPosX = formatWidth - layout.getAdvance();
////							drawPosX = Math.min(formatWidth, ((int)(segment.tabPos + layout.getAdvance()) / tabStop + 1) * tabStop) - layout.getAdvance();
//							drawPosX = formatWidth + segment.getLeftX(horizontalAlignment);
//							break;
//						}
//						case CENTER :
//						{
//							drawPosX = segment.tabPos + (formatWidth - layout.getAdvance()) / 2;
//							break;
//						}
//						case LEFT :
//						default :
//						{
//							drawPosX = segment.getLeftX(horizontalAlignment);
////							drawPosX = segment.tabPos;
//						}
//					}

					drawPosX = segment.leftX;
					
					draw(layout);
				}
			}
			else
			{
				isMaxHeightReached = true;
			}
		}
	}
	
	/**
	 * 
	 */
	public void draw(TextLayout layout)
	{
		layout.draw(
			grx,
			drawPosX + x + leftPadding,
			drawPosY + y + topPadding + verticalOffset + leadingOffset
			);
	}
	
	public static int getRightX(int tabPos, TextLayout layout, HorizontalAlignEnum horizontalAlignment)
	{
		int rightX = 0;
		switch (horizontalAlignment)
		{
			case JUSTIFIED :
//			{
//				if (layout.isLeftToRight())
//				{
//					drawPosX = segment.tabPos;
//				}
//				else
//				{
//					drawPosX = segment.tabPos + formatWidth - layout.getAdvance();
//				}
//				if (lineMeasurer.getPosition() < paragraph.getEndIndex())
//				{
//					layout = layout.getJustifiedLayout(formatWidth);
//				}
//
//				break;
//			}
			case RIGHT ://FIXMETAB RTL writings
			{
				rightX = tabPos;
				break;
			}
			case CENTER :
//			{
//				drawPosX = segment.tabPos + (formatWidth - layout.getAdvance()) / 2;
//				break;
//			}
			case LEFT :
			default :
			{
				rightX = (int)(tabPos + layout.getAdvance());
			}
		}
		return rightX;
	}
	
	public static int getLeftX(int tabPos, float advance, HorizontalAlignEnum horizontalAlignment)
	{
		int leftX = 0;
		switch (horizontalAlignment)
		{
			case JUSTIFIED :
//			{
//				if (layout.isLeftToRight())
//				{
//					drawPosX = segment.tabPos;
//				}
//				else
//				{
//					drawPosX = segment.tabPos + formatWidth - layout.getAdvance();
//				}
//				if (lineMeasurer.getPosition() < paragraph.getEndIndex())
//				{
//					layout = layout.getJustifiedLayout(formatWidth);
//				}
//
//				break;
//			}
			case RIGHT ://FIXMETAB RTL writings
			{
				leftX = (int)(tabPos - advance);
				break;
			}
			case CENTER :
//			{
//				drawPosX = segment.tabPos + (formatWidth - layout.getAdvance()) / 2;
//				break;
//			}
			case LEFT :
			default :
			{
				leftX = tabPos;
			}
		}
		return leftX;
	}

	public static int getAvailableWidth(HorizontalAlignEnum horizontalAlignment, int rightX, int nextTabStop)//FIXMETAB move these
	{
		int availableWidth = 0;
		switch (horizontalAlignment)
		{
			case JUSTIFIED :
//			{
//				if (layout.isLeftToRight())
//				{
//					drawPosX = segment.tabPos;
//				}
//				else
//				{
//					drawPosX = segment.tabPos + formatWidth - layout.getAdvance();
//				}
//				if (lineMeasurer.getPosition() < paragraph.getEndIndex())
//				{
//					layout = layout.getJustifiedLayout(formatWidth);
//				}
//
//				break;
//			}
			case RIGHT ://FIXMETAB RTL writings
			{
				availableWidth = rightX;
				break;
			}
			case CENTER :
//			{
//				drawPosX = segment.tabPos + (formatWidth - layout.getAdvance()) / 2;
//				break;
//			}
			case LEFT :
			default :
			{
				availableWidth = nextTabStop;
			}
		}
		return availableWidth;
	}
}

class TabSegment
{
	public TextLayout layout;
	public int leftX;
	public int rightX;
}