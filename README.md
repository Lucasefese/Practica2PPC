# Practica2PPC
 Segunda práctica de la asignatura de PPC de la universidad de Murcia

 La práctica se basa en crear un servidor, donde tenemos un cliente y tres servidores que se comunican mediante el protocolo UDP.

Tenemos una clase controlador que es la única que tenemos que ejecutar para poner en marcha todo, ya que aqui es donde lanzamos los hilos de "ServerPrincipal" y "Cliente".

La clase ServerPrincipal lanza tres hilos de la clase Server, cada uno con un puerto diferente. 

La clase Server dispone de dos hilos en ejecución, uno para la escritura al cliente y otro para la lectura, en cuanto a la escritura se hace mendiante broadcoast, con
una frecuencia de base de 2000 ms, sin embargo, si el cliente se comunica con un servidor concreto la respuesta se hace mediante unicast. En cuanto a la lectura, el hilo
escucha de forma constante esperando un mensaje por parte del cliente.

El cliente también dispone de dos hilos, uno de escritura y otro de lectura, como los servidores, en cuanto a la escritura se disponen de dos mensajes para poder mandarse,
 cuando ponemos uno de estos mensajes automáticamente aperece en la pantalla el número de servidores a los que le podemos mandar un mensaje, y ya nosotros elegimos a cual hacerlo
 "END" que acaba la comunicación con el servidor que decidamos, "CAMBIAR_FREQ ms" que cambia la frecuencia de envio del servidor que decidamos, "STOP" corta la comunicación con el servidor que eligamos, mientras que "START" vuelve a iniciar la comunicación, cabe puntalizar que con "END" no es posible volver a iniciar la comunicación, también esta implementado el comando ESCRIBIR XML que hace que los datos enviado por los servidores al cliente estén formato XML, al igual que esta implementado el mensaje ESCRIBIR JSON, que cambia el formato de mensaje enviado por los servidores a JSON.

 El mensaje que envia el servidor en formato broadcoast, es el nombre del servidor, el puerto que utiliza, un campo que especifica si el mensaje es XML o JSON y un cuerpo de texto en formato elegido por el cliente de los anteriormente nombrados, la información que contienen los datos enviados varía en función del servidor que este mandando el mensaje.

 El servidor antes de enviar el mensaje en el caso de que los datos sean mandados en formato XML serializa los datos que quiere mandar al cliente para que tengan la forma de un archivo XML, una vez que le llegan al cliente, este escribe en un archivo .xml la información recibido y lo valida con el esquema DTD, y en el caso de que este bien escrito, lo deserializa para mostrar por pantalla los valores que le ha mandado el servidor.

En el caso de que los datos enviados por el servidor tengan que estar en formato JSON, este los serializa con las funcionalidades del paquete GSON, y envia la información al cliente, una vez que llegan al cliente, este los escribe en un archivo .json, y directamente lo deserializa tambien con las funcionalides GSON y muestra la información pantalla, en este caso no hace falta validar el archivo al ser en formato JSON, ya que damos por hecho que al utilizar GSON los datos enviado han sido correctos.
 
