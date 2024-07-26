package com.so.filesystem.painel;

import javax.swing.*;
import com.so.filesystem.model.Arquivo;
import java.awt.*;
import java.awt.event.ActionListener;

public class PainelArquivo extends JPanel {

    public PainelArquivo(Arquivo arquivo, ActionListener salvarAcao, ActionListener editarAcao, ActionListener excluirAcao) {
        setLayout(new BorderLayout());

        JPanel painelCampos = new JPanel();
        painelCampos.setLayout(new GridLayout(4, 2));

        JLabel labelNome = new JLabel("Nome:");
        JTextArea campoNome = criarCampoTexto(arquivo.getNome());

        JLabel labelExtensao = new JLabel("Extensão:");
        JTextArea campoExtensao = criarCampoTexto(arquivo.getExtensao());

        JLabel labelPosicao = new JLabel("Posição:");
        JTextArea campoPosicao = criarCampoTexto(String.valueOf(arquivo.getPosicao()));

        JLabel labelTamanho = new JLabel("Tamanho:");
        JTextArea campoTamanho = criarCampoTexto(String.valueOf(arquivo.getTamanho()));

        painelCampos.add(labelNome);
        painelCampos.add(campoNome);
        painelCampos.add(labelExtensao);
        painelCampos.add(campoExtensao);
        painelCampos.add(labelPosicao);
        painelCampos.add(campoPosicao);
        painelCampos.add(labelTamanho);
        painelCampos.add(campoTamanho);

        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton botaoSalvar = criarBotao("Salvar arquivo", salvarAcao);
        JButton botaoEditar = criarBotao("Editar arquivo [append]", editarAcao);
        JButton botaoExcluir = criarBotao("Excluir arquivo", excluirAcao);

        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoEditar);
        painelBotoes.add(botaoExcluir);

        add(painelCampos, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    private JTextArea criarCampoTexto(String texto) {
        JTextArea campoTexto = new JTextArea(texto);
        campoTexto.setEditable(false);
        return campoTexto;
    }

    private JButton criarBotao(String texto, ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.addActionListener(acao);
        return botao;
    }
}
