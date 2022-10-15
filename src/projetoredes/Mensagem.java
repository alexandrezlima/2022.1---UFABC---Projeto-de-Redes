package projetoredes;

/**
 *
 * @author Alexandre Zeferino Lima
 */
public class Mensagem {
    
    private String mensagem;
    private int opcaoEnvio;
    private int seqNumber;
    private int ackNumber;
    private boolean isACK;
    
    public Mensagem(String msg, int opcao, int seqNum, int ackNum, boolean bisACK) {
        /*
         * A mensagem contém informações úteis, como:
         *  -A mensagem enviada pelo sender (recolhida via input do usuário).
         *  -A opção de envio (1 a 4), escolhida pelo usuário.
         *  -O número de sequência da mensagem.
         *  -O ACK da mensagem.
         *  -Flag indicando se a mensagem em questão trata-se de uma resposta ACK.
        */
        mensagem = msg;
        opcaoEnvio = opcao;
        seqNumber = seqNum;
        ackNumber = ackNum;
        isACK = bisACK;
    }
    
    //Retorna o conteúdo da mensagem em questão.
    public String getMensagem() {
        return mensagem;
    }
    
    //Retorna a opção de envio.
    public int getOpcaoEnvio() {
        return opcaoEnvio;
    }
    
    //Retorna o número de sequência da mensagem.
    public int getseqNumber() {
        return seqNumber;
    }
}
