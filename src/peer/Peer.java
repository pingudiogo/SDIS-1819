package peer;

import disk.*;

public class Peer implements PeerInterface{
	private int peerId;
	private Disk disk;

	private String pVersion;
	private String acessPoint;

	

	public Peer(String pVersion, int sid, String accessPoint, String mcAddress, int mcPort, String mdbAddress,
			int mdbPort, String mdrAddress, int mdrPort) {
		this.peerId = sid;
		disk = new Disk("peer" + peerId);
		this.pVersion = pVersion;

	}

	public static void main(String[] args) {
		int id = Integer.parseInt(args[1]);
		int mcPort = Integer.parseInt(args[4]);
		int mdbPort = Integer.parseInt(args[6]);
		int mdrPort = Integer.parseInt(args[8]);
		Peer peer = new Peer(args[0], id, args[2], args[3], mcPort, args[5], mdbPort, args[7], mdrPort);
	}


	public void backup(String path, int ReplicationDeg) {

	}

	public void delete(String path) {

	}

	public void restore(String path) {

	}

	public void reclaim(float space) {

	}

	public String state() {
		return "test";
	}
}