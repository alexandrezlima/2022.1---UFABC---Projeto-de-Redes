package projetoredes;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexandre Zeferino Lima
 */
public class ThreadSender extends Thread {
    private Sender sender;
    DatagramSocket socket;
    
    
    public ThreadSender(Sender remetente, DatagramSocket socketDestinatario) {
        sender = remetente; //Referência do sender.
        socket = socketDestinatario;
    }
    
    public void run() {
        
        //A thread fica sempre no aguardo de mensagens do receiver.
        while (true) {
            try {
                byte[] recBuffer = new byte[1024];
                DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);

                //BLOCKING - aguarda recebimento do ACK
                socket.receive(recPkt);
                
                //A partir da mensagem ACK recebida, faz-se a conversão de string para a classe "mensagem" via GSON
                String informacao = new String(recPkt.getData(), recPkt.getOffset(), recPkt.getLength());
                Gson gson = new Gson();
                Mensagem mensagemReceiver = gson.fromJson(informacao, Mensagem.class);
                
                //Note que opcaoEnvio = 1 apenas serve para exibir a confirmação de entrega, e não equivale ao ACK acumulativo do GBN.
                if (mensagemReceiver.getOpcaoEnvio() == 1) {
                    System.out.println("Mensagem id " + (mensagemReceiver.getseqNumber()) + " recebida pelo receiver.");
                } else {
                
                    //Por outro lado, opcaoEnvio != 1 significa recebimento de ACK acumulativo, logo atualiza-se a base e reenviam-se os pacotes da base de base até nextSeq.

                    //Verifica se o próximo pacote já está na fila de envio e o envia em caso positivo.
                    try {
                        sender.atualizaBase(recPkt);
                        sender.reenviaPacotes(mensagemReceiver.getseqNumber());
                        /*Para debug, o print abaixo apresenta qual pacote o receiver está requisitando.
                         *System.out.println("Pedindo o pacote " + mensagemReceiver.getseqNumber());
                        */
                    } catch (Exception ex) {
                        Logger.getLogger(ThreadSender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {

            }
        }
    }
}
