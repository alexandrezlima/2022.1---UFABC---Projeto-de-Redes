package projetoredes;


import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


/**
 *
 * @author Alexandre Zeferino Lima
 */
public class Sender {
    
    private ArrayList<String> bufferMensagens;
    private ArrayList<String> bufferACK;
    private int base;
    private int nextSeq;
    private boolean contadorAtivo;
    private int lastValidACK;
    private InetAddress IPAddress;
    private int porta;
    private DatagramSocket socket;
    private Sender self;
    
    public static void main(String[] args) throws Exception {
        
        // O SO designará uma porta entre 1024 e 65535 para porta local (sender.)
        DatagramSocket clientSocket = new DatagramSocket();
            
        //Adquire IP
        InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
        
        //Cria instância do Sender com o socket e o IP.
        Sender newSender = new Sender();
        
        //A instância criada passará a ser usada para controle do envio de mensagens.
        newSender.run(IPAddress, 9888, clientSocket, newSender);
    }
    
    public void run(InetAddress IP, int port, DatagramSocket clientSocket, Sender newSender) throws Exception {
        //Inicializa as variáveis do sender.
        bufferMensagens = new ArrayList<>();
        bufferACK = new ArrayList<>();
        base = 0;
        nextSeq = 0;
        contadorAtivo = false;
        lastValidACK = 0;
        IPAddress = IP;
        porta = port;
        socket = clientSocket;
        self = newSender;
        
        //Inicializa o sender para que possa receber entradas do teclado.
        initializeSender();
    }
    
    public void initializeSender() throws Exception {
        
        //Cria uma thread para recebimento de mensagens do receiver.
        ThreadSender threadRecebimento = new ThreadSender(self, socket);
        threadRecebimento.start();
        
        //Enquanto o cliente estiver rodando, poderá enviar mensagens.        
        while(true) {
            
            System.out.println("Insira sua mensagem:");
            
            //BLOCKING - Captura entrada do teclado
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            String textoMensagem = inFromUser.readLine();
            
            //Imprime as opções de envio.
            System.out.println(""
                    + "Escolha o tipo de envio da mensagem:\n"
                    + "1. Envio como pacote lento\n"
                    + "2. Envio como pacote perdido\n"
                    + "3. Envio como pacote duplicado\n"
                    + "4. Envio normal\n"
                    + "5. Envio fora de ordem");
            
            //BLOCKING - Captura entrada do teclado, em que o usuário escolhe uma opção
            String opcaoEnvio = inFromUser.readLine();
            
            //Prepara pacote para envio da mensagem
            preparaPacote(textoMensagem, Integer.parseInt(opcaoEnvio));
            
        }
    }
    
    public void preparaPacote(String mensagem, int tipoEnvio) throws Exception {
        
        //Cria a instância da mensagem com a classe do tipo Mensagem
        Mensagem msg = new Mensagem(mensagem, tipoEnvio, nextSeq, nextSeq+1, false);
        
        //Transforma em string via GSON
        Gson gson = new Gson();
        String strMsg = gson.toJson(msg);
        
        //Adiciona ao buffer de mensagens
        bufferMensagens.add(strMsg);
        
        System.out.println("Mensagem \""
                + msg.getMensagem()
                + "\" enviada como opção "
                + tipoEnvio + " ("
                + tipoEnvioMensagem(tipoEnvio)
                + ") com id "
                + nextSeq);
        
        //Como não há necessidade de verificação (checksum), a mensagem está pronta para envio
        nextSeq++;
        
        enviaPacote(strMsg, tipoEnvio);
    }
    
    public void enviaPacote(String msg, int tipoEnvio) throws Exception {
        
        byte[] sendData = new byte[1024];
        sendData = msg.getBytes();
        
        //Cria o datagrampacket a partir da mensagem
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);

