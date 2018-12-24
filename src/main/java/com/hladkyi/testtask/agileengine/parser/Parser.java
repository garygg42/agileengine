package com.hladkyi.testtask.agileengine.parser;

import com.hladkyi.testtask.agileengine.parser.model.Button;
import com.hladkyi.testtask.agileengine.parser.tools.JsoupFindByIdSnippet;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.RGBColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

import static com.hladkyi.testtask.agileengine.parser.tools.CssSnippets.getButtonColor;
import static com.hladkyi.testtask.agileengine.parser.tools.CssSnippets.getStyles;
import static com.hladkyi.testtask.agileengine.parser.tools.JsoupCssSelectSnippet.findElementsByQuery;
import static com.hladkyi.testtask.agileengine.parser.tools.JsoupFindByIdSnippet.*;

public class Parser {

    public static void main(String[] args) throws IOException {
        String originalPagePathArg = args[0];
        String modifiedPagePathArg = args[1];

        System.out.println(findModifiedButtonPath(originalPagePathArg, modifiedPagePathArg));
    }

    public static String findModifiedButtonPath(String originalPagePathArg, String modifiedPagePathArg) {
        Path originalPagePath = Paths.get(originalPagePathArg);
        File originalPageFile = originalPagePath.toFile();
        Path modifiedPagePath = Paths.get(modifiedPagePathArg);
        File modifiedPageFile = modifiedPagePath.toFile();

        Map<String, CSSStyleDeclaration> styles = getStyles(originalPagePath);

        Button originalMakeOkButton = getOriginalButton(originalPageFile, styles);

        Elements buttonsFromModifiedPage = findElementsByQuery(modifiedPageFile, "a[class*=\"btn\"]")
                .orElseThrow(() -> new IllegalStateException("Could not find any button in modified page"));

        Button foundMakeOkButtonFromModifiedPage = buttonsFromModifiedPage.stream()
                .map(element -> {
                    String buttonText = element.text();
                    RGBColor buttonColor = getButtonColor(styles, element);
                    return new Button(buttonColor, buttonText, element.parents());
                })
                .filter(candidate -> Objects.equals(candidate.getColor().toString(), originalMakeOkButton.getColor().toString())) // Should be the same colour as original button
                .map(element -> {
                    String originalSelector = originalMakeOkButton.getAncestors().get(0).cssSelector();
                    String modifiedSelector = element.getAncestors().get(0).cssSelector();
                    SimilarityStrategy strategy = new JaroWinklerStrategy();
                    StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
                    double score = service.score(originalSelector, modifiedSelector);
                    return new AbstractMap.SimpleEntry<>(score, element);
                }) // Calculate how far away new button from original
                .sorted((o1, o2) -> Double.compare(o2.getKey(), o1.getKey())) // Get the nearest one
                .map(AbstractMap.SimpleEntry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find a button that would be similar to Make Everything OK"));

        return foundMakeOkButtonFromModifiedPage.getAncestors().get(0).cssSelector();
    }

    private static Button getOriginalButton(File originalPageFile, Map<String, CSSStyleDeclaration> styles) {
        Element originalButtonElement = findElementById(originalPageFile, "make-everything-ok-button")
                .orElseThrow(() -> new IllegalStateException(""));

        String originalButtonText = originalButtonElement.text();
        RGBColor buttonColor = getButtonColor(styles, originalButtonElement);

        return new Button(buttonColor, originalButtonText, originalButtonElement.parents());
    }
}
