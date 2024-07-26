package com.so.filesystem.painel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.so.filesystem.model.Arquivo;
import com.so.filesystem.Implementation;

import java.awt.*;
import java.io.*;
import java.util.List;

public class PainelPrincipal extends JFrame {
    private final JPanel listarPainel;
    private final List<Arquivo> arquivos;
    private final Implementation implementation;
    private final JLabel label;
    private Integer espacoDisponivel;

    public PainelPrincipal(Implementation implementation) {
        this.implementation = implementation;
        setTitle("Gerenciador de arquivos");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        espacoDisponivel = implementation.espacoDisponivel();
        label = new JLabel();
        label.setText("Espaço livre: " + formatarEspacoDisponivel(espacoDisponivel));

        arquivos = implementation.getArquivos();

        listarPainel = new JPanel();
        listarPainel.setLayout(new BoxLayout(listarPainel, BoxLayout.Y_AXIS));
        atualizarListaPainel();

        JScrollPane scrollPane = new JScrollPane(listarPainel);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JButton button = adicionarArquivo();
        add(button, BorderLayout.SOUTH);
        add(label, BorderLayout.NORTH);
    }

    private JButton adicionarArquivo() {
        JButton button = new JButton("Adicionar arquivo");
        button.addActionListener(e -> {
            File file = escolherArquivo();
            System.out.println(file.length());
            byte[] data = new byte[(int) file.length()];
            try (FileInputStream inp = new FileInputStream(file)) {
                inp.read(data);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println(file.getName());
            implementation.criar(file.getName(), data);
            atualizarEspacoLivre();
            atualizarListaPainel();
            listarPainel.revalidate();
            listarPainel.repaint();
        });
        return button;
    }

    private void atualizarEspacoLivre() {
        espacoDisponivel = implementation.espacoDisponivel();
        label.setText("Espaço livre: " + formatarEspacoDisponivel(espacoDisponivel));
    }

    private void atualizarListaPainel() {
        listarPainel.removeAll();
        for (Arquivo arquivo : arquivos) {
            PainelArquivo painelArquivo = new PainelArquivo(arquivo,
                    e -> {
                        byte[] data = implementation.ler(arquivo.getNome() + arquivo.getExtensao(), 0, -1);
                        salvarArquivo(data, arquivo.getNome() + "." + arquivo.getExtensao());
                        JOptionPane.showMessageDialog(PainelPrincipal.this, "Arquivo salvo: " + arquivo.getNome() + arquivo.getExtensao());
                    },
                    e -> {
                        File file = escolherArquivo();
                        byte[] data = new byte[(int) file.length()];
                        try (FileInputStream fis = new FileInputStream(file)) {
                            fis.read(data);
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                        implementation.append(arquivo.getNome(), data);
                        atualizarEspacoLivre();
                        atualizarListaPainel();
                        listarPainel.revalidate();
                        listarPainel.repaint();
                    },
                    e -> {
                        implementation.excluir(arquivo.getNome());
                        atualizarEspacoLivre();
                        atualizarListaPainel();
                        listarPainel.revalidate();
                        listarPainel.repaint();
                        JOptionPane.showMessageDialog(PainelPrincipal.this, "Arquivo excluído: " + arquivo);
                    }
            );
            listarPainel.add(painelArquivo);
        }
        listarPainel.revalidate();
        listarPainel.repaint();
    }

    public static File escolherArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showOpenDialog(null);
        return fileChooser.getSelectedFile();
    }

    public static void salvarArquivo(byte[] data, String nomeArquivo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File(nomeArquivo));
        fileChooser.setFileFilter(new FileNameExtensionFilter(nomeArquivo.split("\\.")[1], nomeArquivo.split("\\.")[1]));
        fileChooser.showSaveDialog(null);
        File file = fileChooser.getSelectedFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String formatarEspacoDisponivel(int freeSpace) {
        StringBuilder sb = new StringBuilder();
        String value = String.valueOf(freeSpace);

        if (value.length() > 9) {
            String s1 = value.substring(value.length() - 9, value.length() - 6);
            String s = value.substring(0, value.length() - 9);
            sb.append(s).append(".").append(s1).append("Gb");
        } else if (value.length() > 6) {
            String s1 = value.substring(value.length() - 6, value.length() - 3);
            String s = value.substring(0, value.length() - 6);
            sb.append(s).append(".").append(s1).append("Mb");
        } else if (value.length() > 3) {
            String s1 = value.substring(value.length() - 3);
            String s = value.substring(0, value.length() - 3);
            sb.append(s1).append(".").append(s).append("Kb");
        } else {
            sb.append(value).append("Bytes");
        }
        return sb.toString();
    }
}
