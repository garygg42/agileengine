package com.hladkyi.testtask.agileengine.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    @Test
    public void testSample1() {
        String actual = Parser.findModifiedButtonPath("src\\test\\resources\\sample-0-origin.html", "src\\test\\resources\\sample-1-evil-gemini.html");
        assertEquals("#page-wrapper > div.row:nth-child(3) > div.col-lg-8 > div.panel.panel-default > div.panel-body", actual);
    }

    @Test
    public void testSample2() {
        String actual = Parser.findModifiedButtonPath("src\\test\\resources\\sample-0-origin.html", "src\\test\\resources\\sample-2-container-and-clone.html");
        assertEquals("#page-wrapper > div.row:nth-child(3) > div.col-lg-8 > div.panel.panel-default > div.panel-body > div.some-container", actual);
    }

    @Test
    public void testSample3() {
        String actual = Parser.findModifiedButtonPath("src\\test\\resources\\sample-0-origin.html", "src\\test\\resources\\sample-3-the-escape.html");
        assertEquals("#page-wrapper > div.row:nth-child(3) > div.col-lg-8 > div.panel.panel-default > div.panel-footer", actual);
    }

    @Test
    public void testSample4() {
        String actual = Parser.findModifiedButtonPath("src\\test\\resources\\sample-0-origin.html", "src\\test\\resources\\sample-4-the-mash.html");
        assertEquals("#page-wrapper > div.row:nth-child(3) > div.col-lg-8 > div.panel.panel-default > div.panel-footer", actual);
    }
}