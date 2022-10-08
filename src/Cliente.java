import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


public class Cliente {
	private ByteBlockRequest bb;
	private JFrame frame;
	private CloudByte[] b;
	private String id;
	private String portcli;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private ServerSocket ss;

	public Cliente(String id, String portcli) {
		frame = new JFrame("lable");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(600, 145);
		addFrameContent();
		frame.pack();
		b = new CloudByte[1000000];
		this.id = id;
		this.portcli = portcli;

		this.open();
		try {
			socket = new Socket(InetAddress.getByName(null), Integer.parseInt(portcli));
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void open() {
		frame.setVisible(true);
	}

	private void addFrameContent() {
		frame.setLayout(new GridLayout(2, 1));

		Container x = new Container();
		x.setLayout(new FlowLayout());

		JLabel title = new JLabel("Cliente");
		;

		JLabel label = new JLabel("Posição a consultar");
		JTextField Posicao = new JTextField();
		Posicao.setColumns(15);

		JLabel label2 = new JLabel("Comprimento");
		JTextField Comprimento = new JTextField();
		Comprimento.setColumns(15);

		JTextArea Respostas = new JTextArea("Resposta...");
		Respostas.setEditable(false);

		JButton button = new JButton("Consultar");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ByteBlockRequest d = new ByteBlockRequest(Integer.parseInt(Posicao.getText()),
						Integer.parseInt(Comprimento.getText()));

				try {
					out.writeObject(d);
					b = (CloudByte[]) in.readObject();
					System.out.println(b[0]);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < b.length; i++) {

						if (b[i].isParityOk() == false) {
							System.out.println("Data querie. ERROR was  detected at" + b[i].toString());
						}

						sb.append(" " + b[i].toString());

					}
					Respostas.setText(sb.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

		x.add(label);
		x.add(Posicao);
		x.add(label2);
		x.add(Comprimento);
		x.add(button);
		frame.add(x);
		frame.add(Respostas);

	}

	String Getportcli() {
		return this.portcli;

	}

	public static void main(String[] args) {
		Cliente a = new Cliente(args[0], args[1]);

	}

}