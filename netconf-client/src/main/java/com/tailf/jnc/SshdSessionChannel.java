package com.tailf.jnc;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Session;
import com.google.common.collect.ImmutableSet;
import com.tailf.jnc.*;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.session.ClientSession;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;

public class SshdSessionChannel implements Transport {
    private BufferedReader in = null;
    private PrintWriter out = null;
    private final ArrayList<IOSubscriber> ioSubscribers;

    protected long readTimeout = 0; // millisecs

    private static final String endmarker = "]]>]]>";
    private static final int end = endmarker.length() - 1;
    ClientChannel channel;
    ClientSession session;
 

    public SshdSessionChannel(ClientSession session, ClientChannel channel) {
        this.channel = channel;
        this.session = session;
        channel.getInvertedErr();
        final InputStream is = channel.getInvertedOut();
        final OutputStream os = channel.getInvertedIn();
        in = new BufferedReader(new InputStreamReader(is));
        out = new PrintWriter(os, false);
        ioSubscribers = new ArrayList<IOSubscriber>();
    }

    @Override
    public boolean ready() throws IOException {
        if (in.ready()) {
            return true;
        }

        return !session.waitFor(ImmutableSet.of(ClientSession.ClientSessionEvent.TIMEOUT), (1)).contains(ClientSession.ClientSessionEvent.TIMEOUT);
    }

    /**
     * given a live SSHSession, check if the server side has closed it's end of
     * the ssh socket
     */
    public boolean serverSideClosed() throws IOException {
        int conditionSet = ChannelCondition.TIMEOUT & ChannelCondition.CLOSED
                & ChannelCondition.EOF;

        return !session.waitFor(ImmutableSet.of(ClientSession.ClientSessionEvent.TIMEOUT, ClientSession.ClientSessionEvent.CLOSED), (1)).contains(ClientSession.ClientSessionEvent.TIMEOUT);
    }

    /**
     * If we have readTimeout set, and an outstanding operation was timed out -
     * the socket may still be alive. However since we timed out our read
     * operation and subsequently didn't process the xml data - there may be
     * parts of unprocessed xml data left on the socket. This function reads
     * and throws away all such unprocessed data. An alternative after timeout
     * is of course to close the socket and reconnect.
     *
     * @return number of discarded characters
     */
    public int readUntilWouldBlock() {
        int ret = 0;
        while (true) {
            try {
                if (!(ready())) {
                    return ret;
                }
                in.read();
                ret++;
            } catch (final IOException e) {
                return ret;
            }
        }
    }

    /**
     * Reads in "one" reply from the SSH transport input stream. A
     * <em>]]&gt;]]&gt;</em> character sequence is used to separate multiple
     * replies as described in <a target="_top"
     * href="ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt">RFC 4742</a>.
     */
    @Override
    public StringBuffer readOne() throws IOException, JNCException {
        final StringWriter wr = new StringWriter();
        int ch;
        while (true) {
            if ((readTimeout > 0) && !in.ready()) { // else we want to block
                if (session.waitFor(ImmutableSet.of(ClientSession.ClientSessionEvent.TIMEOUT), (readTimeout)).contains(ClientSession.ClientSessionEvent.TIMEOUT)) {
                    // it's a timeout - there is nothing to
                    // read, not even eof
                    throw new JNCException(JNCException.TIMEOUT_ERROR,
                            Long.valueOf(readTimeout));
                }
            }

            // If readTimeout /= 0 we're guaranteed to not block
            // If its == 0, we want to block

            ch = in.read();
            if (ch == -1) {
//                trace("end of input (-1)");
                throw new SessionClosedException("Session closed");
            }

            for (int i = 0; i < endmarker.length(); i++) {
                if (ch == endmarker.charAt(i)) {
                    if (i < end) {
                        ch = in.read();
                    } else {
                        for (final IOSubscriber sub : ioSubscribers) {
                            sub.inputFlush(endmarker.substring(0, end));
                        }
                        return wr.getBuffer();
                    }
                } else {
                    subInputChar(wr, endmarker.substring(0, i));
                    subInputChar(wr, ch);
                    break;
                }
            }
        }
    }

    private void subInputChar(StringWriter wr, int ch) {
        wr.write(ch);
        for (int i = 0; i < ioSubscribers.size(); i++) {
            final IOSubscriber sub = ioSubscribers.get(i);
            sub.inputChar(ch);
        }
    }

    private void subInputChar(StringWriter wr, String s) {
        for (int i = 0; i < s.length(); i++) {
            subInputChar(wr, s.charAt(i));
        }
    }

    /**
     * Prints an integer (as text) to the output stream.
     *
     * @param iVal Text to send to the stream.
     */
    @Override
    public void print(long iVal) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrint(iVal);
        }
        out.print(iVal);
    }

    /**
     * Prints text to the output stream.
     *
     * @param s Text to send to the stream.
     */
    @Override
    public void print(String s) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrint(s);
        }
        out.print(s);
    }

    /**
     * Prints an integer (as text) to the output stream. A newline char is
     * appended to end of the output stream.
     *
     * @param iVal Text to send to the stream.
     */
    @Override
    public void println(int iVal) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrintln(iVal);
        }
        out.println(iVal);
    }

    /**
     * Print text to the output stream. A newline char is appended to end of
     * the output stream.
     *
     * @param s Text to send to the stream.
     */
    @Override
    public void println(String s) {
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputPrintln(s);
        }
        out.println(s);
    }

    /**
     * Add an IO Subscriber for this transport. This is useful for tracing the
     * messages.
     *
     * @param s An IOSUbscriber that will be called whenever there is something
     *          received or sent on this transport.
     */
    public void addSubscriber(IOSubscriber s) {
        ioSubscribers.add(s);
    }

    /**
     * Removes an IO subscriber.
     *
     * @param s The IO subscriber to remove.
     */
    public void delSubscriber(IOSubscriber s) {
        for (int i = 0; i < ioSubscribers.size(); i++) {
            final IOSubscriber x = ioSubscribers.get(i);
            if (s.equals(x)) {
                ioSubscribers.remove(i);
                return;
            }
        }
    }

    /**
     * Signals that the final chunk of data has be printed to the output
     * transport stream. This method furthermore flushes the transport output
     * stream buffer.
     * <p>
     * A <em>]]&gt;]]&gt;</em> character sequence is added, as described in <a
     * target="_top" href="ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt">RFC
     * 4742</a>, to signal that the last part of the reply has been sent.
     */
    @Override
    public void flush() {
        out.print(endmarker);
        out.flush();
        for (final IOSubscriber sub : ioSubscribers) {
            sub.outputFlush(endmarker);
        }
    }


    /**
     * Closes the SSH channnel
     */
    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
