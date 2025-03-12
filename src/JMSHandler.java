import javax.jms.*;
import javax.naming.*;
public abstract class JMSHandler {
    protected String host;
    protected Context jndiContext;
    protected ConnectionFactory connectionFactory;
    protected Queue queue;
    protected Connection connection;
    protected Session session;
    protected MessageProducer queueSender;
    protected MessageConsumer queueReceiver;

    protected JMSHandler(String host) throws NamingException, JMSException {
        this.host = host;
        createJNDIContext();
        lookupConnectionFactory();
        lookupQueue();
        createConnection();
        createSession();
        createSender();
        createReceiver();
    }
    private void createJNDIContext() throws NamingException {
        System.setProperty("org.omg.CORBA.ORBInitialHost", host);
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
        try {
            jndiContext = new InitialContext();
        } catch (NamingException e) {
            System.err.println("Could not create JNDI API context: " + e);
            throw e;
        }
    }
    private void lookupConnectionFactory() throws NamingException {

        try {
            connectionFactory = (ConnectionFactory)jndiContext.lookup("jms/JPoker24GameConnectionFactory");
        } catch (NamingException e) {
            System.err.println("JNDI API JMS connection factory lookup failed: " + e);
            throw e;
        }
    }
    private void lookupQueue() throws NamingException {

        try {
            queue = (Queue)jndiContext.lookup("jms/JPoker24GameQueue");
        } catch (NamingException e) {
            System.err.println("JNDI API JMS queue lookup failed: " + e);
            throw e;
        }
    }
    private void createConnection() throws JMSException {
        try {
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            System.err.println("Failed to create connection to JMS provider: " + e);
            throw e;
        }
    }
    protected void createSession() throws JMSException {
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            System.err.println("Failed to create session: " + e);
            throw e;
        }
    }
    protected void createSender() throws JMSException {
        try {
            queueSender = session.createProducer(queue);
        } catch (JMSException e) {
            System.err.println("Failed to create session: " + e);
            throw e;
        }
    }
    protected void createReceiver() throws JMSException {
        try {
            queueReceiver = session.createConsumer(queue);
        } catch (JMSException e) {
            System.err.println("Failed to create session: " + e);
            throw e;
        }
    }
    public void close() {
        if(connection != null) {
            try {
                connection.close();
            } catch (JMSException e) { }
        }
    }
}