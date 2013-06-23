
public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        for(int i=1;i<=10;i++){
        	Thread thread=new Thread(new HOWClient());
        	thread.start();
        }
	}
}
