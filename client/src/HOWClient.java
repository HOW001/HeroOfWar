import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class HOWClient implements Runnable{
	private static final int REGISTER_USER = 0X010001;// �����û�,ʧ�ܷ�����ʾ,�ɹ����ط������б�
	private static final int LOGIN_USER = 0X010002;// �û���¼,ʧ�ܷ�����ʾ,�ɹ����ط������б�
	private static final int SERVER_LIST = 0X010003;// �������б�,�ͻ��˲�����������
	private static final int ROLE_LOGIN_SERVER = 0X010004;// ��ɫ��¼��Ϸ
	private static final int CREATE_ROLE = 0x010005;// ��ɫ����
	private static final int PLAYER_TICK = 0x020001;// ���������Ϣ
	private static final int RECEIVE_UI_INFO = 0x0C0001;// ������ʾ��Ϣ

	private static java.util.concurrent.atomic.AtomicInteger index = new AtomicInteger();;
	static long use = System.currentTimeMillis();

	private String userName = null;
	private String pwd = null;
	private String roleName = null;
	private int gender = 1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		use = System.currentTimeMillis();

		new HOWClient().createLink();

	}

	private OutputStream out;
	private InputStream in;
	private Socket socket = null;

	/**
	 * @author liuzhigang ��ʼ��������
	 */
	public void createLink() {

		try {
			System.out.println("��ʼ��������");
			socket = new Socket("127.0.0.1", 60000);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			byte[] sendData=send(REGISTER_USER);
			flush(sendData);
			long lastTickTime = System.currentTimeMillis();
			int length = 0;
			while (true) {
				length = in.available();
				if (length == 0) {
					if (System.currentTimeMillis() - lastTickTime >= 10000) {
						lastTickTime = System.currentTimeMillis();
						sendData=send(PLAYER_TICK);
						flush(sendData);
						System.out.println(userName+":�����������ݳ���");
//						out.flush();
					}
					Thread.sleep(1000);
					continue;
				}
				byte[] data = new byte[length];
				in.read(data);
				receive(data);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendTick(DataOutputStream dos) {
		try {
			dos.writeInt(PLAYER_TICK);
			dos.writeInt(index.incrementAndGet());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void flush(byte[] sendData){
    	try {
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(sendData.length + 4);
			dos.write(sendData);
			System.out.println("�������ݳ���:" + sendData.length);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
	/**
	 * @author liuzhigang
	 * @return ������Ϣ
	 */
	public byte[] send(int cmd) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(cmd);
			switch (cmd) {
			case REGISTER_USER:
				sendRegisterUser(dos);
				break;
			case LOGIN_USER:
				sendLoginUser(dos);
				break;
			case ROLE_LOGIN_SERVER:
				sendRoleLogin(dos);
				break;
			case PLAYER_TICK:
				sendTick(dos);
				break;
			case CREATE_ROLE:
				sendCreateRole(dos);
				break;
			default:
				return null;
			}
			byte[] sendData = baos.toByteArray();
			return sendData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * @author liuzhigang
	 * @param dos
	 *            ������ɫ
	 */
	public void sendCreateRole(DataOutputStream dos) {
		try {
			dos.writeUTF(roleName);
			dos.writeByte(gender);
			System.out.println("������ɫ:roleName=" + roleName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author liuzhigang
	 * @param dos
	 *            ��ɫ��¼
	 */
	public void sendRoleLogin(DataOutputStream dos) {
		try {
			System.out.println(userName + "���͵�¼��Ϸ����!");
			dos.writeUTF(userName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author liuzhigang
	 * @param dos
	 *            �û���¼
	 */
	public void sendLoginUser(DataOutputStream dos) {
		try {
			index.getAndIncrement();
			dos.writeUTF("how2999");
			dos.writeUTF("how2999");
			userName="how2999";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author liuzhigang
	 * @return ����ע������
	 */
	public void sendRegisterUser(DataOutputStream dos) {
		try {
			index.getAndIncrement();
			 dos.writeUTF("liuzg0008" + index);
			// dos.writeUTF("123456");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author liuzhigang
	 * @param data
	 *            ������Ϣ
	 */
	public void receive(byte[] data) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			// int avlid=dis.readInt();
			int postion = 0;
			for (; postion < data.length;) {
				int realLen = dis.readInt();
				postion += realLen;
				byte[] bytes = new byte[realLen - 4];
				dis.read(bytes);
				ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes);
				DataInputStream dis2 = new DataInputStream(bais2);
				int cmd = dis2.readInt();
				switch (cmd) {
				case REGISTER_USER:
					receiveRegisterUser(dis2);
					break;
				case LOGIN_USER:
					receiveLoginUser(dis2);
					break;
				case SERVER_LIST:
					receiveServerList(dis2);
					break;
				case RECEIVE_UI_INFO:
					receiveUIInfo(dis2);
					break;
				case CREATE_ROLE:
					receiveCreateRole(dis2);
					break;
				default:
					System.out
							.println("ǰ���յ�δ֪��Ϣ:0x" + Integer.toHexString(cmd));
				}
				System.out.println("�յ���¼������Ϣ:realLen=" + realLen + ",cmd=0x"
						+ Integer.toHexString(cmd));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author liuzhigang
	 * @param dis
	 *            �յ��½���ɫ����
	 */
	public void receiveCreateRole(DataInputStream dis) {
		try {
			roleName = dis.readUTF();
			gender = dis.readByte();
			System.out.println("�յ��ɴ����Ľ�ɫ��:" + roleName);
			byte[] sendData=send(CREATE_ROLE);
			flush(sendData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author liuzhigang
	 * @param dis
	 *            ����UI��Ϣ
	 */
	public void receiveUIInfo(DataInputStream dis) {
		try {
			int type = dis.readShort();// ��������
			String desc = dis.readUTF();// ��������
			String left = dis.readUTF();// �����Ϣ
			String right = dis.readUTF();// �Ҽ���Ϣ
			int leftCommand = dis.readInt();// �������
			int leftParam = dis.readShort();// ��������������
			System.out.println("type=" + type + ",desc=" + desc + ",left="
					+ left + ",right=" + right + ",leftCommand="
					+ Integer.toHexString(leftCommand) + ",leftParam="
					+ leftParam);
			for (int index = 1; index <= leftParam; index++) {
				String leftValue = dis.readUTF();
				System.out.println("value" + index + "=" + leftValue);
			}
			int rightCommand = dis.readInt();// �Ҽ�����
			int rightParam = dis.readShort();// �Ҽ������������
			System.out.print("rightCommand="
					+ Integer.toHexString(rightCommand) + ",rightParam="
					+ rightParam);
			for (int index = 1; index <= rightParam; index++) {
				String rightValue = dis.readUTF();
				System.out.println("value" + index + "=" + rightValue);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void receiveLoginUser(DataInputStream dis) {
		try {
			boolean isSuccess = dis.readBoolean();
			String desc = dis.readUTF();
			System.out.print("�û���¼���:" + isSuccess + ",desc=" + desc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void receiveRegisterUser(DataInputStream dis) {
		try {
			boolean isSuccess = dis.readBoolean();
			String desc = dis.readUTF();
			userName = dis.readUTF();
			pwd = dis.readUTF();
			System.out.print("�û�ע����:" + isSuccess + ",desc=" + desc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author liuzhigang
	 * @param dis
	 *            ����ע�᷵�ص���Ϣ
	 */
	public void receiveServerList(DataInputStream dis) {
		try {
			int normalServerID = dis.readInt();
			int lastLoginServerID = dis.readInt();
			System.out.println("�ϴε�¼������ID:" + lastLoginServerID);
			int serverCount = dis.readShort();
			String server_ip = "127.0.0.1";
			int server_port = 60000;
			for (int index = 1; index <= serverCount; index++) {
				int server_id = dis.readInt();
				String server_name = dis.readUTF();
				server_ip = dis.readUTF();
				server_port = dis.readInt();
				String state = dis.readUTF();
				System.out.println("��������Ϣ:" + server_id + "===" + server_name
						+ "===" + server_ip + "===" + server_port + "==="
						+ state);
			}
			socket.close();
			socket = new Socket(server_ip, server_port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			// ��ʼ���͵�¼����
			byte[] sendData=send(ROLE_LOGIN_SERVER);
			flush(sendData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		createLink();
	}
}
