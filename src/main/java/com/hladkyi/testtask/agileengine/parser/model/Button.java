package com.hladkyi.testtask.agileengine.parser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.select.Elements;
import org.w3c.dom.css.RGBColor;

//@Data
//@AllArgsConstructor
public class Button {

    RGBColor color;
    String text;
    Elements ancestors;


    public Button(RGBColor color, String text, Elements ancestors) {
        this.color = color;
        this.text = text;
        this.ancestors = ancestors;
    }

    public RGBColor getColor() {
        return color;
    }

    public void setColor(RGBColor color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Elements getAncestors() {
        return ancestors;
    }

    public void setAncestors(Elements ancestors) {
        this.ancestors = ancestors;
    }

    @Override
    public String toString() {
        return "Button{" +
                "color=" + color +
                ", text='" + text + '\'' +
                '}';
    }
}
