import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class StorageNode {

	private Socket socket;
	private CloudByte[] b;
	private BufferedReader in;
	private PrintWriter out;
	private String id;
	private String portcli;
	private String portnode;
	private String file;
	private ServerSocket ss;
	private ArrayList<CloudByte> list;
	private final int Size_array = 10000;
	private List<ByteBlockRequest> lista;
	private List<String> lista_nodes = new ArrayList<>();

	public StorageNode(String id, String portcli, String portnode, String file) {
		b = new CloudByte[1000000];

		this.id = id;
		this.portcli = portcli;
		this.portnode = portnode;
		this.file = file;

		acdDiretorio();

		CarregarData(file);
		DetetaError e = new DetetaError();
		e.start();
		ErrorThread t = new ErrorThread();
		t.start();
		Clienteserver();
//		
	}

	CloudByte[] GetData() {

		return this.b;

	}

	public StorageNode(String id, String portcli, String portnode) {

		this.b = new CloudByte[1000000];
		this.id = id;
		this.portcli = portcli;
		this.portnode = portnode;
		acdDiretorio();
		startDownload();
		DetetaError e = new DetetaError();
		ErrorThread t = new ErrorThread();
		t.start();
		e.start();
		Clienteserver();
	}

	public class Download extends Thread {

		private Socket socket;
		private String id;
		private String portcli;

		public Download(String id, String portcli) {
			this.id = id;
			this.portcli = portcli;
		}

		@Override
		public void run() {
			try {
				socket = new Socket(InetAddress.getByName(id), Integer.parseInt(portcli));
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				System.out.println("download iniciado " + id + " " + portcli);
				ByteBlockRequest t = null;
				int cont = 0;
				while ((t = remove()) != null) {

					out.writeObject(t);

					CloudByte[] cb = (CloudByte[]) in.readObject();

					addBlock(cb, t.Getstartindex());

					cont++;

				}
				System.out.println(" Download terminado: " + id + " " + portcli + " " + cont + " " + "blocos");
			} catch (IOException | ClassNotFoundException e) {

			} catch (InterruptedException e) {

				return;
			} // TODO Auto-generated method stub

		}

	}

	void startDownload() {
		Fill_List();

		for (String n : consultadeNodes()) {

			String[] strs = n.split(" ");
			String ip = strs[0];
			String port = strs[1];
			new Download(ip, port).start();

		}

		synchronized (this) {
			while (!lista.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		System.out.println("download concluido");
	}

	void Fill_List() {
		lista = new ArrayList<>();
		int count = 0;
		ByteBlockRequest r = new ByteBlockRequest(count, 100);
		for (int i = 0; i < 10000; i++) {
			lista.add(r);
			count = count + 100;

		}
		System.out.println(lista.size());

	}

	void inscricao() {

		this.out.println("INSC " + id + " " + portnode);
		System.out.println("Sending to directory " + "INSC " + id + " " + portnode);
	}

	List<String> consultadeNodes() {
		List<String> lista_nodes = new ArrayList<>();
		this.out.println("nodes");
		String a = "   ";
		String r = null;
		while (!a.equalsIgnoreCase("end")) {
			try {
				a = this.in.readLine();
				if (!a.equalsIgnoreCase("end")) {
					String[] strs = a.split(" ");
					if (strs[0].contains("node")) {
						r = strs[1].concat(" " + strs[2]);
						lista_nodes.add(r);
						System.out.println(r);

						if (r.contains(this.portnode)) {
							lista_nodes.remove(r);

						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return lista_nodes;
	}

	public synchronized void addBlock(CloudByte[] cb, int startindex) throws InterruptedException {

		for (int i = 0; i < cb.length; i++) {

			b[i + startindex] = cb[i];

		}
		notifyAll();
	}

	public synchronized ByteBlockRequest remove() throws InterruptedException {
		if (lista.isEmpty()) {
			return null;
		} else {

			ByteBlockRequest e = lista.get(0);
			lista.remove(0);

			return e;
		}

	}

	void Clienteserver() {
		try {
			this.ss = new ServerSocket(Integer.parseInt(portnode));
			System.out.println("Accepting connections");

			while (true) {
				Socket socket = ss.accept();
				DealWithClient d = new DealWithClient(socket);
				d.start();

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class DealWithClient extends Thread {

		private Socket socket;

		public DealWithClient(Socket s) {
			this.socket = s;

		}

		@Override
		public void run() {
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				while (true) {
					ByteBlockRequest bb = (ByteBlockRequest) in.readObject();
					CloudByte[] r = new CloudByte[bb.length];
					for (int i = bb.startIndex, x = 0; i < bb.startIndex + bb.length; i++, x++) {

						r[x] = b[i];

						if (r[x].isParityOk() == false) {

							System.out.println("Data querie: ERROR was  detected at" + r[x].toString());

							CorrectsError(x);
						}
					}

					out.writeObject(r);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void acdDiretorio() {
		try {
			this.socket = new Socket(InetAddress.getByName(null), Integer.parseInt(portcli));
			this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			inscricao();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	String Getid() {
		return this.id;
	}

	String Getportnode() {

		return this.portnode;
	}

	String Getportcli() {

		return this.portcli;
	}

	CloudByte[] CarregarData(String file) {
		byte[] r = new byte[0];
		try {
			r = Files.readAllBytes(new File(file).toPath());
		} catch (IOException e) {

			e.printStackTrace();
		}
		for (int i = 0; i < 1000000; i++) {

			this.b[i] = new CloudByte(r[i]);

		}

		System.out.println("Dados Carregados");

		return b;
	}

	void CorrectsError(int index) {



		Countdownlatch cdl = new Countdownlatch(2);
		for (String n : consultadeNodes()) {

			String[] strs = n.split(" ");
			String ip = strs[0];
			String port = strs[1];
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {

						Socket socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
						ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
						ByteBlockRequest bbr = new ByteBlockRequest(index, 1);

						out.writeObject(bbr);

						CloudByte[] d = (CloudByte[]) in.readObject();
						cdl.Countdown(d[0]);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

		}
		b[index] = cdl.await();
		System.out.println("ERROR corrected:" + " " + index + " " + b[index].toString() + b[index].isParityOk());
	}

	private class DetetaError extends Thread {
		@Override
		public void run() {
			while (true) {
				for (int i = 0; i < b.length; i++) {
					if (b[i].isParityOk() == false) {

						System.out.println("Data querie: ERROR was  detected at" + b[i].toString());

						CorrectsError(i);

					}

				}

			}
		}

	}

	private class ErrorThread extends Thread {
		@Override
		public void run() {
			

		

			Scanner br = new Scanner(System.in);
			while (true) {
				String s = br.nextLine();
				String[] strs = s.split(" ");

				if (s.contains("ERROR")) {
					int i = Integer.parseInt(strs[1]);
					b[i].makeByteCorrupt();

					System.out.println(
							"ERROR injected Cloudbyte: " + b[i].getValue() + " " + "Parity" + " " + b[i].isParityOk());

				}

			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			StorageNode n = new StorageNode(args[0], args[1], args[2], args[3]);
		} else {

			StorageNode n = new StorageNode(args[0], args[1], args[2]);
		}
	}
}