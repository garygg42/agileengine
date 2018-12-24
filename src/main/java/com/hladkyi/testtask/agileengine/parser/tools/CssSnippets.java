package com.hladkyi.testtask.agileengine.parser.tools;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CssSnippets {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupCssSelectSnippet.class);

    private static String CHARSET_NAME = "utf8";

    public static RGBColor getButtonColor(Map<String, CSSStyleDeclaration> styles, Element buttonElement) {
        Set<String> buttonClassNames = buttonElement.classNames();
        return buttonClassNames.stream()
                .map(className -> ((CSSPrimitiveValue) styles.get("." + className)
                        .getPropertyCSSValue("background-color")))
                .filter(Objects::nonNull)
                .map(CSSPrimitiveValue::getRGBColorValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new)).getLast();
    }

    public static Map<String, CSSStyleDeclaration> getStyles(Path originalPagePath) {
        File originalPageFile = originalPagePath.toFile();
        Document doc = null;
        try {
            doc = Jsoup.parse(originalPageFile, CHARSET_NAME, originalPageFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error parsing [{}] file", originalPageFile.toPath(), e);
        }

        Elements links = doc.head().getElementsByTag("link");
        String allCss = links.stream()
                .map(link -> link.attr("href"))
                .map(cssPathArg -> {
                    Path cssPath = originalPagePath.getParent().resolve(cssPathArg);
                    try {
                        return new String(Files.readAllBytes(cssPath));
                    } catch (IOException e) {
                        LOGGER.error("Error reading [{}] file", cssPath, e);
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.joining());


        InputSource source = new InputSource(new StringReader(allCss));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        CSSStyleSheet sheet = null;
        try {
            sheet = parser.parseStyleSheet(source, null, null);
        } catch (IOException e) {
            LOGGER.error("Error parsing [{}] file", originalPageFile.toPath(), e);
        }
        CSSRuleList ruleList = sheet.getCssRules();

        Map<String, CSSStyleDeclaration> styles = new HashMap<>();

        for (int i = 0; i < ruleList.getLength(); i++) {
            CSSRule rule = ruleList.item(i);
            if (rule instanceof CSSStyleRule) {
                CSSStyleRule styleRule = (CSSStyleRule) rule;
                CSSStyleDeclaration styleDeclaration = styleRule.getStyle();
                styles.put(styleRule.getSelectorText(), styleDeclaration);
            }
        }
        return styles;
    }
}
