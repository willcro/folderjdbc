package com.willcro.folderdb.files.readers;

import com.willcro.folderdb.config.FileConfiguration;
import com.willcro.folderdb.exception.ConfigurationException;
import com.willcro.folderdb.sql.Table;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses XML files
 *
 * Required configuration: - xPath
 */
@Slf4j
public class XmlReader extends BaseReader {

  @Override
  public String getId() {
    return "xml";
  }

  @Override
  public List<Table> readFile(File file, FileConfiguration config) throws ConfigurationException {
    var xPathExpr = config.getXpath();
    if (xPathExpr == null) {
      throw new ConfigurationException("Required field xPath not found");
    }

    try {
      FileInputStream fileIS = new FileInputStream(file);
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document xmlDocument = builder.parse(fileIS);
      XPath xPath = XPathFactory.newInstance().newXPath();
      var nodeList = (NodeList) xPath.compile(xPathExpr)
          .evaluate(xmlDocument, XPathConstants.NODESET);
      System.out.println(nodeList);

      var data = new ArrayList<Map<String, String>>();

      for (int i = 0; i < nodeList.getLength(); i++) {
        data.add(getDataFromNode(nodeList.item(i)));
      }

      return Collections.singletonList(convertData(data, file.getName()));

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private Map<String, String> getDataFromNode(Node node) {
    if (node.getNodeType() != Node.ELEMENT_NODE) {
      return Collections.emptyMap();
    }

    var ret = new HashMap<String, String>();
    var children = node.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {
      var child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        ret.put(child.getNodeName(), child.getTextContent());
      }
    }

    return ret;
  }

  private Table convertData(List<Map<String, String>> data, String name) {
    var columns = data.stream()
        .map(Map::keySet)
        .flatMap(Collection::stream)
        .distinct().sorted()
        .collect(Collectors.toList());

    var columnToIndex = new HashMap<String, Integer>();
    for (int i = 0; i < columns.size(); i++) {
      columnToIndex.put(columns.get(i), i);
    }

    var rows = data.stream().map(row -> convertRow(row, columnToIndex));

    return Table.builder()
        .name(name)
        .columns(columns)
        .rows(rows)
        .build();
  }

  private List<String> convertRow(Map<String, String> row, Map<String, Integer> columnToIndex) {
    var ret = IntStream.range(0, columnToIndex.size())
        .mapToObj(it -> (String) null).collect(Collectors.toCollection(ArrayList::new));
    row.forEach((key, value) -> ret.set(columnToIndex.get(key), value));
    return ret;
  }

}
