package projetoredes;


import com.google.gson.Gson;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Alexandre Zeferino Lima
 */
public class ThreadAtendimento extends Thread {
    
    private DatagramPacket pacote;
    private DatagramSocket serverSocket;
    private Receiver receiver;
    private int pacoteEsperado;
    
    public void updateThread(DatagramPacket pkt, DatagramSocket receiverSocket, Receiver receiverReference, int numSeqEsperado) throws Exception {
        //Atualiza os dados contidos na thread, como o atual pacote recebido e o número do pacote esperado
        pacote = pkt;
        serverSocket = receiverSocket;
        receiver = receiverReference;
        pacoteEsperado = numSeqEsperado;
        
        //Faz a verificação da atualização dos dados
        verifica();
    }
    
    public void verifica() throws Exception {
        
        //Cria nova string a partir da mensagem enviada pelo sender
        Gson gson = new Gson();
        String data = new String(pacote.getData(), pacote.getOffset(), pacote.getLength());

        //Converte de GSON para a classe Mensagem
        Mensagem mensagemRecebida = gson.fromJson(data, Mensagem.class);
        
        //Caso o pacote recebido seja diferente do esperado
        if (mensagemRecebida.getseqNumber() != pacoteEsperado) {
            
            //Verifica-se se essa mensagem já foi recebida. Se sim, é uma duplicata.
            if (mensagemRecebida.getseqNumber() <= receiver.getLastValidACK()) {
                System.out.println(""
                    + "Mensagem id "
                    + mensagemRecebida.getseqNumber()
                    + " recebida de forma duplicada");
                
            } else {
                //Caso contrário, está fora de ordem de LastValidACK até o atual recebido.
                
                //Imprime a lista de pacotes ainda não recebidos.
                System.out.print(""
                    + "Mensagem id "
                    + mensagemRecebida.getseqNumber()
                    + " recebida fora de ordem, ainda não recebidos os identificadores [");
                
                for (int i = pacoteEsperado; i < mensagemRecebida.getseqNumber() - 1; i++) {
                    System.out.print(i + ", ");
                }
                
                System.out.println((mensagemRecebida.getseqNumber()-1) + "]");
                
                //Requisita o envio do próximo pacote esperado, que equivale ao atual recebido acrescido de um. Ou seja, é o ACK de resposta.
                requisitaPacote(pacoteEsperado, 2);
            }
            
        } else {

            //Caso a mensagem seja a esperada, ela é guardada no buffer de mensagens recebidas.
            receiver.guardaBuffer(data, mensagemRecebida.getseqNumber());
            requisitaPacote(pacoteEsperado, 1);
            /*Para debug, pode-se verificar qual foi a mensagem recebida por meio do print abaixo.
             *System.out.println("Pacote recebido. Mensagem:" + mensagemRecebida.getMensagem());
            */
            
        }
    }
    
    public void requisitaPacote(int numPacote, int opcaoEntrega) throws Exception {
        
        //Cria uma mensagem que corresponde ao ACK de resposta para o sender.
        Mensagem resposta = new Mensagem("", opcaoEntrega, numPacote, numPacote+1, true);
        
        //Opcao entrega: apenas para informar o recebimento (1) ou enviar o ACK do pacote faltante.
        
        
        //Transforma em string via GSON.
        Gson gson = new Gson();
        String strMsg = gson.toJson(resposta);
        
        //Guarda o ACK de resposta no buffer de respostas.
        receiver.guardaBufferResposta(strMsg);
        
        byte[] sendBuf = new byte[1024];
        sendBuf = strMsg.getBytes();
        
        //Captura o IP e a porta do remetente.
        InetAddress IPAddress = pacote.getAddress();
        int port = pacote.getPort();
        
        
        //Cria o pacote com a Mensagem e os dados de entrega para o sender.
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, IPAddress, port);
        
        //Envia o pacote de resposta para o sender.
        serverSocket.send(sendPacket);
        
        /*Para debug, pode-se imprimir que o server enviou o ACK resposta por meio do print abaixo.
         *System.out.println("mensagem enviada pelo server\n");
        */
    }    
}