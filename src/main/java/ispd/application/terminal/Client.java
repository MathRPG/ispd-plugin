package ispd.application.terminal;

import ispd.motor.metrics.*;
import java.io.*;
import java.net.*;
import org.w3c.dom.*;

/**
 * A helper class for the client part of the terminal application simulation.
 */
public class Client {

    private final InetAddress serverAddress;

    private final int serverPort;

    private int clientPort = 0;

    public Client (final InetAddress serverAddress, final int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort    = serverPort;
    }

    /**
     * Sends a model to a server for simulation.
     *
     * @param model
     *     A configuration file for setting up a simulation.
     */
    public void sendModelToServer (final Document model) {
        try (
            final var socket = new Socket(this.serverAddress.getHostName(), this.serverPort);
            final var outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            socket.setReuseAddress(true);
            outputStream.writeObject(model);
            this.clientPort = socket.getLocalPort();
        } catch (final IOException ignored) {
            System.out.println("Couldn't create the client socket.");
        }
    }

    /**
     * Get the metrics from the server and returns it.
     *
     * @return The metrics from a simulation
     */
    public General receiveMetricsFromServer () {
        try (
            final var serverSocket = new ServerSocket(this.clientPort);
            final var inputStream = new ObjectInputStream(serverSocket.accept().getInputStream())
        ) {
            return (General) inputStream.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}
