package com.mnzit.groov

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.XmlTemplateEngine
import org.apache.commons.io.IOUtils
import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.Fop
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import java.nio.file.Files
import java.nio.file.Paths

class NewTest {
    private static String FO_JSON_RESPONSE = "${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/input/foResponse.json"

    static void main(String[] args) {
        def binding = [:]
        def colors = ["Red", "Green", "Yellow"]
        def regex =  "<a[^>]+href=\\'(.*?)\\'[^>]*>(.*?)<\\/a>"

        binding.regexxx = regex
        binding.instruction = "Download and save this <a href='https://go.ml.com/lbd26url' target='_blank' data-bactmln='action-AC200002-step121link0'>budget and debt management worksheet</a> and <a href='https://go.ml.com/lbd26url2' target='_blank' data-bactmln='action-AC200002-step121link0'>budget and debt management worksheet2</a> to set up your budget and monitor your progress."

        try {
            OutputStream out = new java.io.FileOutputStream("demo.pdf");
            String foRespBodyXML = parseJson(FO_JSON_RESPONSE)

            String foFullXML = getFullFOXMLString(foRespBodyXML)
            foFullXML = prepareFOString(binding, foFullXML)
            println foFullXML;
            Source src = new SAXSource(new InputSource(new StringReader(foFullXML)))

            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler())

            // Start XSLT transformation and FOP processing
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            transformer.transform(src, res)
        } catch (Exception e) {
            println e.getMessage()
        }

    }

    private static String getFileContent(String textFilePath) {
        String data = ""
        data = new String(Files.readAllBytes(Paths.get(textFilePath)))
        return data
    }


    private static String getFullFOXMLString(String foBody) {
        DocumentBuilderFactory documentBuildFactory = DocumentBuilderFactory.newInstance()
        documentBuildFactory.setNamespaceAware(true)
        DocumentBuilder doccumentBuilder = documentBuildFactory.newDocumentBuilder()
        String docNS = "http://www.w3.org/1999/XSL/Format"
        Document rootDocument = doccumentBuilder.parse(new File("/Users/manjitshakya/playground/groovy-compile-example/src/main/resources/com/bofa/omni/tools/crc/data/demo.xsl"))
        InputStream inputStream = IOUtils.toInputStream(foBody)
        Document foBodyDocument = doccumentBuilder.parse(inputStream)
        Node importedBodyNode = rootDocument.importNode(foBodyDocument.getFirstChild(), true)

        rootDocument.getElementsByTagNameNS(docNS, "flow").item(0).appendChild(importedBodyNode)

        IOUtils.closeQuietly(inputStream)
        return convertToXMLString(rootDocument)
    }


    private static String parseJson(String path) {
        String json = getFileContent(path)
        com.google.gson.JsonParser parser = new com.google.gson.JsonParser()
        com.google.gson.JsonElement element = parser.parse(json)
        com.google.gson.JsonObject object = element.getAsJsonObject()
        com.google.gson.JsonObject payload = object.get("payload").getAsJsonObject()
        com.google.gson.JsonObject foContent = payload.get("foContent").getAsJsonObject()
        String xml = foContent.get("foXml").getAsString()
        return xml
    }

    private static String convertToXMLString(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        tf.setOutputProperty(OutputKeys.INDENT, "yes")
        Writer out = new StringWriter()
        tf.transform(new DOMSource(xml), new StreamResult(out))
        return out.toString()
    }

    private static String prepareFOString(Object binding, String data) {
        def engine = new XmlTemplateEngine()
        Template template = (Template) engine.createTemplate(data)
        Writable wrt = template.make((Map) binding)

        //ByteArrayOutputStream os = new ByteArrayOutputStream()
        //wrt.writeTo(new BufferedWriter(new OutputStreamWriter(os)))
        return wrt.toString()
    }
}
