package extras;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Sax extends DefaultHandler {
    private StringBuilder currentValue = new StringBuilder();
    private String orden;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Limpiar el valor actual cuando se encuentra una nueva etiqueta
        currentValue.setLength(0);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Mostrar el contenido de la etiqueta cuando se cierra
        switch (qName.toLowerCase()) {
            case "temp":
                System.out.println("Temperatura: " + currentValue.toString() + " C");
                break;
            case "hv":
                System.out.println("Humedad relativa: " + currentValue.toString() + " %");
                break;
            case "hpa":
                System.out.println("Presión atmosférica: " + currentValue.toString() + " hPa");
                break;
            case "nub":
                System.out.println("Nubosidad: " + currentValue.toString());
                break;
            case "vien":
                System.out.println("Viento: " + currentValue.toString() + " Km/h");
                break;
            case "pr":
                System.out.println("Precipitación: " + currentValue.toString() + " mm");
                break;
            case "orden":
                System.out.println("La orden dada por el cliente es " + currentValue.toString());
                orden=currentValue.toString();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // Almacenar el contenido de la etiqueta actual
        currentValue.append(ch, start, length);
    }

    public String getOrden() {
        return orden;
    }
}