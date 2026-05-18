/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/TextSegment.java — A contiguous run of text with uniform formatting
public class TextSegment {
    private String text;                      // private = content changed only through setText()
    private Formatting formatting;            // private = formatting paired with this text run

    public TextSegment(String text) {
        this.text = text;
        this.formatting = new Formatting();
    }

    public TextSegment(String text, Formatting formatting) {
        this.text = text;
        this.formatting = new Formatting(formatting);
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Formatting getFormatting() { return formatting; }
    public int length() { return text.length(); }

    public TextSegment copy() { return new TextSegment(text, formatting); }

    @Override
    public String toString() {
        String fmt = formatting.toString();
        return "[" + text + "]" + (fmt.isEmpty() ? "" : " " + fmt);
    }
}
