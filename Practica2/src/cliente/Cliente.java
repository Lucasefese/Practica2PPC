package cliente;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import com.google.gson.*;


import extras.DTD;
import extras.Sax;

public class Cliente extends Thread{
    private DatagramSocket socket;
    private InetAddress address;
    private Map<Integer, String> mapaServers = new HashMap<Integer, String>();
    private String escGlob;
    private String form = "XML";
    public void run(){
        try {
            socket = new DatagramSocket(9000);
            address = InetAddress.getByName("localhost");
            System.out.println("Los mensajes de control disponibles actualmente son: END | CAMBIAR_FREQ_ms | STOP | START | ESCRIBIR_XML | ESCRIBIR_JSON");
            iniciarLectura();
            new Thread(()->{
                byte[] buf = new byte[256];
                try {
                    while(true) {
                        //antes de enviar mensaje me aseguro de que el buffer este vacio
                        Arrays.fill(buf, (byte) 0);
                        BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in));
                        String mensaje = entradaTeclado.readLine();
                        mostrarMapa();
                        String server = entradaTeclado.readLine();
                        if(mensaje.equals("END") || mensaje.startsWith("CAMBIAR_FREQ_") || mensaje.equals("STOP") || mensaje.equals("START") || mensaje.startsWith("ESCRIBIR_XML") || mensaje.startsWith("ESCRIBIR_JSON")) {
                            if(mensaje.startsWith("ESCRIBIR_XML")){
                                form="XML";
                            }else if(mensaje.startsWith("ESCRIBIR_JSON")){
                                form="JSON";
                            }
                            if(form.equals("XML")){
                                serializaXml(mensaje);
                                mensaje = form + ' ' + escGlob;
                                if(server.equals("1")){
                                    buf=mensaje.getBytes();
                                    DatagramPacket p1 = new DatagramPacket(buf, buf.length, address, 8000);
                                    socket.send(p1);
                                }else if(server.equals("2")) {
                                    buf=mensaje.getBytes();
                                    DatagramPacket p1 = new DatagramPacket(buf, buf.length, address, 8001);
                                    socket.send(p1);
                                }else if(server.equals("3")) {
                                    buf = mensaje.getBytes();
                                    DatagramPacket p1 = new DatagramPacket(buf, buf.length, address, 8002);
                                    socket.send(p1);
                                }
                                }else if(form.equals("JSON")){
                                    serializarJSON(mensaje);
                                    mensaje = form + ' ' + escGlob;
                                    if(server.equals("1")) {
                                        buf=mensaje.getBytes();
                                        DatagramPacket p1 = new DatagramPacket(buf, buf.length, address, 8000);
                                        socket.send(p1);
                                    }else if(server.equals("2")) {
                                        buf=mensaje.getBytes();
                                        DatagramPacket p1 = new DatagramPacket(buf, buf.length, address, 8001);
                                        socket.send(p1);
                                    }else if(server.equals("3")) {
                                        buf=mensaje.getBytes();
                                        DatagramPacket p1 = new DatagramPacket(buf, buf.length, address, 8002);
                                        socket.send(p1);
                                    }
                                }

                        }else{
                            System.out.println("El mensaje escrito no es valido, las opciones son END | CAMBIAR_FREQ ms | STOP | START | ESCRIBIR XML | ESCRIBIR JSON");

                        }

                        mensaje="";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarLectura() {
        new Thread(()->{
            try {
                byte[] buf = new byte[256];
                //antes de recibir el mensaje me aseguro de que el buffer este vacio
                while(true) {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p);
                    if(!mapaServers.containsKey(p.getPort())) {
                        mapaServers.put(p.getPort(), p.getAddress().getHostAddress());
                    }
                    String recibido = new String(p.getData(), 0, p.getLength());
                    StringTokenizer rec = new StringTokenizer(recibido," ");
                    String nombreSer = rec.nextToken().toString();
                    rec.nextToken();
                    String format = rec.nextToken().toString();
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
                    if(format.equals("XML")){
                        mostrarContenidoDeserializadoXML(nombreSer, info);
                    }else {
                        mostrarContenidoDeserializadoJSON(nombreSer, info);
                    }
                    Arrays.fill(buf, (byte) 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void close() {
        socket.close();
    }

    public void mostrarMapa() {
        int contador=1;
        for (Integer clave : mapaServers.keySet()) {
            String valor = mapaServers.get(clave); // Obtener el valor correspondiente a la clave
            System.out.println(contador + ":" + valor + ":" + clave);
            contador++;
        }
    }


    public boolean mostrarContenidoDeserializadoXML(String nombreServer, String info){
        String rutaArchivo = "./sources/" + nombreServer + ".xml"; // Cambia esto por la ruta que desee
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

            boolean validacion = validarArchivo(nombreServer);
            if(!validacion){
                System.out.println("El mensaje enviado por el servidor esta mal parseado");
                return false;
            }

            // Crear una instancia de SAXParserFactory y SAXParser
            System.out.println("El contenido deserializado en XML de " + nombreServer + " es:");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser;
            saxParser = factory.newSAXParser();
            // Crear el manejador que muestra los datos
            Sax handler = new Sax();

            // Parsear el archivo XML
            saxParser.parse(new File(rutaArchivo), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void mostrarContenidoDeserializadoJSON(String nombreSer, String info){
        try {
            // Imprimir el resultado
            // System.out.println(escGlob);
            String rutaArchivo = "./sources/" + nombreSer + ".js"; // Cambia esto por la ruta que desees

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

            System.out.println("El contenido deserializado en JSON de " + nombreSer + " es:");

            // Recorrer las claves y obtener los valores
            for (String clave : claves) {
                System.out.println(clave + ": " + jsonObject.get(clave));
            }


            // Cerrar el lector
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean validarArchivo(String nombreServer) {
        DTD validator = new DTD();
        String xmlFilePath = "./sources/" + nombreServer +".xml";

        return validator.validarXMLConDTD(xmlFilePath);
    }

    public void serializaXml(String orden) {
        escGlob = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n"  + "<!DOCTYPE Cliente SYSTEM \"cliente.dtd\">" + "\n" + "<Cliente>" + "\n" +  "\t<orden>" + orden + "</orden>" + "\n"  + "</Cliente>";
    }

    public void serializarJSON(String orden) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("orden", orden);

        // Crear Gson con Pretty Printing
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convertir JsonObject a cadena JSON
        escGlob = gson.toJson(jsonObject);
    }
}