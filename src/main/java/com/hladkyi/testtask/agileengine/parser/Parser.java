package com.hladkyi.testtask.agileengine.parser;

import com.hladkyi.testtask.agileengine.parser.model.Button;
import com.hladkyi.testtask.agileengine.parser.tools.JsoupCssSelectSnippet;
import com.hladkyi.testtask.agileengine.parser.tools.JsoupFindByIdSnippet;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.jsoup.Jsoup;
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
import java.util.*;
import java.util.stream.Collectors;

public class Parser {

    public static void main(String[] args) throws IOException {
        String originalPagePathArg = args[0];
        String modifiedPagePathArg = args[1];
        Path originalPagePath = Paths.get(originalPagePathArg);
        File originalPageFile = originalPagePath.toFile();
        Path modifiedPagePath = Paths.get(modifiedPagePathArg);
        File modifiedPageFile = modifiedPagePath.toFile();

        Map<String, CSSStyleDeclaration> styles = getStyles(originalPagePath);

        Button originalButton = getOriginalButton(originalPageFile, styles);

        Elements elementsFromModified = JsoupCssSelectSnippet
                .findElementsByQuery(modifiedPageFile, "a[class*=\"btn\"]")
                .orElseThrow(IllegalStateException::new);

        Button foundButton = elementsFromModified.stream()
                .map(element -> {
                    String buttonText = element.text();
                    RGBColor buttonColor = getButtonColor(styles, element);
                    return new Button(buttonColor, buttonText, element.parents());
                })
                .filter(candidate -> Objects.equals(candidate.getColor(), originalButton.getColor()))
                .map(e -> {
                    String originalSelector = originalButton.getAncestors().get(0).cssSelector();
                    String modifiedSelector = e.getAncestors().get(0).cssSelector();
                    SimilarityStrategy strategy = new JaroWinklerStrategy();
                    StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
                    double score = service.score(originalSelector, modifiedSelector);
                    return new AbstractMap.SimpleEntry<>(score, e);
                })
                .sorted((o1, o2) -> Double.compare(o2.getKey(), o1.getKey()))
                .map(AbstractMap.SimpleEntry::getValue)
                .findFirst().orElseThrow(IllegalStateException::new);

        System.out.println(foundButton.getAncestors().get(0).cssSelector());
    }

    private static Button getOriginalButton(File originalPageFile, Map<String, CSSStyleDeclaration> styles) {
        Element originalButtonElement = JsoupFindByIdSnippet
                .findElementById(originalPageFile, "make-everything-ok-button")
                .orElseThrow(() -> new IllegalStateException(""));

        String originalButtonText = originalButtonElement.text();
        RGBColor buttonColor = getButtonColor(styles, originalButtonElement);

        return new Button(buttonColor, originalButtonText, originalButtonElement.parents());
    }

    private static RGBColor getButtonColor(Map<String, CSSStyleDeclaration> styles, Element buttonElement) {
        Set<String> buttonClassNames = buttonElement.classNames();
        return buttonClassNames.stream()
                .map(className -> ((CSSPrimitiveValue) styles.get("." + className)
                        .getPropertyCSSValue("background-color")))
                .filter(Objects::nonNull)
                .map(CSSPrimitiveValue::getRGBColorValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new)).getLast();
    }

    private static Map<String, CSSStyleDeclaration> getStyles(Path originalPagePath) throws IOException {
        File originalPageFile = originalPagePath.toFile();
        Document doc = Jsoup.parse(originalPageFile, "utf8", originalPageFile.getAbsolutePath());

        Elements links = doc.head().getElementsByTag("link");
        String allCss = links.stream()
                .map(link -> link.attr("href"))
                .map(cssPath -> {
                    try {
                        return new String(Files.readAllBytes(originalPagePath.getParent().resolve(cssPath)));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.joining());


        InputSource entireSource = new InputSource(new StringReader(allCss));
        CSSOMParser entireParser = new CSSOMParser(new SACParserCSS3());
        CSSStyleSheet sheet = entireParser.parseStyleSheet(entireSource, null, null);
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
