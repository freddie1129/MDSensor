package socketclientfx;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import socketfx.Constants;
import socketfx.FxSocketClient;
import socketfx.SocketListener;
import java.nio.charset.StandardCharsets;

import static java.lang.Math.*;
import java.io.IOException;
import java.io.PipedInputStream;

import javafx.scene.control.TableView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.lang.Thread.*;
import java.lang.InterruptedException;
import java.nio.ByteBuffer;

import java.util.Timer;
import java.util.TimerTask;

import static socketclientfx.ConsVar.PointsNum;

class genSinSignal {

    public double gfre;
    public double phase = 0;
    private double step;

    public genSinSignal() {

    }

    public genSinSignal(double fre, double fs) {
        //   double fs = 100;
        gfre = fre;
        step = 2 * Math.PI * fre / fs;
    }

    public void init(double fre, double fs) {
        //  double fs = 100;
        gfre = fre;
        step = 2 * Math.PI * fre / fs;
    }

    double[] getData(int len) {
        double[] s = new double[len];
        for (int i = 0; i < len; i++) {
            phase += step;
            phase = phase % (2 * PI);
            s[i] = sin(phase);
        }
        return s;
    }
}

/**
 * FXML Controller class
 *
 * @author jtconnor
 */
public class FXMLDocumentController implements Initializable {

    public genSinSignal[] gFactory;
    public double fs;

    public DataFactory dataSource;
    public PipedInputStream pipeIn;

    Timer timer;

    class AutoSendTask extends TimerTask {

        FXMLDocumentController fc;

        public AutoSendTask(FXMLDocumentController fc) {

            this.fc = fc;

        }

        public void run() {
            fc.SendData();
            // System.out.println("Time's up!");
            // toolkit.beep();
            //timer.cancel(); //Not necessary because we call System.exit
            //  System.exit(0); //Stops the AWT thread (and everything else)
        }
    }

    public void testTimer() {
        System.out.println("Time's up!");
    }

    @FXML
    private TableView<Frequency> tableView;
//    @FXML
//    private TextField FreTextField;
//    @FXML
//    private TextField FsTextField;
    
    
    @FXML
    private TextField TagTextField;

//    @FXML
//    private Button startButton;

//    @FXML
    //   private ListView<String> rcvdMsgsListView;
//    @FXML
    //   private ListView<String> sentMsgsListView;
    @FXML
    private Button sendAutoButton;

    @FXML
    private ListView<String> logListView;

//    @FXML
//    private Button sendButton;
//    @FXML
//    private TextField sendTextField;
//    @FXML
//    private TextField selectedTextField;
    @FXML
    private TextField signalFreTextFile;

    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private TextField hostTextField;
    @FXML
    private TextField portTextField;


    private final static Logger LOGGER
            = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private ObservableList<String> rcvdMsgsData;
    private ObservableList<String> sentMsgsData;

    private ListView lastSelectedListView;

    private ObservableList<String> logData;

    private boolean connected;
    private volatile boolean isAutoConnected;

    private static final int DEFAULT_RETRY_INTERVAL = 2000; // in milliseconds

    public enum ConnectionDisplayState {

        DISCONNECTED, ATTEMPTING, CONNECTED, AUTOCONNECTED, AUTOATTEMPTING
    }

    private FxSocketClient socket;

