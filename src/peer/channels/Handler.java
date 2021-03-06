package peer.channels;

import java.net.DatagramPacket;

import chunk.Chunk;
import disk.ChunkManagement;
import peer.Peer;
import peer.protocols.backup.Store;
import peer.protocols.reclaim.Reclaim;
import peer.protocols.reclaim.Removed;
import peer.protocols.restore.GetChunk;
import message.*;

public class Handler implements Runnable {

    Peer peer;
    DatagramPacket packet;
    Message msg;
    MessageHeader msgHeader;

    public Handler(Peer peer, DatagramPacket packet) {
        this.peer = peer;
        this.packet = packet;
    }

    public void parsePacket() {
        int packetLength = packet.getLength();
        byte[] rawData = new byte[packetLength];
        byte[] packetData = this.packet.getData();
        System.arraycopy(packetData, packet.getOffset(), rawData, 0, packetLength);
        this.msg = new Message(rawData);
        this.msgHeader = msg.getHeader();
    }

    @Override
    public void run() {

        parsePacket();

        if (this.peer.getPeerId() != this.msgHeader.getSenderId())
            switch (this.msgHeader.getMessageType()) {
            case "PUTCHUNK":
                handlePUTCHUNK();
                break;
            case "GETCHUNK":
                handleGETCHUNK();
                break;
            case "CHUNK":
                handleCHUNK();
                break;
            case "DELETE":
                handleDELETE();
                break;
            case "REMOVED":
                handleREMOVED();
                break;
            default:
                break;
            }
    }

    private void handleREMOVED() {
        String fileId = this.msgHeader.getFileId();
        int chunkNo = this.msgHeader.getChunkNo();
        ChunkManagement.getInstance().registerRemoved(fileId, chunkNo);

        Thread t = new Thread(new Removed(peer, fileId, chunkNo));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handlePUTCHUNK() {
        if (this.peer.getDisk().getFreeSpace() < (packet.getLength())) {
            System.out.println("Peer " + this.peer.getPeerId() + "- PUTCHUNK request: Not enough space to store chunk");
            return;
        }

        Chunk chunk = new Chunk(this.msgHeader.getFileId(), this.msgHeader.getChunkNo(), this.msgHeader.getReplicaDeg(),
                this.msg.getBody());

        Thread t = new Thread(new Store(peer, chunk));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void handleGETCHUNK() {
        String fileId = this.msgHeader.getFileId();
        int chunkNo = this.msgHeader.getChunkNo();
        Thread t = new Thread(new GetChunk(peer, fileId, chunkNo));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleCHUNK() {
        Chunk chunk = new Chunk(this.msgHeader.getFileId(), this.msgHeader.getChunkNo(), this.msgHeader.getReplicaDeg(),
                this.msg.getBody());

        ChunkManagement.getInstance().addRestoreChunk(chunk);
    }

    public void handleDELETE() {
        String fileID = msgHeader.getFileId();
        this.peer.deleteFile(fileID);
        ChunkManagement.getInstance().deleteStores(fileID);
    }
}