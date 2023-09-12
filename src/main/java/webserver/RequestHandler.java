package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        String line = "", requestPath = "";
        String rootPath = getFullPath();

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             DataOutputStream dos = new DataOutputStream(out)) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            /*
            while((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (line.contains("GET") || line.contains("POST")) {
                    requestPath = line.split("\\s")[1];
                    System.out.println(requestPath);
                    break;
                }
            }
            */

            line = reader.readLine();
            if (line.contains("GET") || line.contains("POST")) {
                requestPath = line.split("\\s")[1];
                System.out.println(requestPath);
            }

            /*
            Path path = Path.of(fullPath+"/webapp/",requestPath);
            File file = new File(path.toString());
            System.out.println(file.isFile()+","+file.exists());
            byte[] body = Files.readAllBytes(path);
             */

            File file = getRequestFile(rootPath, requestPath);
            byte[] body = null;
            if (file.exists() && file.isFile()) {
                //body = getRequestFileContent(requestPath);
                body = getResponseContentFile(file);
                response200Header(dos, body.length);
            } else {
                response400Header(dos, 0);
            }
            responseBody(dos, body);

            /*
            System.out.println(in.available());
            System.out.println("========================================");
            while((val = in.read(readBuffer)) != -1) {
                System.out.println(Arrays.toString(readBuffer));
                System.out.println("val:"+val);
            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
             */
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private File getRequestFile(String rootPath, String requestPath) throws IOException {
        Path path = Path.of(rootPath+"/webapp/",requestPath);
        return (new File(path.toString()));
    }

    private byte[] getResponseContentFile(File file) throws IOException {
        return Files.readAllBytes(file.getAbsoluteFile().toPath());
    }

    private byte[] getRequestFileContent(String requestPath) throws IOException {
        Path path = Path.of(requestPath+"/webapp/",requestPath);
        File file = new File(path.toString());
        System.out.println(file.isFile()+","+file.exists());
        return Files.readAllBytes(path);
    }

    private String getFullPath() {
        Path currentPath = Paths.get("");
        return currentPath.toAbsolutePath().toString();
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response400Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 400 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            //dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            if (body != null && body.length > 0) {
                dos.write(body, 0, body.length);
            }
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
