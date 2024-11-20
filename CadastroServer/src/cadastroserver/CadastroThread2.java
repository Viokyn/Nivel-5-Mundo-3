/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cadastroserver;

import controller.MovimentoJpaController;
import controller.PessoaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import model.Movimento;
import model.Produto;
import model.Usuario;

public class CadastroThread2 extends Thread {

    public final ProdutoJpaController ctrl;
    public final UsuarioJpaController ctrlUsu;
    public final PessoaJpaController ctrlPessoa;
    public final MovimentoJpaController ctrlMov;
    public final JTextArea entrada;
    public final Socket s1;

    public CadastroThread2(ProdutoJpaController ctrl, UsuarioJpaController ctrlUsu, PessoaJpaController ctrlPessoa, MovimentoJpaController ctrlMov, JTextArea entrada, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.ctrlPessoa = ctrlPessoa;
        this.ctrlMov = ctrlMov;
        this.entrada = entrada;
        this.s1 = s1;
    }

    @Override
    public void run() {
        System.out.println("thread is running...");
        entrada.append(">> Nova comunicação em " + java.time.LocalDateTime.now() + "\n");

        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            in = new ObjectInputStream(s1.getInputStream());
            out = new ObjectOutputStream(s1.getOutputStream());

            String login = (String) in.readObject();
            String senha = (String) in.readObject();

            Usuario user = ctrlUsu.findUsuario(login, senha);
            if (user == null) {
                entrada.append("Erro de conexão do usuário\n");
                out.writeObject("nok");
                return;
            }
            out.writeObject("ok");
            entrada.append("Usuário conectado com sucesso\n");

            String input;
            do {
                input = (String) in.readObject();
                if ("l".equalsIgnoreCase(input)) {
                    List<Produto> produtos = ctrl.findProdutoEntities();
                    for (Produto produto : produtos) {
                        entrada.append(produto.getNome() + "::" + produto.getQuantidade() + "\n");
                    }
                    out.writeObject(produtos);
                } else if ("e".equalsIgnoreCase(input) || "s".equalsIgnoreCase(input)) {

                    Movimento movimento = new Movimento();
                    movimento.setUsuarioidUsuario(user);
                    movimento.setTipo(input.toUpperCase().charAt(0));

                    int idPessoa = Integer.parseInt((String) in.readObject());
                    movimento.setPessoaidPessoa(ctrlPessoa.findPessoa(idPessoa));

                    int idProduto = Integer.parseInt((String) in.readObject());
                    Produto produto = ctrl.findProduto(idProduto);
                    movimento.setProdutoidProduto(produto);

                    int quantidade = Integer.parseInt((String) in.readObject());
                    movimento.setQuantidadeProduto(quantidade);

                    BigDecimal valor = new BigDecimal((String) in.readObject());
                    movimento.setPrecoUnitario(valor);

                    if ("e".equalsIgnoreCase(input)) {
                        produto.setQuantidade(produto.getQuantidade() + quantidade);
                    } else {
                        produto.setQuantidade(produto.getQuantidade() - quantidade);
                    }
                    ctrl.edit(produto);
                    ctrlMov.create(movimento);
                    entrada.append("Movimento criado\n");

                } else if (!"x".equalsIgnoreCase(input)) {
                    System.out.println("Comando inválido recebido:" + input);
                    entrada.append("Comando inválido recebido:" + input + "\n");
                }

            } while (!input.equalsIgnoreCase("x"));

        } catch (Exception ex) {
            Logger.getLogger(CadastroThread2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }

            try {
                out.close();
            } catch (Exception e) {
            }
            
            entrada.append("<< Fim de comunicação em " + java.time.LocalDateTime.now()+ "\n");
            System.out.println("thread finalizada...");
        }
    }
}
