package com.vladmihalcea.hpjp.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 * @author Vlad Mihalcea
 */
public class XmlUtils {

  public static Document readXmlDocument(String xmlValue) {
    return readXmlDocument(xmlValue.getBytes(StandardCharsets.UTF_8));
  }

  public static Document readXmlDocument(byte[] xmlBytes) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new ByteArrayInputStream(xmlBytes));
      doc.getDocumentElement().normalize();
      return doc;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