    /*
     * Synchronized method set up to wait until there is no socket connection.
     * When notifyDisconnected() is called, waiting will cease.
     */
    private synchronized void waitForDisconnect() {
        while (connected) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /*
     * Synchronized method responsible for notifying waitForDisconnect()
     * method that it's OK to stop waiting.
     */
    private synchronized void notifyDisconnected() {
        connected = false;
        notifyAll();
    }

    /*
     * Synchronized method to set isConnected boolean
     */
    private synchronized void setIsConnected(boolean connected) {
        this.connected = connected;
    }

    /*
     * Synchronized method to check for value of connected boolean
     */
    private synchronized boolean isConnected() {
        return (connected);
    }

    private void connect() {
        socket = new FxSocketClient(new FxSocketListener(),
                hostTextField.getText(),
                Integer.valueOf(portTextField.getText()),
                Constants.instance().DEBUG_SEND);
        socket.connect();        
    }

    private void autoConnect() {
        new Thread() {
            @Override
            public void run() {
                while (isAutoConnected) {
                    if (!isConnected()) {
                        socket = new FxSocketClient(new FxSocketListener(),
                                hostTextField.getText(),
                                Integer.valueOf(portTextField.getText()),
                                Constants.instance().DEBUG_NONE);
                        socket.connect();
                    }
                    waitForDisconnect();
//                    try {
//                       // Thread.sleep(Integer.valueOf(
//                         //       retryIntervalTextField.getText()) * 1000);
//                    } catch (InterruptedException ex) {
//                    }
                }
            }
        }.start();
    }

    private void displayState(ConnectionDisplayState state) {
        switch (state) {
            case DISCONNECTED:
                connectButton.setDisable(false);
                disconnectButton.setDisable(true);
                //sendButton.setDisable(true);
//                sendTextField.setDisable(true);
 //               connectedLabel.setText("Not connected");
                break;
            case ATTEMPTING:
            case AUTOATTEMPTING:
                connectButton.setDisable(true);
                disconnectButton.setDisable(true);
                //sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                connectedLabel.setText("Attempting connection");
                break;
            case CONNECTED:
                connectButton.setDisable(true);
                disconnectButton.setDisable(false);
                //sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                connectedLabel.setText("Connected");
                break;
            case AUTOCONNECTED:
                connectButton.setDisable(true);
                disconnectButton.setDisable(true);
                //sendButton.setDisable(false);
//                sendTextField.setDisable(false);
 //               connectedLabel.setText("Connected");
                break;
        }
    }

//    @FXML
//    protected void startFactory(ActionEvent event) {
//        if (dataSource.getState() == State.NEW) {
//            dataSource.start();
//            startButton.setText("stop");
//        } else if (dataSource.getState() == State.RUNNABLE || dataSource.getState() == State.TIMED_WAITING) {
//            try {
//                dataSource.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            startButton.setText("continue");
//        } else if (dataSource.getState() == State.WAITING) {
//            dataSource.notify();
//            startButton.setText("stop");
//        }
//    }

    @FXML
    protected void addSignal(ActionEvent event) {
        ObservableList<Frequency> data = tableView.getItems();
        int num = data.size() + 1;
        double fre = Double.parseDouble("10"/*FreTextField.getText()*/);
        double fs = Double.parseDouble("100"/*FsTextField.getText()*/);
        
        String strTag =  String.format("%s%03d",TagTextField.getText(),num);    
        
        

        

        data.add(new Frequency(num, fre, fs, strTag));

        num = tableView.getItems().size();
        dataSource.ResetFactory();

        for (int i = 0; i < num; i++) {
         //   String stringTag = data.get(i).getTag() + String.format("Duke's Birthday: %1$tm %1$te,%1$tY", c);
            

            
                    dataSource.addSig(data.get(i).getFre(),
                                data.get(i).getFs(),
                                data.get(i).getTag().toCharArray());
           // dataSource.addSig(data.get(i).getFre(),data.get(i).getFs(),);
            //dataSource.setFs(data.get(i).getFs());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        
      //  signalFreTextFile.setVisible(false);
//        FreTextField.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                if (newValue.matches("\\d*")) {
//                    int value = Integer.parseInt(newValue);
//                } else {
//                    FreTextField.setText(oldValue);
//                }
//            }
//        });

//        FsTextField.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                if (newValue.matches("\\d*")) {
//                    int value = Integer.parseInt(newValue);
//                } else {
//                    FsTextField.setText(oldValue);
//                }
//            }
//        });
//        
        TagTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("\\p{Upper}")) {
                    int value = Integer.parseInt(newValue);
                } else {
                    TagTextField.setText(oldValue);
                }
            }
        });

        dataSource = new DataFactory();
        dataSource.setBlockLen(PointsNum);
        pipeIn = new PipedInputStream();
        try {
            pipeIn.connect(dataSource.pipeOut);
        } catch (IOException e) {
            System.out.println(e);
        }

        gFactory = new genSinSignal[5];
        fs = 200;

        setIsConnected(false);
        isAutoConnected = false;
        displayState(ConnectionDisplayState.DISCONNECTED);
        
   


        Runtime.getRuntime().addShutdownHook(new ShutDownThread());
       
    }

    class ShutDownThread extends Thread {

        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");
                }
                socket.shutdown();
            }
        }
    }

    class FxSocketListener implements SocketListener {

        @Override
        public void onMessage(String line) {
            if (line != null && !line.equals("")) {
                rcvdMsgsData.add(line);
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
            if (isClosed) {
                notifyDisconnected();
                if (isAutoConnected) {
                    displayState(ConnectionDisplayState.AUTOATTEMPTING);
                } else {
                    displayState(ConnectionDisplayState.DISCONNECTED);
                }
            } else {
                setIsConnected(true);
                if (isAutoConnected) {
                    displayState(ConnectionDisplayState.AUTOCONNECTED);
                } else {
                    displayState(ConnectionDisplayState.CONNECTED);
                }
            }
        }
    }

//    @FXML
//    private void handleClearRcvdMsgsButton(ActionEvent event) {
//        rcvdMsgsData.clear();
//        if (lastSelectedListView == rcvdMsgsListView) {
//            selectedTextField.clear();
//        }
//    }
    @FXML
    private void handleClearLogButton(ActionEvent event) {
        logData.clear();
        if (lastSelectedListView == logListView) {
            //selectedTextField.clear();
        }
    }

//        @FXML
//    private void handleSetParamButton(ActionEvent event) {
//            gFactory[0].init(Double.parseDouble(signalFreTextFile0.getText()),fs);
//            gFactory[1].init(Double.parseDouble(signalFreTextFile1.getText()),fs);
//            gFactory[2].init(Double.parseDouble(signalFreTextFile2.getText()),fs);
//            gFactory[3].init(Double.parseDouble(signalFreTextFile3.getText()),fs);
//            gFactory[4].init(Double.parseDouble(signalFreTextFile4.getText()),fs);
//    }
//    @FXML
//    private void handleClearSentMsgsButton(ActionEvent event) {
//        sentMsgsData.clear();
//        if (lastSelectedListView == sentMsgsListView) {
//            selectedTextField.clear();
//        }
//    }
    //public void char[] formatFream(char[] buf)
    //{
    //    
    //    
    //}
//    public byte[] doublsToBytes(double[] values) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(baos);
//        for (int i = 0; i < values.length; ++i) {
//            try {
//                dos.writeDouble(values[i]);
//            } catch (IOException e) {
//                
//            }
//        }
//        
//        return baos.toByteArray();
//    }  
    @FXML
    private void handleStopSend(ActionEvent event) {

        timer.cancel();
    }
        public double[] toDouble(byte[] bytes, int num) {
            double[] da = new double[num / 8];
            for (int i = 0; i < num / 8; i++) {
                byte[] te = new byte[8];
                System.arraycopy(bytes, i * 8, te, 0, 8);
                da[i] = ByteBuffer.wrap(te).getDouble();
            }
            return da;
        }
        
    public void SendData() {

        System.out.println("**********************");
        String strLog = "";
        int len = 8 * tableView.getItems().size();
        byte[] dIn = new byte[len];
        int rlen;
        int p = 0;

        do {
            try {
                rlen = pipeIn.read(dIn, p, len - p);
                System.out.printf("rlen = %d\n", rlen);
                if (rlen != -1) {
                    p = p + rlen;
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        } while (p != len);
        
        double[] test = toDouble(dIn,dIn.length);
        for (int i = 0; i < test.length; i++)
        {
             strLog = strLog + String.format(" %02f", test[i]);       
        }
             System.out.println(strLog);
        
        strLog = "";
        for (int i = 0; i < dIn.length; i++) {
            strLog = strLog + String.format(" %02d", dIn[i]);
        }
        System.out.println(strLog);
        strLog = "";
        
        short PN = (short) PointsNum;                       //Number of points, usually 1
        short CN = (short) tableView.getItems().size();     //Number of signal, 
        byte[]  frameHead = new byte[8 + CN * 4 + len];        
        frameHead[0] = 'f';
        frameHead[1] = 'a';
        frameHead[2] = 'f';
        frameHead[3] = 'e';        
        frameHead[4] = (byte) (PN >>> 8);
        frameHead[5] = (byte) (PN & 0xFF);
        frameHead[6] = (byte) (CN >>> 8);
        frameHead[7] = (byte) (CN & 0xFF);       
        
        ObservableList<Frequency> data = tableView.getItems();
        for (int i = 0; i < data.size(); i++)
        {
            byte[] tag = data.get(i).getTag().getBytes(StandardCharsets.UTF_8);
            char[] chartag = data.get(i).getTag().toCharArray(); //  .getBytes(StandardCharsets.UTF_8);
            System.arraycopy(data.get(i).getTag().getBytes(StandardCharsets.UTF_8), 0, frameHead, 8 + i * 4, 4);
        }
        System.arraycopy(dIn,0,frameHead,8 + CN * 4 ,len);
        
        
        short pointNum = (short) (frameHead[4]<< 8 | frameHead[5] & 0xFF);
                        short sigNum = (short) (frameHead[6]<< 8 | frameHead[7] & 0xFF);
                        

                        
                        
                        
                        System.out.printf("HEAD:%c%c%c%c Points:%d SignalNum: %d\n", (char)frameHead[0],(char)frameHead[1],(char)frameHead[2],(char)frameHead[3],
                                                pointNum, sigNum);
                        
                        for (int i = 0; i < sigNum; i++)
                        {
                             System.out.printf("tag %d: %c%c%c%c\n", 
                                     i,
                                     (char)frameHead[8 + i * 4 + 0],
                                     (char)frameHead[8 + i * 4 + 1],
                                     (char)frameHead[8 + i * 4 + 2],
                                     (char)frameHead[8 + i * 4 + 3]);
                        }
                        
                        
                        
        
        

        
        socket.sendMessage(frameHead, 0, frameHead.length);
      //  socket.sendMessage(dIn, 0, dIn.length);
        System.out.printf("SendLength = %d", dIn.length);
    }

    @FXML
    private void handleAutoSendMessageButton(ActionEvent event) {
        if (dataSource.getState() == State.NEW) {
            dataSource.start();}
            
        timer = new Timer();
        timer.schedule(new AutoSendTask(this), 1000, 1000);
    }
//
//    @FXML
//    private void handleSendMessageButton(ActionEvent event) {
//        if (!sendTextField.getText().equals("")) {
//            socket.sendMessage(sendTextField.getText());
//            sentMsgsData.add(sendTextField.getText());
//        }
//    }

    @FXML
    private void handleConnectButton(ActionEvent event) {
        displayState(ConnectionDisplayState.ATTEMPTING);
        connect();
    }

    @FXML
    private void handleDisconnectButton(ActionEvent event) {
        socket.shutdown();
    }

//    @FXML
//    private void handleAutoConnectCheckBox(ActionEvent event) {
//        if (autoConnectCheckBox.isSelected()) {
//            isAutoConnected = true;
//            if (isConnected()) {
//                displayState(ConnectionDisplayState.AUTOCONNECTED);
//            } else {
//                displayState(ConnectionDisplayState.AUTOATTEMPTING);
//                autoConnect();
//            }
//        } else {
//            isAutoConnected = false;
//            if (isConnected()) {
//                displayState(ConnectionDisplayState.CONNECTED);
//            } else {
//                displayState(ConnectionDisplayState.DISCONNECTED);
//            }
//        }
//    }

//    @FXML
//    private void handleRetryIntervalTextField(ActionEvent event) {
//        try {
//            Integer.parseInt(retryIntervalTextField.getText());
//        } catch (NumberFormatException ex) {
//            retryIntervalTextField.setText(
//                    Integer.toString(DEFAULT_RETRY_INTERVAL / 1000));
//        }
//    }
}
