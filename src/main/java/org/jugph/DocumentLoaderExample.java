package org.jugph;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;

public class DocumentLoaderExample {
    public static void main(String[] args) {
        Document document = UrlDocumentLoader.load("https://github.com/JUGPH/.github/tree/main/profile",
                        new TextDocumentParser());

        HtmlTextExtractor textExtractor = new HtmlTextExtractor();
        Document transformedDocument = textExtractor.transform(document);

        System.out.println(document.text()+"\n===========Transformed Document===================\n"+transformedDocument.text());
    }
}
