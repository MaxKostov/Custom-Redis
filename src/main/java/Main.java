import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args){
    ServerSocket serverSocket = null;
    int port = 6379;
    try {
      serverSocket = new ServerSocket(port);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      // Wait for connection from client.
      while(true){
        Socket clientSocket = serverSocket.accept();
        Thread thread = new Thread(() -> {clientSocketHandler(clientSocket);});
        thread.start();
        }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }

  public static void clientSocketHandler(Socket clientSocket) {
    try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
         BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8))) {

      while (!clientSocket.isClosed()) {
        String firstLine = input.readLine();
        if (firstLine == null) break;

        if (firstLine.startsWith("*")) {
          int numberOfCommands = Integer.parseInt(firstLine.substring(1));
          if (numberOfCommands < 1) continue;

          input.readLine();
          String command = input.readLine();

          if (command.equalsIgnoreCase("ping")) {
            output.write("+PONG\r\n");
            output.flush();
          } else if (command.equalsIgnoreCase("echo")) {
            if (numberOfCommands > 1) {
              StringBuilder builder = new StringBuilder();
              int totalLength = 0;

              for (int i = 1; i < numberOfCommands; i++) {
                input.readLine();
                String arg = input.readLine();
                if (i > 1) {
                  builder.append(" ");
                  totalLength++;
                }
                builder.append(arg);
                totalLength += arg.length();
              }

              output.write("$" + totalLength + "\r\n" + builder + "\r\n");
              output.flush();
            } else {
              output.write("-ERR wrong number of arguments for 'echo' command\r\n");
              output.flush();
            }
          } else {
            output.write("-ERR unknown command '" + command + "'\r\n");
            output.flush();
          }
        }
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
