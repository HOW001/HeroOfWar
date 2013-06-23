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
	private static final int REGISTER_USER = 0X010001;// 创建用户,失败返回提示,成功返回服务器列表
	private static final int LOGIN_USER = 0X010002;// 用户登录,失败返回提示,成功返回服务器列表
	private static final int SERVER_LIST = 0X010003;// 服务器列表,客户端不用主动请求
	private static final int ROLE_LOGIN_SERVER = 0X010004;// 角色登录游戏
	private static final int CREATE_ROLE = 0x010005;// 角色创建
	private static final int PLAYER_TICK = 0x020001;// 玩家心跳信息
	private static final int RECEIVE_UI_INFO = 0x0C0001;// 接收提示信息

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
	 * @author liuzhigang 开始建立连接
	 */
	public void createLink() {

		try {
			System.out.println("开始建立连接");
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
						System.out.println(userName+":发送心跳数据长度");
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
			System.out.println("发送数据长度:" + sendData.length);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
	/**
	 * @author liuzhigang
	 * @return 发送信息
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
	 *            创建角色
	 */
	public void sendCreateRole(DataOutputStream dos) {
		try {
			dos.writeUTF(roleName);
			dos.writeByte(gender);
			System.out.println("创建角色:roleName=" + roleName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author liuzhigang
	 * @param dos
	 *            角色登录
	 */
	public void sendRoleLogin(DataOutputStream dos) {
		try {
			System.out.println(userName + "发送登录游戏命令!");
			dos.writeUTF(userName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author liuzhigang
	 * @param dos
	 *            用户登录
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
	 * @return 发送注册请求
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
	 *            接收信息
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
							.println("前端收到未知信息:0x" + Integer.toHexString(cmd));
				}
				System.out.println("收到登录返回信息:realLen=" + realLen + ",cmd=0x"
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
	 *            收到新建角色命令
	 */
	public void receiveCreateRole(DataInputStream dis) {
		try {
			roleName = dis.readUTF();
			gender = dis.readByte();
			System.out.println("收到可创建的角色名:" + roleName);
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
	 *            接收UI信息
	 */
	public void receiveUIInfo(DataInputStream dis) {
		try {
			int type = dis.readShort();// 弹板类型
			String desc = dis.readUTF();// 内容描述
			String left = dis.readUTF();// 左键信息
			String right = dis.readUTF();// 右键信息
			int leftCommand = dis.readInt();// 左键命令
			int leftParam = dis.readShort();// 左键命令参数个数
			System.out.println("type=" + type + ",desc=" + desc + ",left="
					+ left + ",right=" + right + ",leftCommand="
					+ Integer.toHexString(leftCommand) + ",leftParam="
					+ leftParam);
			for (int index = 1; index <= leftParam; index++) {
				String leftValue = dis.readUTF();
				System.out.println("value" + index + "=" + leftValue);
			}
			int rightCommand = dis.readInt();// 右键命令
			int rightParam = dis.readShort();// 右键命令参数个数
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
			System.out.print("用户登录结果:" + isSuccess + ",desc=" + desc);
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
			System.out.print("用户注册结果:" + isSuccess + ",desc=" + desc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @author liuzhigang
	 * @param dis
	 *            接收注册返回的信息
	 */
	public void receiveServerList(DataInputStream dis) {
		try {
			int normalServerID = dis.readInt();
			int lastLoginServerID = dis.readInt();
			System.out.println("上次登录服务器ID:" + lastLoginServerID);
			int serverCount = dis.readShort();
			String server_ip = "127.0.0.1";
			int server_port = 60000;
			for (int index = 1; index <= serverCount; index++) {
				int server_id = dis.readInt();
				String server_name = dis.readUTF();
				server_ip = dis.readUTF();
				server_port = dis.readInt();
				String state = dis.readUTF();
				System.out.println("服务器信息:" + server_id + "===" + server_name
						+ "===" + server_ip + "===" + server_port + "==="
						+ state);
			}
			socket.close();
			socket = new Socket(server_ip, server_port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			// 开始发送登录命令
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
