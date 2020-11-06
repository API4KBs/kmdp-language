package edu.mayo.kmdp.language.translators.surrogate.v2;

import java.util.Collection;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.surrogate.KnowledgeAsset;

public class SurrogateV2ToHTML {

  public Document transform(KnowledgeAsset knowledgeAsset) {
    String html = MakeHTML.makeHTML(knowledgeAsset);
    return Jsoup.parse(html);
  }

  public static class MakeHTML {

    protected MakeHTML() {
      // nothing to do
    }

    private static final class HTMLStyle extends ToStringStyle {

      public HTMLStyle() {

        setContentStart("<table>" + System.lineSeparator() +
            "<tbody><tr><td>");

        setFieldSeparator("</td></tr>" + System.lineSeparator() + "<tr><td>");
        setFieldNameValueSeparator("</td><td>");

        setContentEnd("</td></tr>" + System.lineSeparator() + "</tbody></table>");

        setArrayContentDetail(true);
        setUseShortClassName(true);
        setUseClassName(false);
        setUseIdentityHashCode(false);
      }

      @Override
      public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
        if (value == null) {
          return;
        }
        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
          return;
        }
        super.append(buffer, fieldName, value, fullDetail);
      }

      @Override
      public void appendDetail(StringBuffer buffer, String fieldName, Object value) {

        if (value instanceof Term) {
          Term t = (Term) value;
          buffer.append(t.getLabel());
          return;
        }
        if (value instanceof SemanticIdentifier) {
          SemanticIdentifier id = (SemanticIdentifier) value;
          buffer.append(id.getVersionId() != null ? id.getVersionId() : id.getResourceId());
          return;
        }

        if (value.getClass().getName().startsWith("java.lang")) {
          super.appendDetail(buffer, fieldName, value);
          return;
        }

        buffer.append(ReflectionToStringBuilder.toString(value, this));
      }

      @Override
      protected void appendDetail(StringBuffer buffer, String fieldName, Collection<?> coll) {
        coll.forEach(item -> {
          if (item instanceof Term) {
            Term t = (Term) item;
            buffer.append(t.getLabel());
            return;
          }

          buffer.append(ReflectionToStringBuilder.toString(item, this));
        });
      }
    }

    public static String makeHTML(Object object) {
      return ReflectionToStringBuilder.toString(object, new HTMLStyle());
    }
  }
}
