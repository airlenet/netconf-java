package com.airlenet.netconf;

import com.sshtools.net.SocketWrapper;
import com.sshtools.publickey.SshPrivateKeyFile;
import com.sshtools.publickey.SshPrivateKeyFileFactory;
import com.sshtools.ssh.*;
import com.sshtools.ssh.components.SshKeyPair;
import com.sshtools.ssh.components.SshPublicKey;
import com.sshtools.ssh.components.jce.SshX509RsaPublicKey;
import com.sshtools.ssh2.Ssh2Client;
import com.sshtools.ssh2.Ssh2Context;
import com.sshtools.ssh2.Ssh2Session;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
class DeviceHandler implements Runnable {

    // private vars
    Socket socket = null;
    Properties properties = null;

    String client_hello = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
            + "  <capabilities>\n"
            + "    <capability>\n"
            + "      urn:ietf:params:netconf:base:1.1\n"
            + "    </capability>\n"
            + "  </capabilities>\n"
            + "</hello>\n"
            + "]]>]]>\n";

    String client_goodbye = ""
            + "<rpc message-id=\"101\"\n"
            + "     xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
            + "  <close-session/>\n"
            + "</rpc>\n"
            + "]]>]]>\n";

    String set_public_key_preamble = ""
            + "<rpc message-id=\"101\"\n"
            + "     xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
            + "  <set-public-key xmlns=\"example.com:1.0\">\n";

    String set_public_key_postamble = ""
            + "\n  </set-public-key>\n"
            + "</rpc>\n"
            + "]]>]]>\n";


    public DeviceHandler(Socket socket, Properties properties) {
        this.socket = socket;
        this.properties = properties;  // does NOT require synchronization
    }


