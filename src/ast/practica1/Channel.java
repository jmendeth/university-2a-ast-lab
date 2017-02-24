/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.practica1;

import ast.protocols.tcp.TCPSegment;

/**
 *
 * @author xavier
 */
public interface Channel {
    public void send(TCPSegment seg);
    public TCPSegment receive();
}
