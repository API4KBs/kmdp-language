package edu.mayo.kmdp.language;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.of;

import edu.mayo.kmdp.language.detectors.html.HTMLDetector;
import edu.mayo.kmdp.language.detectors.surrogate.v2.Surrogate2Detector;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class DetectorTest {

  @Test
  void testHtmlDetector() {
    String html = "<html>\n"
        + "<body>\n"
        + "\n"
        + "<h1>Test Heading</h1>\n"
        + "<p>Some Paragraph.</p>\n"
        + "\n"
        + "</body>\n"
        + "</html>";

    HTMLDetector htmlDetector = new HTMLDetector();
    Answer<SyntacticRepresentation> rep = htmlDetector.applyDetect(of(html), null)
        .map(KnowledgeCarrier::getRepresentation);
    assertTrue(rep.isSuccess());
  }

  @Test
  void testHtmlDetectorWithXML() {
    String html = "<foo></foo>";

    HTMLDetector htmlDetector = new HTMLDetector();
    Answer<SyntacticRepresentation> rep = htmlDetector.applyDetect(of(html), null)
        .map(KnowledgeCarrier::getRepresentation);
    assertTrue(rep.isFailure());
  }

  @Test
  void testHtmlDetectorWithText() {
    String html = "blah blah blah";

    HTMLDetector htmlDetector = new HTMLDetector();
    Answer<SyntacticRepresentation> rep = htmlDetector.applyDetect(of(html), null)
        .map(KnowledgeCarrier::getRepresentation);
    assertTrue(rep.isFailure());
  }

  @Test
  void testSurr2Detector() {
    String xml = "<ns5:knowledgeAsset "
        + "xmlns:id=\"http://www.omg.org/spec/API4KP/1.0/id\"\n"
        + "  xmlns:api=\"http://www.omg.org/spec/API4KP/1.0/services\"\n"
        + "  xmlns:ann=\"http://kmdp.mayo.edu/metadata/annotations\"\n"
        + "  xmlns:ns5=\"http://kmdp.mayo.edu/metadata/v2/surrogate\"\n>"
        + "</ns5:knowledgeAsset>";

    Surrogate2Detector surrDetector = new Surrogate2Detector();
    Answer<SyntacticRepresentation> rep = surrDetector.applyDetect(of(xml), null)
        .map(KnowledgeCarrier::getRepresentation);
    assertTrue(rep.isSuccess());
  }

}
