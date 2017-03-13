/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.practica3;

import ast.protocols.tcp.TCPSegment;

/**
 *
 * @author xavier
 */
public interface Channel {

    /**
     * Send the passed segment over the channel.
     * @param seg segment to send.
     */
    public void send(TCPSegment seg);

    /**
     * Receive a segment from the channel.
     * @return the received segment.
     */
    public TCPSegment receive();

    /**
     * Returns the maximum message length to avoid
     * fragmentation.
     * @return the maximum message size.
     */
    public int getMMS();

}
