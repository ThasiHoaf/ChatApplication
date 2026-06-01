package ChatSystem.client.ui;

import ChatSystem.client.ClientManager;
import ChatSystem.shared.Message;
import ChatSystem.shared.MessageType;
import ChatSystem.shared.User;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class HistoryPanel extends JPanel {;
    private User user;

    private JTable historyArea;
    private DefaultTableModel historyTable;

    private JScrollPane scrollpane;

    private JButton deleteBtn;

    public HistoryPanel(ClientManager manager, User user){
        this.user = user;


        String[] columns = {"Time", "Content"};
        historyTable = new DefaultTableModel(columns, 0);
        historyArea = new JTable(historyTable);

        // Configure column widths and enable wrapping in content column
        historyArea.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        historyArea.setRowHeight(20);
        historyArea.getColumnModel().getColumn(0).setPreferredWidth(150); // Time
        historyArea.getColumnModel().getColumn(1).setPreferredWidth(500); // Content

        // Multiline cell renderer to wrap text and auto-size rows
        class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
            public MultiLineTableCellRenderer() {
                setLineWrap(true);
                setWrapStyleWord(true);
                setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                setText(value == null ? "" : value.toString());
                setFont(table.getFont());
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                // adjust row height to fit the text
                int prefHeight = getPreferredSize().height;
                if (table.getRowHeight(row) != prefHeight) {
                    table.setRowHeight(row, prefHeight);
                }
                return this;
            }
        }

        historyArea.getColumnModel().getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());

        scrollpane = new JScrollPane(historyArea);
        setLayout(new BorderLayout());
        add(scrollpane, BorderLayout.CENTER);

        add(new JLabel("Chat History", SwingConstants.CENTER), BorderLayout.NORTH);

        deleteBtn = new JButton("Delete");
        add(deleteBtn, BorderLayout.SOUTH);

        setupEvent(manager);
    }

    private void setupEvent(ClientManager mamager){
        deleteBtn.addActionListener(e -> {
            Message deleteMessage = new Message(MessageType.HISTORY_DELETE);
            deleteMessage.setSender(user.getUserName());

            int selectedRow = historyArea.getSelectedRow();

            if(selectedRow != -1){
                String content = (String) historyArea.getValueAt(selectedRow, 1);
                String[] contents = content.split(":");
                String messageId = contents[0];
                deleteMessage.setContent(messageId);
                mamager.sendMessage(deleteMessage);
            }
        });
    }
    public void setHistoryArea(long id, String content, String time){
        String[] row = {time, id + ":" + content};
        historyTable.addRow(row);

        // After adding the row, ensure the row height is large enough to display wrapped content
        int rowIndex = historyTable.getRowCount() - 1;
        int col = 1; // content column
        try {
            int colWidth = historyArea.getColumnModel().getColumn(col).getWidth();
            TableCellRenderer renderer = historyArea.getCellRenderer(rowIndex, col);
            Component comp = renderer.getTableCellRendererComponent(historyArea, historyTable.getValueAt(rowIndex, col), false, false, rowIndex, col);
            // Allow component to wrap to the column width for correct preferred height calculation
            comp.setSize(colWidth, Short.MAX_VALUE);
            int prefHeight = comp.getPreferredSize().height;
            if (historyArea.getRowHeight(rowIndex) != prefHeight) {
                historyArea.setRowHeight(rowIndex, prefHeight);
            }
        } catch (Exception e) {
            // fallback: do nothing
        }
    }

    public void clearHistory(){
        historyTable.setRowCount(0);
    }

    public void removeHistoryById(String id){
        if(id == null) return;
        for (int i = historyTable.getRowCount() - 1; i >= 0; i--) {
            Object cell = historyTable.getValueAt(i, 1);
            if (cell instanceof String){
                String s = (String) cell;
                if (s.startsWith(id + ":")){
                    historyTable.removeRow(i);
                }
            }
        }
    }
}