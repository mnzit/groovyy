package com.mnzit.groov

import groovy.text.Template
import org.apache.fop.apps.Fop
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.xml.sax.InputSource

import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.sax.SAXSource

class Demo {
    private static String root_path= "${System.properties['user.dir']}/src/main/resources/com/bofa/omni/tools/crc"
    private static String FOP_XML="${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/crc-fo.xml"
    private static String FO_JSON_RESPONSE="${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/input/foResponse.json"
    private static String FO_FULL_XML="${System.properties.'user.dir'}/src/main/resources/com/bofa/omni/tools/crc/data/crcContent.xml"

    private void preparePdf(String foString, Fop fop, Transformer transformer) throws Exception{
        // Setup input and output for XSLT transformation
        //LOGGER.info "${foString}"
        Source src = new SAXSource(new InputSource(new StringReader(foString)))

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler())

        // Start XSLT transformation and FOP processing
        transformer.transform(src, res)
    }

    // removed as footnote space issue been resolved
    // private String minifyXML(String source) {
    // 	BufferedReader reader = new BufferedReader(new StringReader(source))
    // 	StringBuilder buffer = new StringBuilder(source.size())
    // 	reader.eachLine {
    // 		buffer.append(it.trim())
    // 	}
    // 	IOUtils.closeQuietly(reader)
    // 	return buffer.toString()
    // }

    private String prepareFOString(Object binding) {
        Template template = (Template)getContext().getObject("assessment.download.pdf.fo.template")
        Writable wrt =template.make((Map)binding)

        //ByteArrayOutputStream os = new ByteArrayOutputStream()
        //wrt.writeTo(new BufferedWriter(new OutputStreamWriter(os)))
        return wrt.toString()
    }

    private Fop createFop() {
        String target=root_path+"/generated/generatedPdf.pdf"

            FopFactory fopFactory = FopFactory.newInstance(new File(FOP_XML))

            // Step 2: Set up output stream.
            // Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(target)))

                // Step 3: Construct fop with desired output format
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)

        return fopFactory.newFop(MimeConstants.MIME_PDF, out)
    }


}
