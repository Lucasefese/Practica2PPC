package server;


import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.google.gson.*;
import extras.DTD;
import extras.Sax;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class S1 extends Thread {
    //TODO HACER FUNCIONES PARA QUE NO SE HAGA TODO EN UNA FUNCION
    private DatagramSocket socket;
    private InetAddress address;
    private boolean running = true;
    private int FREQ=10000;
    private String nombreServer;
    private int puerto;
    private double temp;
    private double hv;
    private String formato = "XML";
    private boolean nonStop;
    private String escGlob;

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public void setHv(int hv) {
        this.hv = hv;
    }

    public S1(int puerto, String nombServer, int tem, int hv) {
        try {
            this.puerto = puerto;
            temp = tem;
            this.hv = hv;
            socket = new DatagramSocket(puerto);
            nombreServer = nombServer;
            try {
                socket.setBroadcast(true);
                address = InetAddress.getByName("255.255.255.255");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public S1(int tem , int hv) {
        temp=tem;
        this.hv=hv;
    }

    public void run(){
        serializaXml(nombreServer);
        iniciarEscritura();
        new Thread(()->{
            try {
                byte[] buf = new byte[256];
                while (running){
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    //Cogemos solo los byte validos del mensaje que nos llega desde el cliente
                    String recibido = new String(packet.getData(), 0, packet.getLength()).trim();
                    StringTokenizer rec = new StringTokenizer(recibido," ");
                    String formatoCLiente = rec.nextToken().toString();
                    String info="";
                    /*
                    Como la información en XML tiene espacios en blancos tenemos que quedarnos con toda
                    la información restante teniendo en cuenta este detalle
                    */
                    while (rec.hasMoreTokens()) {
                        info = info + " " + rec.nextToken().toString();
                    }
                    //Ahora tenemos que eliminar el primer hueco en blanco que se forma al hacer info=info + x
                    info=info.trim();
                    String orden="";
                    if(formatoCLiente.equals("XML")){
                        orden = mostrarContenidoDeserializadoXML(info);
                    }else if(formatoCLiente.equals("JSON")){
                        orden = mostrarContenidoDeserializadoJSON(info);
                    }

                    if (orden.equals("END")){
                        close();
                        running = false;
                        nonStop=false;
                        continue;
                    }else if(orden.startsWith("CAMBIAR_FREQ")) {
                        StringTokenizer eliminarNombre = new StringTokenizer(orden,"_");
                        eliminarNombre.nextToken();
                        eliminarNombre.nextToken();
                        String freq = eliminarNombre.nextToken().toString();
                        FREQ = Integer.parseInt(freq);
                    }else if(orden.equals("STOP")) {
                        nonStop=false;
                    }else if(orden.equals("START")){
                        nonStop=true;
                        iniciarEscritura();
                    }else if(orden.equals("ESCRIBIR_XML")){
                        formato="XML";
                        serializaXml(nombreServer);
                    }else if(orden.equals("ESCRIBIR_JSON")){
                        formato="JSON";
                        serializarJSON(nombreServer);
                    }
                    //vaciamos el buffer para enviar el mensaje con el buffer vacio
                    Arrays.fill(buf, (byte) 0);

                }

                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void iniciarEscritura() {
        new Thread(()->{
            try {
                byte[] buf = new byte[256];
                nonStop=true;
                while (nonStop){
                    String msg = nombreServer + " " + puerto + " " + formato + " " + escGlob;
                    //System.out.println(msg);
                    buf = msg.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length,address,9000);
                    socket.send(packet);
                    //vaciamos el buffer para hacer una mejor lectura del siguiente mensaje
                    Arrays.fill(buf, (byte)0);
                    Thread.sleep(FREQ);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void serializaXml(String nombreServer) {
        escGlob = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"  + "<!DOCTYPE Server SYSTEM \"S1.dtd\">" + "\n" + "<Server>" + "\n" +  "\t<temp>" + temp + "</temp>" + "\n" + "\t<hv>" + hv + "</hv>" + "\n" + "</Server>";
        //String rutaArchivo = "./sources/" + nombreServer + ".xml"; // Cambia esto por la ruta que desee
    }

    public void serializarJSON(String nombreServer) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("temperatura", temp);
        jsonObject.addProperty("Humedad Relativa", hv);

        // Crear Gson con Pretty Printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convertir JsonObject a cadena JSON
        escGlob = gson.toJson(jsonObject);
    }

    public boolean validarArchivo() {
        DTD validator = new DTD();
        String xmlFilePath = "./sources/cliente.xml";

        return validator.validarXMLConDTD(xmlFilePath);
    }


    public String mostrarContenidoDeserializadoXML(String info){
        String rutaArchivo = "./sources/cliente.xml"; // Cambia esto por la ruta que desee
        //System.out.println(info);
        try {

            // Crear un objeto File para representar el archivo
            File archivo = new File(rutaArchivo);
            archivo.delete();

            if(!archivo.exists()) {
                try {
                    archivo.createNewFile();
                    FileWriter fileWrites = new FileWriter(archivo, true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWrites);
                    bufferedWriter.write(info);
                    bufferedWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            boolean validacion = validarArchivo();
            if(!validacion){
                System.out.println("El mensaje enviado por el cliente esta mal parseado");
                return "false";
            }

            // Crear una instancia de SAXParserFactory y SAXParser
            System.out.println("El contenido deserializado en XML del cliente en el server " + nombreServer +" es:");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser;
            saxParser = factory.newSAXParser();
            // Crear el manejador que muestra los datos
            Sax handler = new Sax();

            // Parsear el archivo XML
            saxParser.parse(new File(rutaArchivo), handler);
            return handler.getOrden();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "true";
    }

    public String mostrarContenidoDeserializadoJSON(String info){
        try {
            // Imprimir el resultado
            // System.out.println(escGlob);
            String rutaArchivo = "./sources/cliente.js"; // Cambia esto por la ruta que desees

            // Crear un objeto File para representar el archivo
            File archivo = new File(rutaArchivo);
            archivo.delete();

            if(!archivo.exists()) {
                try{
                    archivo.createNewFile();
                    FileWriter fileWrites = new FileWriter(archivo, true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWrites);
                    bufferedWriter.write(info);
                    bufferedWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            //Leemos archivo JSON
            FileReader reader = new FileReader(rutaArchivo);

            // Parsear el archivo a JsonObject
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Obtener las claves dinámicamente
            Set<String> claves = jsonObject.keySet();

            System.out.println("El contenido deserializado en JSON del cliente en el server " + nombreServer +" es:");
            String orden="";
            // Recorrer las claves y obtener los valores
            for (String clave : claves) {
                 orden = String.valueOf(jsonObject.get(clave));
                System.out.println(clave + ": " + jsonObject.get(clave));
            }

            // Cerrar el lector
            reader.close();
            return orden.substring(1, orden.length() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "true";
    }

    public void close() {
        socket.close();
    }
}