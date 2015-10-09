/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eastearly.richedittextview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

public class EmailHtmlUtil {

	// Regex that matches characters that have special meaning in HTML. '<',
	// '>', '&' and
	// multiple continuous spaces.
	private static final Pattern PLAIN_TEXT_TO_ESCAPE = Pattern
			.compile("[<>&]| {2,}|\r?\n");

	/**
	 * Escape some special character as HTML escape sequence.
	 * 
	 * @param text
	 *            Text to be displayed using WebView.
	 * @return Text correctly escaped.
	 */
	public static String escapeCharacterToDisplay(String text) {
		Pattern pattern = PLAIN_TEXT_TO_ESCAPE;
		Matcher match = pattern.matcher(text);

		if (match.find()) {
			StringBuilder out = new StringBuilder();
			int end = 0;
			do {
				int start = match.start();
				out.append(text.substring(end, start));
				end = match.end();
				int c = text.codePointAt(start);
				if (c == ' ') {
					// Escape successive spaces into series of "&nbsp;".
					for (int i = 1, n = end - start; i < n; ++i) {
						out.append("&nbsp;");
					}
					out.append(' ');
				} else if (c == '\r' || c == '\n') {
					out.append("<br>");
				} else if (c == '<') {
					out.append("&lt;");
				} else if (c == '>') {
					out.append("&gt;");
				} else if (c == '&') {
					out.append("&amp;");
				}
			} while (match.find());
			out.append(text.substring(end));
			text = out.toString();
		}
		return text;
	}

	public static String toHtml(Spanned spans) {
		StringBuilder out = new StringBuilder();
		withinHtml(out, spans);
		return out.toString();
	}

	private static void withinHtml(StringBuilder out, Spanned text ) {
		int len = text.length();

		int next;
		for (int i = 0; i < text.length(); i = next) {
			next = text.nextSpanTransition(i, len, ParagraphStyle.class);
			ParagraphStyle[] style = text.getSpans(i, next,
					ParagraphStyle.class);
			String elements = " ";
			boolean needDiv = false;

			for (int j = 0; j < style.length; j++) {
				if (style[j] instanceof AlignmentSpan) {
					Layout.Alignment align = ((AlignmentSpan) style[j])
							.getAlignment();
					needDiv = true;
					if (align == Layout.Alignment.ALIGN_CENTER) {
						elements = "align=\"center\" " + elements;
					} else if (align == Layout.Alignment.ALIGN_OPPOSITE) {
						elements = "align=\"right\" " + elements;
					} else {
						elements = "align=\"left\" " + elements;
					}
				}
			}
			if (needDiv) {
				out.append("<div " + elements + ">");
			}

			withinDiv(out, text, i, next);

			if (needDiv) {
				out.append("</div>");
			}
		}
	}

	private static void withinDiv(StringBuilder out, Spanned text, int start,
			int end) {
		int next;
		for (int i = start; i < end; i = next) {
			next = text.nextSpanTransition(i, end, QuoteSpan.class);
			QuoteSpan[] quotes = text.getSpans(i, next, QuoteSpan.class);

			for (QuoteSpan quote : quotes) {
				out.append("<blockquote>");
			}

			withinBlockquote(out, text, i, next);

			for (QuoteSpan quote : quotes) {
				out.append("</blockquote>\n");
			}
		}
	}

	private static void withinBlockquote(StringBuilder out, Spanned text,
			int start, int end) {
		out.append(getOpenParaTagWithDirection(text, start, end));

		int next;
		for (int i = start; i < end; i = next) {
			next = TextUtils.indexOf(text, '\n', i, end);
			if (next < 0) {
				next = end;
			}

			int nl = 0;

			while (next < end && text.charAt(next) == '\n') {
				nl++;
				next++;
			}

			withinParagraph(out, text, i, next - nl, nl, next == end);
		}

		out.append("</p>\n");
	}

	private static String getOpenParaTagWithDirection(Spanned text, int start,
			int end) {

		return "<p dir=\"ltr\">";
	}

