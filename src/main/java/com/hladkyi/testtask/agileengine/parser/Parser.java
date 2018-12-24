package com.hladkyi.testtask.agileengine.parser;

import com.hladkyi.testtask.agileengine.parser.tools.JsoupFindByIdSnippet;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Parser {

    public static void main(String[] args) throws IOException {
        String originalPagePathArg = args[0];
        String modifiedPagePath = args[1];
        Path originalPagePath = Paths.get(originalPagePathArg);
        File originalPageFile = originalPagePath.toFile();

        Element originalButton = JsoupFindByIdSnippet
                .findElementById(originalPageFile, "make-everything-ok-button")
                .orElseThrow(() -> new IllegalStateException(""));

        Attributes attributes = originalButton.attributes();
        Set<String> classNames = originalButton.classNames();
        String text = originalButton.text();

        Document doc = Jsoup.parse(
                originalPageFile,
                "utf8",
                originalPageFile.getAbsolutePath());

        Elements links = doc.head().getElementsByTag("link");
        String styles = StreamSupport.stream(links.spliterator(), false)
                .map(link -> link.attr("href"))
                .map(cssPath -> {
                    try {
                        return new String(Files.readAllBytes(originalPagePath.getParent().resolve(cssPath)));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }).collect(Collectors.joining());


        InputSource entireSource = new InputSource(new StringReader(styles));
        CSSOMParser entireParser = new CSSOMParser(new SACParserCSS3());
        CSSStyleSheet sheet = entireParser.parseStyleSheet(entireSource, null, null);
        CSSRuleList ruleList = sheet.getCssRules();

        for (int i = 0; i < ruleList.getLength(); i++)
        {
            CSSRule rule = ruleList.item(i);
            if (rule instanceof CSSStyleRule)
            {
                CSSStyleRule styleRule=(CSSStyleRule)rule;
                System.out.println("selector:" + i + ": " + styleRule.getSelectorText());
                CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                for (int j = 0; j < styleDeclaration.getLength(); j++)
                {
                    String property = styleDeclaration.item(j);
                    System.out.println("property: " + property);
                    System.out.println("value: " + styleDeclaration.getPropertyCSSValue(property).getCssText());
                    System.out.println("priority: " + styleDeclaration.getPropertyPriority(property));
                }
            }
        }
    }
}
