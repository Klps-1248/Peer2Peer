import java.io.*;
import java.net.Socket;
import java.util.*;

public class DataController implements Runnable {
    private static String ID_Peer = null;
    RandomAccessFile randomAccessFile;

    public DataController(String ID_Peer) {
        DataController.ID_Peer = ID_Peer;
    }

    public void run() {
        String dataType;
        DataParameters dp;
        MessageInfo msg;
        String currentID_peer;

        while (true) {
            dp = Peer2Peer.deleteQueueData();
            while (dp == null) {
                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (Exception exn) {
                    exn.printStackTrace();
                }
                dp = Peer2Peer.deleteQueueData();
            }

            msg = dp.getMessage();

            dataType = msg.fetchdata_Type();
            currentID_peer = dp.getpeerID();
            int position = Peer2Peer.hm_peerData.get(currentID_peer).position;
            if (dataType.equals("" + Constants.have) && position != 14) {
                Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " Peer sent HAVE msg to peer " + currentID_peer);
                if (dividePayLoadData(currentID_peer, msg)) {
                    sendInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                    Peer2Peer.hm_peerData.get(currentID_peer).position = 9;
                } else {
                    sendNotInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                    Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                }
            } else {
                switch (position) {
                    case 2:
                        if (dataType.equals("" + Constants.bitField)) {
                            Peer2Peer.logger
                                    .logDisplay(
                                            Peer2Peer.ID_peer + " Peer sent PIECE " + currentID_peer);
                            sendBitFieldMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 3;
                        }
                        break;

                    case 3:

                        if (dataType.equals("" + Constants.Interested)) {
                            Peer2Peer.logger
                                    .logDisplay(
                                            Peer2Peer.ID_peer + " received REQUEST message to Peer " + currentID_peer);
                            Peer2Peer.logger
                                    .logDisplay(
                                            Peer2Peer.ID_peer + " Peer sent INTERESTED to " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).isInterested_Peer = 1;
                            Peer2Peer.hm_peerData.get(currentID_peer).isHandShake = 1;

                            if (!Peer2Peer.prefNeighbours_HM.containsKey(currentID_peer)
                                    && !Peer2Peer.unchokedNeighbours_HM.containsKey(currentID_peer)) {
                                sendChokeMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).peer_isChoked = 1;
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 6;
                            } else {
                                Peer2Peer.hm_peerData.get(currentID_peer).peer_isChoked = 0;
                                sendUnChokeMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 4;
                            }
                        } else if (dataType.equals("" + Constants.notInterested)) {
                            Peer2Peer.logger.logDisplay(
                                    Peer2Peer.ID_peer + " Peer sent NOT INTERESTED message to " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).isInterested_Peer = 0;
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 5;
                            Peer2Peer.hm_peerData.get(currentID_peer).isHandShake = 1;
                        }
                        break;

                    case 4:
                        if (dataType.equals("" + Constants.request)) {
                            dataTransmission(Peer2Peer.pD.get(currentID_peer), msg, currentID_peer);
                            if (!Peer2Peer.prefNeighbours_HM.containsKey(currentID_peer)
                                    && !Peer2Peer.unchokedNeighbours_HM.containsKey(currentID_peer)) {
                                sendChokeMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).peer_isChoked = 1;
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 6;
                            }
                        }
                        break;

                    case 8:
                        if (dataType.equals("" + Constants.bitField)) {
                            if (dividePayLoadData(currentID_peer, msg)) {
                                sendInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 9;
                            } else {
                                sendNotInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                            }
                        }
                        break;

                    case 9:
                        if (dataType.equals("" + Constants.choke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 14;
                        } else if (dataType.equals("" + Constants.unChoke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got UNCHOKED by Peer " + currentID_peer);
                            int initialConflict = Peer2Peer.payLoadCurrent.get_firstBitField(
                                    Peer2Peer.hm_peerData.get(currentID_peer).payloadData);
                            if (initialConflict != -1) {
                                sendRequest(initialConflict, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 11;
                                Peer2Peer.hm_peerData.get(currentID_peer).time1 = new Date();
                            } else
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                        }
                        break;

                    case 11:
                        if (dataType.equals("" + Constants.choke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 14;
                        }

                        else if (dataType.equals("" + Constants.bit)) {
                            byte[] payLoad_Array = msg.array_getPayLoad();
                            Peer2Peer.hm_peerData.get(currentID_peer).time2 = new Date();
                            long d = Peer2Peer.hm_peerData.get(currentID_peer).time2.getTime()
                                    - Peer2Peer.hm_peerData.get(currentID_peer).time1.getTime();
                            Peer2Peer.hm_peerData
                                    .get(currentID_peer).rateOfStream = ((double) (payLoad_Array.length
                                            + Constants.messageSize + Constants.messageType) / (double) d) * 100;
                            Payloadbit p = Payloadbit.convertTobit(payLoad_Array);
                            Peer2Peer.payLoadCurrent.refresh_payLoad(p, "" + currentID_peer);
                            int idx = Peer2Peer.payLoadCurrent.get_firstBitField(
                                    Peer2Peer.hm_peerData.get(currentID_peer).payloadData);
                            if (idx != -1) {
                                sendRequest(idx, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 11;
                                Peer2Peer.hm_peerData.get(currentID_peer).time1 = new Date();
                            } else
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                            Peer2Peer.readAdjacentPeerData();
                            ;

                            Enumeration<String> keys = Collections
                                    .enumeration(Peer2Peer.hm_peerData.keySet());
                            while (keys.hasMoreElements()) {
                                String nextElement = keys.nextElement();
                                RemotePeerData r = Peer2Peer.hm_peerData.get(nextElement);
                                if (nextElement.equals(Peer2Peer.ID_peer))
                                    continue;
                                if (r.isFinished == 0 && r.peer_isChoked == 0 && r.isHandShake == 1) {
                                    sendHaveMessage(nextElement, Peer2Peer.pD.get(nextElement));
                                    Peer2Peer.hm_peerData.get(nextElement).position = 3;
                                }
                            }
                        }

                        break;

                    case 14:
                        if (dataType.equals("" + Constants.unChoke)) {
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got CHOKED by Peer " + currentID_peer);
                            try {
                                Thread.sleep(6000);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                ;
                            }
                            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " got UNCHOKED by Peer " + currentID_peer);
                            Peer2Peer.hm_peerData.get(currentID_peer).position = 14;
                        } else if (dataType.equals("" + Constants.have)) {
                            if (dividePayLoadData(currentID_peer, msg)) {
                                sendInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 9;
                            } else {
                                sendNotInterestedMessage(currentID_peer, Peer2Peer.pD.get(currentID_peer));
                                Peer2Peer.hm_peerData.get(currentID_peer).position = 13;
                            }
                        }

                        break;
                }
            }

        }
    }

    private void dataTransmission(Socket socket, MessageInfo requestMessage, String pId) {
        File f = new File(Peer2Peer.ID_peer, Constants.fileDesc);
        byte[] bidx = requestMessage.array_getPayLoad();
        int pidx = Constants.byteToIntConverter(bidx, 0);
        byte[] byte_Read = new byte[Constants.sizeOfbit];
        int readBytes = 0;

        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sending bit " + pidx + " to Peer " + pId);
        try {
            randomAccessFile = new RandomAccessFile(f, "r");
            randomAccessFile.seek((long) pidx * Constants.sizeOfbit);
            readBytes = randomAccessFile.read(byte_Read, 0, Constants.sizeOfbit);
        } catch (Exception ex) {
            Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " has error in reading the file: " + ex.toString());
        }

        byte[] buffer_Bytes = new byte[readBytes + Constants.maxbitLength];
        System.arraycopy(bidx, 0, buffer_Bytes, 0, Constants.maxbitLength);
        System.arraycopy(byte_Read, 0, buffer_Bytes, Constants.maxbitLength, readBytes);

        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.bit, buffer_Bytes)), socket);
        try {
            randomAccessFile.close();
        } catch (Exception ignored) {
        }
    }

    private void sendRequest(int pNo, Socket socket) {
        byte[] bitArray = new byte[Constants.maxbitLength];
        int i = 0;
        while (i < Constants.maxbitLength) {
            bitArray[i] = 0;
            i++;
        }

        byte[] pidxArray = Constants.intToByteConverter(pNo);
        System.arraycopy(pidxArray, 0, bitArray, 0,
                pidxArray.length);

        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.request, bitArray)), socket);
    }

    private void sendInterestedMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent REQUEST message to Peer " + pId);
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent INTERESTED message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.Interested)), socket);
    }

    private void sendNotInterestedMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent NOT INTERESTED message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.notInterested)), socket);
    }

    private boolean dividePayLoadData(String pId, MessageInfo md) {
        PayLoadData payload = PayLoadData.data_Decode(md.array_getPayLoad());
        Peer2Peer.hm_peerData.get(pId).payloadData = payload;
        return Peer2Peer.payLoadCurrent.dividePayLoadData(payload);
    }

    private void sendChokeMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent CHOKE message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.choke)), socket);
    }

    private void sendUnChokeMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent UNCHOKE message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(new MessageInfo(Constants.unChoke)), socket);
    }

    private void sendOutput(byte[] encodedBitField, Socket socket) {
        try {
            OutputStream op = socket.getOutputStream();
            op.write(encodedBitField);
        } catch (Exception exn) {
            System.out.println(exn.getMessage());
            ;
        }
    }

    private void sendBitFieldMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent PIECE message to Peer " + pId);
        sendOutput(MessageInfo.array_DataToByte(
                new MessageInfo(+Constants.bitField, Peer2Peer.payLoadCurrent.data_Encode())), socket);
    }

    private void sendHaveMessage(String pId, Socket socket) {
        Peer2Peer.logger.logDisplay(Peer2Peer.ID_peer + " sent HAVE message to Peer ID: " + pId);
        sendOutput(MessageInfo.array_DataToByte(
                new MessageInfo(Constants.have, Peer2Peer.payLoadCurrent.data_Encode())), socket);
    }

}