	private static void withinParagraph(StringBuilder out, Spanned text,
			int start, int end, int nl, boolean last) {
		int next;
		for (int i = start; i < end; i = next) {
			next = text.nextSpanTransition(i, end, CharacterStyle.class);
			CharacterStyle[] style = text.getSpans(i, next,
					CharacterStyle.class);

			for (int j = 0; j < style.length; j++) {
				if (style[j] instanceof StyleSpan) {
					int s = ((StyleSpan) style[j]).getStyle();

					if ((s & Typeface.BOLD) != 0) {
						out.append("<b>");
					}
					if ((s & Typeface.ITALIC) != 0) {
						out.append("<i>");
					}
				}
				if (style[j] instanceof TypefaceSpan) {
					String s = ((TypefaceSpan) style[j]).getFamily();

					if (s.equals("monospace")) {
						out.append("<tt>");
					}
				}
				if (style[j] instanceof SuperscriptSpan) {
					out.append("<sup>");
				}
				if (style[j] instanceof SubscriptSpan) {
					out.append("<sub>");
				}
				if (style[j] instanceof UnderlineSpan) {
					out.append("<u>");
				}
				if (style[j] instanceof StrikethroughSpan) {
					out.append("<strike>");
				}
				if (style[j] instanceof URLSpan) {
					out.append("<a href=\"");
					out.append(((URLSpan) style[j]).getURL());
					out.append("\">");
				}
				if (style[j] instanceof ImageSpan) {
					out.append("<img src=\"");
					out.append(((ImageSpan) style[j]).getSource());
					out.append("\">");

					// Don't output the dummy character underlying the image.
					i = next;
				}
				if (style[j] instanceof AbsoluteSizeSpan) {
                    int fontSize =  ((HtmlAbsoluteSizeSpan) style[j]).getHtmlFontSize();
                    if(fontSize>HtmlAbsoluteSizeSpan.STANDARD_FONT_SIZE)
                    {
                        out.append("<big>");
                    }
                    else if(fontSize < HtmlAbsoluteSizeSpan.STANDARD_FONT_SIZE)
                    {
                        out.append("<small>");
                    }
                    out.append("<font size =\"");
                    out.append(fontSize);
                    out.append("\">");
				}
				if (style[j] instanceof ForegroundColorSpan) {
					out.append("<font color =\"#");
					String color = Integer
							.toHexString(((ForegroundColorSpan) style[j])
									.getForegroundColor() + 0x01000000);
					while (color.length() < 6) {
						color = "0" + color;
					}
					out.append(color);
					out.append("\">");
				}	
				if (style[j] instanceof BackgroundColorSpan) {
					out.append("<font style =\"background-color:#");
					String color = Integer
							.toHexString(((BackgroundColorSpan) style[j])
									.getBackgroundColor() + 0x01000000);
					while (color.length() < 6) {
						color = "0" + color;
					}
					out.append(color);
					out.append("\">");
				}	
			}

			withinStyle(out, text, i, next);

			for (int j = style.length - 1; j >= 0; j--) {
				if (style[j] instanceof BackgroundColorSpan) {
					out.append("</font>");
				}
				if (style[j] instanceof ForegroundColorSpan) {
					out.append("</font>");
				}
				if (style[j] instanceof AbsoluteSizeSpan) {
					out.append("</font>");
                    int fontSize =  ((HtmlAbsoluteSizeSpan) style[j]).getHtmlFontSize();
                    if(fontSize>HtmlAbsoluteSizeSpan.STANDARD_FONT_SIZE)
                    {
                        out.append("</big>");
                    }
                    else if(fontSize < HtmlAbsoluteSizeSpan.STANDARD_FONT_SIZE)
                    {
                        out.append("</small>");
                    }
				}
				if (style[j] instanceof URLSpan) {
					out.append("</a>");
				}
				if (style[j] instanceof StrikethroughSpan) {
					out.append("</strike>");
				}
				if (style[j] instanceof UnderlineSpan) {
					out.append("</u>");
				}
				if (style[j] instanceof SubscriptSpan) {
					out.append("</sub>");
				}
				if (style[j] instanceof SuperscriptSpan) {
					out.append("</sup>");
				}
				if (style[j] instanceof TypefaceSpan) {
					String s = ((TypefaceSpan) style[j]).getFamily();

					if (s.equals("monospace")) {
						out.append("</tt>");
					}
				}
				if (style[j] instanceof StyleSpan) {
					int s = ((StyleSpan) style[j]).getStyle();

					if ((s & Typeface.BOLD) != 0) {
						out.append("</b>");
					}
					if ((s & Typeface.ITALIC) != 0) {
						out.append("</i>");
					}
				}
			}
		}

		String p = last ? "" : "</p>\n"
				+ getOpenParaTagWithDirection(text, start, end);

		if (nl == 1) {
			out.append("<br>\n");
		} else if (nl == 2) {
			out.append(p);
		} else {
			for (int i = 2; i < nl; i++) {
				out.append("<br>");
			}
			out.append(p);
		}
	}

	private static void withinStyle(StringBuilder out, CharSequence text,
			int start, int end) {
		for (int i = start; i < end; i++) {
			char c = text.charAt(i);

			if (c == '<') {
				out.append("&lt;");
			} else if (c == '>') {
				out.append("&gt;");
			} else if (c == '&') {
				out.append("&amp;");
			} else if (c > 0x7E || c < ' ') {
				out.append("&#" + ((int) c) + ";");
			} else if (c == ' ') {
				while (i + 1 < end && text.charAt(i + 1) == ' ') {
					out.append("&nbsp;");
					i++;
				}

				out.append(' ');
			} else {
				out.append(c);
			}
		}
	}
}
