/*
 * Copyright (c) 2026 Abhijay (abj). All rights reserved.
 *
 * This source code is proprietary and confidential. Unauthorized copying,
 * modification, distribution, or use of this file, via any medium, is
 * strictly prohibited without prior written permission of the author.
 */
// model/Formatting.java — Holds formatting properties (bold, italic, font, size)
import java.util.HashSet;
import java.util.Set;

public class Formatting {
    public enum Style { BOLD, ITALIC, UNDERLINE } // enum = fixed set of text styles; compile-time safe

    private Set<Style> styles;                // private = styles managed through add/remove methods
    private int fontSize;                     // private = font size controlled via setter

    public Formatting() {
        this.styles = new HashSet<>();
        this.fontSize = 12;
    }

    public Formatting(Formatting other) {
        this.styles = new HashSet<>(other.styles);
        this.fontSize = other.fontSize;
    }

    public void addStyle(Style style) { styles.add(style); }
    public void removeStyle(Style style) { styles.remove(style); }
    public boolean hasStyle(Style style) { return styles.contains(style); }
    public Set<Style> getStyles() { return new HashSet<>(styles); }
    public int getFontSize() { return fontSize; }
    public void setFontSize(int size) { this.fontSize = size; }

    @Override
    public String toString() {
        if (styles.isEmpty() && fontSize == 12) return "";
        StringBuilder sb = new StringBuilder("(");
        for (Style s : styles) { sb.append(s).append(", "); }
        if (fontSize != 12) sb.append("size=").append(fontSize).append(", ");
        if (sb.length() > 1) sb.setLength(sb.length() - 2);
        sb.append(")");
        return sb.toString();
    }
}
