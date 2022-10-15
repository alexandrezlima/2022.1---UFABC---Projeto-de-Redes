package projetoredes;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

/**
 *
 * @author Alexandre Zeferino Lima
 */
public class Receiver {
    
    private int lastValidACK;
    private Receiver self;
    private ArrayList<String> bufferMensagens;
    private ArrayList<String> bufferRespostas;
    private DatagramSocket serverSocket;
    
    public static void main(String[] args) throws Exception {
        DatagramSocket serverSkt = new DatagramSocket(9888);
        
        //Cria uma instância do receiver e a inicia.
        Receiver newReceiver = new Receiver();
        newReceiver.run(newReceiver, serverSkt);
    }
    
    public void run(Receiver newReceiver, DatagramSocket serverSkt) throws Exception {
        lastValidACK = -1;
        self = newReceiver; //Referência à instância do receiver.
        bufferMensagens = new ArrayList<>();
        bufferRespostas = new ArrayList<>();
        serverSocket = serverSkt;
        
        initializeReceiver();
    }
    
    public void initializeReceiver() throws Exception {
        //Atendimento via uma thread em paralelo
        ThreadAtendimento thread = new ThreadAtendimento();
        
        while (true) {
            byte[] recBuffer = new byte[1024];
            DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);
            
            /*Para debug, o print abaixo informaria qual o pacote que o receiver está esperando.
             *System.out.println("Aguardando pacote ID " + (lastValidACK+1));
            */
            
            //Aguarda alguma mensagem do sender
            serverSocket.receive(recPkt); //BLOCKING
            
            //Após recebida a mensagem, utiliza a thread criada para tratar a mensagem
            thread.updateThread(recPkt, serverSocket, self, lastValidACK+1);
        }
    }
    
    //Retorna o último ACK enviado para o sender.
    public int getLastValidACK() {
        return lastValidACK;
    }
    
    //Salva a mensagem recebida ao buffer de mensagens, estas que irão para a camada de aplicação.
    public void guardaBuffer(String mensagemRecebida, int numSeq) {
        bufferMensagens.add(mensagemRecebida);
        
        //Atualiza-se o último ACK enviado para o sender.
        lastValidACK++;
        
        //Imprime, conforme solicitado, a mensagem recebida em ordem.
        System.out.println("Mensagem id " + numSeq + " recebida na ordem, entregando para a camada de aplicação.");
    }
    
    //Salva o ACK de resposta ao buffer de respostas.
    public void guardaBufferResposta(String resposta) {
        bufferRespostas.add(resposta);
    }
}