    @Override
    public void run() {
        final String  trusted_ca_cert;
        String  username="admin";
        String  private_key;
        String  password="admin";

        trusted_ca_cert = properties.getProperty("trusted_ca_cert");
//        username = properties.getProperty("default_device.username");
//        password = properties.getProperty("default_device.password");
        private_key = properties.getProperty("default_device.private_key");

        assert(socket.isConnected());

        // wrap the socket in something that implements SshTransport
        SocketWrapper socketWrapper = new SocketWrapper(socket);

        System.out.println("Starting SSH protocol...");
        try {

            // Create an SshConnector instance
            SshConnector con = SshConnector.createInstance();

            // Make sure we only use SSH2
//            con.setSupportedVersions(SshConnector.SSH2);

            // Config how we'll do host key verification
            HostKeyVerification hkv = new HostKeyVerification() {

                public boolean verifyHost(String hostname, SshPublicKey key) {

                    // ensure it's an X.509 key
                    if (key.getClass().getName() !=
                            "com.maverick.ssh.components.jce.SshX509RsaPublicKey") {
                        System.out.println("Device doesn't have X.509 key...dropping.");
                        return false;
                    }
                    SshX509RsaPublicKey key2 = (SshX509RsaPublicKey)key;
                    X509Certificate device_cert = (X509Certificate)key2.getCertificate();

                    // note: at some point this will switch to support
                    // RFC 6187. Right now J2SSH Maverick only supports
                    // draft-saarenmaa-ssh-x509-00
                    if (key.getAlgorithm() != "x509v3-sign-rsa") {
                        System.out.println("Device doesn't have X.509 key...dropping.");
                        return false;
                    }


                    // now get the trusted CA cert
                    X509Certificate ca_cert = null;
                    InputStream inStream = null;
                    try {
                        inStream = new FileInputStream(trusted_ca_cert);
                        CertificateFactory cf =
                                CertificateFactory.getInstance("X.509");
                        ca_cert = (X509Certificate)
                                cf.generateCertificate(inStream);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    // verify device's cert signed by our trusted CA
                    try {
                        PublicKey pubkey = ca_cert.getPublicKey();
                        device_cert.verify(pubkey);
                    } catch (CertificateException certEx) {
                        System.out.println("Invalid certificate...dropping.");
                        return false;
                    } catch (NoSuchAlgorithmException noAlgEx) {
                        System.out.println("Invalid algorithm...dropping.");
                        return false;
                    } catch (NoSuchProviderException noAlgEx) {
                        System.out.println("Invalid provider...dropping.");
                        return false;
                    } catch (SignatureException sigEx) {
                        System.out.println("Invalid signature...dropping.");
                        return false;
                    } catch (InvalidKeyException kexEx) {
                        System.out.println("Invalid key...dropping.");
                        return false;
                    }
                    // if logic gets here, device cert was signed by trusted CA


                    // ensure device cert has an expected serial number

                    // first extract CN from device cert...
                    String dn = device_cert.getSubjectX500Principal().getName();
                    LdapName ldapDN = null;
                    try {
                        ldapDN = new LdapName(dn);
                    } catch (InvalidNameException nameEx) {
                        System.out.println("Invalid DN name...dropping.");
                        return false;
                    }
                    String  CN_field = null;
                    for(Rdn rdn: ldapDN.getRdns()) {
                        if (rdn.getType().equals("CN")) {
                            CN_field = (String)rdn.getValue();
                            break;
                        }
                    }
                    if (CN_field == null) {
                        System.out.println("Missing CN field...dropping.");
                        return false;
                    }

                    // now check if CN matches an expected serial-number
                    Integer num_devices;
                    try {
                        num_devices = Integer.parseInt(
                                properties.getProperty("num_devices"));
                    } catch(NumberFormatException e) {
                        System.out.print("\n*** ERROR: num_devices specified, "
                                + "please check property file and "
                                + "try again.\n");
                        return false;
                    }
                    for (int i=0; i<num_devices; i++) {
                        String propname = "device." + i + ".serial_number";
                        String sn = properties.getProperty(propname);
                        if (sn.equals(CN_field)) {
                            return true;
                        }
                    }
                    System.out.println("\nUnexpected serial number...dropping");
                    return false;
                }
            };
            Ssh2Context context = (Ssh2Context)con.getContext();//SshConnector.SSH2
            context.setHostKeyVerification(hkv);

            // Connect to the host
            SshClient ssh = con.connect(socketWrapper, username);
            assert(ssh.isConnected()==true);

            // cast to ssh2 client class
            assert (ssh instanceof Ssh2Client);
            Ssh2Client ssh2 = (Ssh2Client)ssh;

            // first try to authenticate using private key
            boolean set_public_key = false;
            String set_public_key_rpc = null;
            if (private_key != null) {
                System.out.println("trying \"" + private_key + "\"");
                PublicKeyAuthentication pk = new PublicKeyAuthentication();
                SshPrivateKeyFile pkfile = SshPrivateKeyFileFactory.parse(
                        new FileInputStream(private_key));
                SshKeyPair pair = pkfile.toKeyPair(null);
                pk.setPrivateKey(pair.getPrivateKey());
                pk.setPublicKey(pair.getPublicKey());
                ssh2.authenticate(pk);
                // set key on network-element, if password auth works...
                if (ssh2.isAuthenticated() == false) {
                    set_public_key = true;
                    BufferedReader br = new BufferedReader(new FileReader(private_key+".pub"));
                    String line = (new BufferedReader(new FileReader(private_key+".pub"))).readLine();
                    String[] tokens = line.split("[ ]");
                    set_public_key_rpc = set_public_key_preamble +
                            tokens[0] + " " + tokens[1] +
                            set_public_key_postamble;
                }
            }

            // next try to authenticate using password
            if(ssh2.isAuthenticated() == false && password != null) {
                System.out.println("trying password \"" + password + "\"");
                PasswordAuthentication pwd = new PasswordAuthentication();
                pwd.setPassword(password);
                ssh2.authenticate(pwd);
            }

            // ensure we're authenticated
            if(ssh2.isAuthenticated() == false) {
                System.out.println("*** Error logging into this device!");
                ssh2.disconnect();
                return;
            }

            // Start "netconf" subsystem
            final Ssh2Session session = (Ssh2Session)ssh2.openSessionChannel();
            session.startSubsystem("netconf");

            Thread t = new Thread() {
                public void run() {
                    try {
                        int read;
                        while ((read = session.getInputStream().read()) > -1) {
                            System.out.write(read);
                            System.out.flush();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            t.start();

            // send <hello>
            System.out.println("\nsending: " + client_hello);
            System.out.flush();
            session.getOutputStream().write(client_hello.getBytes());

            // send <set-public-key>, if needed
            if (set_public_key == true) {
                System.out.println("\nsending: " + set_public_key_rpc);
                System.out.flush();
                session.getOutputStream().write(set_public_key_rpc.getBytes());
            }

            // in a real app, the logic would wait forever for there to
            // be data ready to send or receive but, to keep things simple,
            // we'll just sleep 5 seconds and then disconnect...
            System.out.println("\nsleeping 5 seconds...");
            System.out.flush();
            Thread.sleep(5000);

            // send <close-session>
            System.out.println("\nsending: " + client_goodbye);
            System.out.flush();
            session.getOutputStream().write(client_goodbye.getBytes());
            Thread.sleep(100); // just to make sure its delivered

            // close out SSH session and connection
            session.close();
            ssh2.disconnect();

        } catch(Throwable t) {
            System.out.println("\ncatch-all stacktrace catcher:");
            t.printStackTrace();
        }
    }
}


public class CallhomeExample {
    public static void main(String[] args) {
        String  file;
        Integer port=4334;


        // determine which prop file to read from command line
//        file = System.getProperty("file");
//        if (file == null) {
//            System.out.println("\n*** ERROR: no config file specified, " +
//                    "please try again.\n");
//            return;
//        }

        // parse the prop file
        Properties properties = new Properties();
//        try {
//            properties.load(new FileInputStream(file));
//        } catch (IOException e) {
//            System.out.println("\n*** ERROR: invalid property file " +
//                    "specified, please try again.\n");
//            return;
//        }
//
//        try {
//            port = Integer.parseInt(properties.getProperty("server.port"));
//        } catch(NumberFormatException e) {
//            System.out.print("\n*** ERROR: invalid port specified, " +
//                    "please check property file and try again.\n");
//            return;
//        }


        // start a server socket listening on port
        System.out.println("listening on port " + port + "...");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch(Exception ex) {
            System.out.println("ServerSocket() failed: " + ex);
        }


        while (true) {

            try {
                System.out.println("\n\nWaiting to accept connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Accepted connection from: " +
                        socket.toString());
                DeviceHandler deviceHandler = new DeviceHandler(socket,
                        properties);
                Thread thread = new Thread(deviceHandler);
                thread.start();
            } catch(Exception ex) {
                System.out.println("accept() failed: " + ex);
                System.exit(-1);
            }

        }
    }
}
