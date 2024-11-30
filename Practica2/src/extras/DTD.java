package extras;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import java.io.File;

public class DTD {

    public boolean validarXMLConDTD(String xmlFilePath) {
        try {
            // Configurar el DocumentBuilderFactory para usar DTD
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            // Crear el DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();
            final boolean[] isValid = { true }; // Usar un array para permitir modificación dentro del ErrorHandler

            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    System.out.println("WARNING: " + e.getMessage());
                    isValid[0] = false;
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    System.out.println("ERROR: " + e.getMessage());
                    isValid[0] = false;
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    System.out.println("FATAL ERROR: " + e.getMessage());
                    isValid[0] = false;
                }
            });

            // Parsear el archivo XML
            Document document = builder.parse(new File(xmlFilePath));
            return isValid[0]; // Devolver el estado de validación
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Si hay una excepción, el XML no es válido
        }
    }
}