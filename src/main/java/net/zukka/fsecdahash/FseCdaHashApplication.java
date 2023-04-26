
/*BSD 3-Clause License
 *
 *Copyright (c) 2023, Andrea Zucchelli
*/
package net.zukka.fsecdahash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class FseCdaHashApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(FseCdaHashApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Application started");
		org.apache.xml.security.Init.init();
		// read input xml
		if (args.length == 0) {
			log.error("No input file given");
			return;
		}
		var inputFile = new FileInputStream(args[0]);
		var xml = inputFile.readAllBytes();
		inputFile.close();
		var xmlInputDigest = DigestUtils.sha256Hex(xml);

		var xmlStriped = stripLegalAuthenticator(xml);
		var canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
		var canonicalizedXmlBAOS = new ByteArrayOutputStream();
		canonicalizer.canonicalize(xmlStriped, canonicalizedXmlBAOS, true);

		var canonicalizedXmlBA = canonicalizedXmlBAOS.toByteArray();

		var xmlOutputDigest = DigestUtils.sha256Hex(canonicalizedXmlBA);

		log.debug("Canonicalized XML:\n{}", new String(canonicalizedXmlBA, "UTF-8"));
		log.info("Input sha:\n{}\nOutput sha:\n{}", xmlInputDigest, xmlOutputDigest);

	}

	public byte[] stripLegalAuthenticator(byte[] inputxml) throws TransformerException, SAXException, IOException,
			ParserConfigurationException, XPathExpressionException {
		var dbf = DocumentBuilderFactory.newInstance();
		var document = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(inputxml));

		var xpf = XPathFactory.newInstance();
		var xpath = xpf.newXPath();
		var expression = xpath.compile("//legalAuthenticator");

		var legalAuthenticatorNode = (Node) expression.evaluate(document, XPathConstants.NODE);
		if (legalAuthenticatorNode != null) {
			legalAuthenticatorNode.getParentNode().removeChild(legalAuthenticatorNode);
		} else {
			log.warn("legalAuthenticator node not found");
		}
		var tf = TransformerFactory.newInstance();
		var t = tf.newTransformer();
		var strippedDocBAOS = new ByteArrayOutputStream();
		t.transform(new DOMSource(document), new StreamResult(strippedDocBAOS));
		return strippedDocBAOS.toByteArray();
	}

}
