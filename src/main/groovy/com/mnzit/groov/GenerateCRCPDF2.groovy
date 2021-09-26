package com.mnzit.groov

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.text.XmlTemplateEngine
import org.apache.commons.io.IOUtils
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

public class GenerateCRCPDF2 {
    private static String root_path = "${System.properties['user.dir']}/src/main/resources/com/bofa/omni/tools/crc"
    private static String FOP_XML = "${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/crc-fo.xml"
    private static String FO_JSON_RESPONSE = "${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/input/foResponse.json"
    private static String FO_FULL_XML = "${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/crcContent.xml"

    public void preparePdf(byte[] foFile, String target) throws Exception {


        FopFactory fopFactory = FopFactory.newInstance(new File(FOP_XML))

        // Step 2: Set up output stream.
        // Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(target)))
        try {
            // Step 3: Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance()
            Transformer transformer = factory.newTransformer() // identity transformer

            //Generate FO From Groovy Script and Data

            // Step 5: Setup input and output for XSLT transformation
            // Setup input stream
            ByteArrayInputStream bin = new ByteArrayInputStream(foFile)
            Source src = new SAXSource(new InputSource(new InputStreamReader(bin, "UTF-8")))

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler())

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res)

        } finally {
            //Clean-up
            out.close()
        }
    }

    private def getHeaderPath(def template) {
        return root_path + "/data/${template}.txt"

    }

    private static String getFileContent(String textFilePath) {
        String data = ""
        data = new String(Files.readAllBytes(Paths.get(textFilePath)))
        return data
    }

    public static void main(String[] args) throws Exception {

        GenerateCRCPDF2 pdfGenerator = new GenerateCRCPDF2()
        String target = root_path + "/generated/generatedPdf.pdf"


        String foRespBodyXML = parseJson(FO_JSON_RESPONSE)
        String foFullXML = getFullFOXMLString(foRespBodyXML)
        def data = [:]
        data.name = "Manjit"
        foFullXML = prepareFOString(data, foFullXML);
        pdfGenerator.preparePdf(foFullXML.toString().bytes, target)
    }

    private static String getFullFOXMLString(String foBody) {
        DocumentBuilderFactory documentBuildFactory = DocumentBuilderFactory.newInstance()
        documentBuildFactory.setNamespaceAware(true)
        DocumentBuilder doccumentBuilder = documentBuildFactory.newDocumentBuilder()
        String docNS = "http://www.w3.org/1999/XSL/Format"
        Document rootDocument = doccumentBuilder.parse(new File(FO_FULL_XML))
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
        print xml
        return xml
    }

    private static String convertToXMLString(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        tf.setOutputProperty(OutputKeys.INDENT, "yes")
        Writer out = new StringWriter()
        tf.transform(new DOMSource(xml), new StreamResult(out))
        print out.toString()
        return out.toString()
    }

    private static String prepareFOString(Object binding, String tmp) {
        try {
            def engine = new XmlTemplateEngine()
            Template template = engine.createTemplate(tmp)
            Writable wrt = template.make((Map) binding)
            return wrt.toString()
        } catch (Exception e) {
            println e
        }
        //ByteArrayOutputStream os = new ByteArrayOutputStream()
        //wrt.writeTo(new BufferedWriter(new OutputStreamWriter(os)))
        return null;
    }


}