        switch (tipoEnvio) {
            case 1: //Pacote lento
                if (!contadorAtivo) {
                    //Caso o contador não esteja ativo, ele é iniciado
                    setContadorAtivo(true);
                    ThreadTemporizador tmp = new ThreadTemporizador(nextSeq, nextSeq-1, self, 1, sendPacket);
                    tmp.start();
                }
                //Caso contrário, o contador já está ativo, e o pacote já está na fila de reenvio.
                break;
            
            case 2: //Simula perda de pacote
                if (!contadorAtivo) {
                    //Caso o contador não esteja ativo, é iniciado.
                    setContadorAtivo(true);
                    ThreadTemporizador tmp = new ThreadTemporizador(nextSeq, nextSeq-1, self, 2, sendPacket);
                    tmp.start();
                    
                    //Note que não há o envio do pacote, simulando a perda.
                }
                //Caso o contrário, como o pacote já está na fila, aguarda-se o término do contador para o reenvio.
                break;
            case 3: //Simula o envio duplicado do pacote.
                socket.send(sendPacket);
                socket.send(sendPacket);
                break;
            case 5: //Simula fora de ordem, ou seja, este pacote não é enviado agora. 
                break;
            default://Envio normal do pacote, sem atraso, perda ou duplicação.
                socket.send(sendPacket);
                break;
        }
    }
    
    public void reenviaPacotes(int i) throws Exception {
        
        /*
        //Reenvia apenas o pacote i que ainda não teve ACK confirmado.
        if (contadorAtivo == false) {
            if (i < bufferMensagens.size()) {
                enviaPacote(bufferMensagens.get(i), 4);
                System.out.println("Reenviando o pacote " + i);
            }
        }
        */
        
        //Reenvia todos os pacotes que não tiveram ACK confirmados, de i (que é a base) até o nextSeqNumber.
        
        //if (i >= lastValidACK) {
            if (contadorAtivo == false) {
                if (i < bufferMensagens.size()) {
                    System.out.print("Reenviando o(s) pacote(s) com id(s)");
                    for (int k = i; k < bufferMensagens.size(); k++) {
                        System.out.print(" " + k + " ");
                    }
                }
                System.out.println();
            }

            if (contadorAtivo == false) {
                if (i < bufferMensagens.size()) {
                    for (int k = i; k < bufferMensagens.size(); k++) {
                        enviaPacote(bufferMensagens.get(k), 4);
                    }
                }
            }
        //}
    }
    
    //Retorna uma string correspondente a opção de envio escolhida pelo usuário.
    public String tipoEnvioMensagem(int numEnvio) {
        
        switch (numEnvio) {
            case 1: return "Envio como pacote lento";
            
            case 2: return "Envio como pacote perdido";
            
            case 3: return "Envio como pacote duplicado";
            
            case 4: return "Envio normal";
            
            case 5: return "Fora de ordem";
        }
        
        return "";
    }
    
    //Retorna o último ACK recebido.
    public int getLastValidACK() {
        return lastValidACK;
    }
    
    //Retorna o próximo número de sequência.
    public int getNextSeq() {
        return nextSeq;
    }
    
    //Atualiza a base e o último ack recebido e salva a resposta no buffer.
    public void atualizaBase(DatagramPacket resposta){
        
        
        //Salva a mensagem recebida no buffer de ACKs
        String informacaoResposta = new String(resposta.getData(), resposta.getOffset(), resposta.getLength());
        Gson gson = new Gson();
        Mensagem mensagemReceiver = gson.fromJson(informacaoResposta, Mensagem.class);
        
        base = mensagemReceiver.getseqNumber();
        lastValidACK = base;
        
        String strMsg = gson.toJson(mensagemReceiver);
        bufferACK.add(strMsg);
        
    }
    
    //Registra a informação se o contador está ativo ou não.
    public void setContadorAtivo(boolean ativo) {
        contadorAtivo = ativo;
    }
    
    //Retorna o socket criado para comunicação entre o sender e o receiver.
    public DatagramSocket getSocket() {
        return socket;
    }
    
}