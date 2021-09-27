package com.mnzit.groov

import groovy.json.JsonSlurper
import groovy.text.TemplateEngine
import groovy.text.XmlTemplateEngine
import org.apache.commons.io.IOUtils
import org.apache.fop.apps.Fop
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.sax.SAXSource

class NewTest2 {

    byte[] prepareFOString(String xmlTemplatePath, def binding) throws ParserConfigurationException, SAXException {
        TemplateEngine foProcessing = new XmlTemplateEngine()
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos))
        foProcessing.createTemplate(new File(xmlTemplatePath)).make(binding).writeTo(writer)
        return bos.toByteArray()
    }

    void preparePdf(byte[] foFile, String target) throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File("."))


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
            Source src = new SAXSource(new InputSource(new InputStreamReader(bin)))

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler())

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res)

        } finally {
            //Clean-up
            out.close()
        }
    }


    static void main(String[] args) throws Exception {
        NewTest2 pdfGen = new NewTest2()
        String target = "newTest2.pdf"
        def foString = ""
        try {
            foString = new String(pdfGen.prepareFOString("/Users/manjitshakya/playground/groovy-compile-example/src/main/resources/com/bofa/omni/tools/crc/data/fo-template.xml", pdfGen.getJsonData()), "UTF-8")
//			println foString
            pdfGen.preparePdf(foString.bytes, target)
//			pdfGen.preparePdf(pdfGen.minifyXML(foString).bytes,target)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            def FOOTNOTE_TOKEN = "{FN_"
//			def FOOTNOTE_REG_TOKEN="((?<=\\${FOOTNOTE_TOKEN}\\d{1,3}\\})|(?=\\${FOOTNOTE_TOKEN}\\d{1,3}\\}))"
            def FOOTNOTE_REG_TOKEN = "\\{FN_|\\}"
            def ANCHOR_REG = "<a></a>"

//			def FOOTNOTE_REG_TOKEN="\\${FOOTNOTE_TOKEN}\\d+\\}"
            def instruction1 = "<a href='https://go.ml.com/lbd26url' target='_blank' data-bactmln='action-AC200002-step121-link0'>budget and debt management worksheet</a> Start by identifying and learning about possible guaranteed income sources – Social Security, pensions, or {FN_1}  annuities. Almost everything else is considered nonguaranteed.{FN_10}  {FN_101}{FN_111}"
            def instructLink = "Start by <a href='https://go.ml.com/lbd26url' target='_blank' data-bactmln='action-AC200002-step121-link0'>budget and debt management worksheet</a>"

            //println instruction1.replaceAll(FOOTNOTE_REG_TOKEN, "<test> </test>")

            //			instruction1.split(/(FN_1)/).each{
//			def previousToken=false;
//				instruction1.split(FOOTNOTE_REG_TOKEN).each{
//										Pattern p = Pattern.compile("\\{FN_\\d{1,3}\\}");  // insert your pattern here
//										Matcher m = p.matcher(it);
//										if(org.apache.commons.lang3.StringUtils.isBlank(it)){
//											previousToken = false;
//											println "1<${it}>"
//										}
//										else if(it.startsWith(FOOTNOTE_TOKEN)){
//											def newStr= it.substring(4, it.length()-1);
//											if(previousToken){
//												println "2< >"
//											}
//											previousToken= true
//											println it
//										}else{
//				                            previousToken=false
//											println it
//										}
//			}


            def pattern = "\\{FN_(\\d+)\\}"
            //~/\{FN_(\d+)\}/
            def linkPattern = "<a[^>]*>([^<]+)</a>"

            def newVersion = instruction1.replaceAll(pattern) { a, num -> "<super scruipt>$num some more $a<end script>" }
            /*def newInstTitle = instructLink.replaceAll(linkPattern) { a,title -> "$title"}*/
            def FOOTNOTE_REG_TOKEN_LNK = "<a[^>]*?href=('([^']*?)').*?>(.*?)</a>"
            def newInstTitleRe = (newVersion).replaceAll(FOOTNOTE_REG_TOKEN_LNK) { fnToken, linkUrl, txt, linkTitle -> "&lt;fo:basic-link space-before='15px' external-destination='url($linkUrl)' fox:alt-text='$linkTitle' show-destination='new'&gt; $linkTitle&lt;/fo:basic-link&gt;" }
            def anchorLink = instructLink.split("<a")
            def instruct = "Start by <a href='https://go.ml.com/lbd26url' target='_blank' data-bactmln='action-AC200002-step121-link0'>budget and debt management worksheet</a>"
            def dd = instruct.findAll(FOOTNOTE_REG_TOKEN_LNK) { text, linkUrl, txt, linkTitle -> linkTitle }


            println dd

        }
    }

    String minifyXML(String source) {
        BufferedReader reader = new BufferedReader(new StringReader(source))
        StringBuilder buffer = new StringBuilder(source.size())
        reader.eachLine {
            buffer.append(it.trim())
        }
        IOUtils.closeQuietly(reader)
        return buffer.toString()
    }

//
//    byte[] getFoString() {
//        File file = new File("/Users/manjitshakya/playground/groovy-compile-example/src/main/resources/com/bofa/omni/tools/crc/data/fo.xml");
//        byte[] binaryContent = file.bytes
//        return binaryContent
//    }


    Map<String, Object> getJsonData() {
        def data = [:]
        def inputFile = new File("/Users/manjitshakya/playground/groovy-compile-example/src/main/resources/com/bofa/omni/tools/crc/data/input/data1.json")
        data = new JsonSlurper().parseText(inputFile.text)

        return data
    }

}



