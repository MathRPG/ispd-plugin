package ispd.application.terminal;

import ispd.motor.metrics.*;
import ispd.utils.constants.*;
import java.io.*;
import java.net.*;
import org.w3c.dom.*;

/**
 * A helper class for the server part of the terminal application simulation.
 */
public class Server {

    private final int serverPort;

    private int clientPort = 0;

    private InetAddress clientAddress;

    public Server (final int serverPort)
        throws UnknownHostException {
        this.serverPort    = serverPort;
        this.clientAddress = InetAddress.getByName(StringConstants.LOCALHOST);
    }

    /**
     * Open a port for incoming of a model from a client and returns it.
     *
     * @return A configuration file for setting up a simulation
     */
    public Document getMetricsFromClient () {
        try (
            final var serverSocket = new ServerSocket(this.serverPort)
        ) {
            final var inputSocket = serverSocket.accept();
            final var inputStream = new ObjectInputStream(inputSocket.getInputStream());

            this.clientPort    = inputSocket.getPort();
            this.clientAddress = inputSocket.getInetAddress();

            return (Document) inputStream.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            System.out.println("Couldn't create the server socket.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Return metrics from a simulation to the client that asked for it.
     *
     * @param modelMetrics
     *     Metrics from a simulation result
     */
    public void returnMetricsToClient (final General modelMetrics) {
        try (
            final var outputSocket = new Socket(this.clientAddress, this.clientPort);
            final var outputStream = new ObjectOutputStream(outputSocket.getOutputStream())
        ) {
            outputStream.writeObject(modelMetrics);
        } catch (final IOException ignored) {
            System.out.println("Couldn't create the client socket.");
        }
    }
}
