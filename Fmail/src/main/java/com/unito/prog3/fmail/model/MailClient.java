package com.unito.prog3.fmail.model;

import com.unito.prog3.fmail.support.Support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

public class MailClient {
    private Mailbox mailbox;
    private InetAddress local;

    /**
     * {@code MailClient} Constructor
     * @param mailbox
     */
    public MailClient(Mailbox mailbox){
        try{
            local = InetAddress.getLocalHost();
        }catch (UnknownHostException e){e.printStackTrace();}
        this.mailbox = mailbox;
    }

    public Boolean getConnection() {
        boolean connection_established = false;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            Socket client_socket = new Socket(this.local, Support.port);

            try {
                out = new ObjectOutputStream(client_socket.getOutputStream());
                in = new ObjectInputStream(client_socket.getInputStream());

                Objects.requireNonNull(out).writeObject(this.mailbox.getAccount_name());

                Object input = in.readObject();
                if (input.equals("true"))
                    connection_established = true;
                System.out.println("Connection established: " + connection_established);

                return connection_established;
            }finally {out.flush();in.close();out.close();client_socket.close();}
        }catch (IOException | ClassNotFoundException e){e.printStackTrace();}
    return connection_established;
    }

    public Mailbox getMailbox() {return mailbox;}

    @Override
    public String toString() {
        return "\n      MailClient{" +
                ", mailbox=" + mailbox +
                "}";
    }

}

