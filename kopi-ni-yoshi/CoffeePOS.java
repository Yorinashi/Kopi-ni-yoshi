import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CoffeePOS extends JFrame implements ActionListener {
    // Menu items, prices, and image paths
    private String[] menuItems = {"Espresso", "Americano", "Cappuccino", "Latte", "Mocha"};
    private double[] prices = {150.00, 175.00, 200.00, 225.00, 250.00};
    private String[] imagePaths = {
        "espresso.jpg", "americano.jpeg", "cappucino.jpg", "latte.jpg", "mocha.png"
    };

    // Components
    private JPanel menuPanel;
    private DefaultListModel<OrderItem> orderModel;
    private JList<OrderItem> orderList;
    private JLabel totalLabel;
    private JButton addButton, checkoutButton;

    // Order details
    private OrderItem selectedOrderItem = null; // Track selected item for adding to order

    public CoffeePOS() {
        // Frame settings
        setTitle("Coffee POS System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(111, 78, 55));

        // Initialize order details
        orderModel = new DefaultListModel<>();
        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(Color.WHITE);

        // Menu panel
        menuPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        menuPanel.setBackground(new Color(111, 78, 55));
        for (int i = 0; i < menuItems.length; i++) {
            int index = i; // Create a final copy of i
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(111, 78, 55));
            panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

            // Resize the image to a default size (e.g., 100x100 pixels)
            ImageIcon icon = new ImageIcon(imagePaths[i]);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(img), JLabel.CENTER);

            JLabel label = new JLabel(menuItems[i] + " - ₱" + String.format("%.2f", prices[i]), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            label.setForeground(Color.WHITE);

            panel.add(imageLabel, BorderLayout.CENTER);
            panel.add(label, BorderLayout.SOUTH);
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMenuItemClick(index);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    panel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                }
            });
            menuPanel.add(panel);
        }

        // Order list
        orderList = new JList<>(orderModel);
        orderList.setCellRenderer(new OrderListCellRenderer());
        JScrollPane orderScroll = new JScrollPane(orderList);

        // Buttons
        addButton = createStyledButton("Add to Order");
        addButton.addActionListener(this);

        checkoutButton = createStyledButton("Checkout");
        checkoutButton.addActionListener(this);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));
        buttonPanel.setBackground(new Color(111, 78, 55));
        buttonPanel.add(addButton);
        buttonPanel.add(checkoutButton);

        // Add components to frame
        add(menuPanel, BorderLayout.NORTH);
        add(orderScroll, BorderLayout.CENTER);
        add(totalLabel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // Center frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleMenuItemClick(int index) {
        selectedOrderItem = new OrderItem(menuItems[index], prices[index]);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            if (selectedOrderItem != null) {
                // Check if item is already in the list
                boolean itemFound = false;
                for (int i = 0; i < orderModel.size(); i++) {
                    OrderItem item = orderModel.get(i);
                    if (item.getName().equals(selectedOrderItem.getName())) {
                        item.incrementQuantity();
                        orderModel.set(i, item); // Update the item in the list model
                        itemFound = true;
                        break;
                    }
                }
                if (!itemFound) {
                    orderModel.addElement(selectedOrderItem);
                }
                updateTotal();
                selectedOrderItem = null; // Reset selection
                JOptionPane.showMessageDialog(this, "Item added to order.");
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to add.");
            }
        } else if (e.getSource() == checkoutButton) {
            handlePayment();
        }
    }

    private void updateTotal() {
        double total = 0.0;
        for (int i = 0; i < orderModel.size(); i++) {
            OrderItem item = orderModel.get(i);
            total += item.getTotalPrice();
        }
        totalLabel.setText("Total: ₱" + String.format("%.2f", total));
    }

    private void handlePayment() {
        String paymentStr = JOptionPane.showInputDialog(this, "Enter payment amount: ");
        try {
            double payment = Double.parseDouble(paymentStr);
            double total = getTotal();
            if (payment < total) {
                JOptionPane.showMessageDialog(this, "Insufficient payment. Please enter an amount greater than or equal to the total.");
            } else {
                double change = payment - total;
                JOptionPane.showMessageDialog(this, generateReceipt(payment, change), "Receipt", JOptionPane.INFORMATION_MESSAGE);
                orderModel.clear();
                updateTotal();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
        }
    }

    private String generateReceipt(double payment, double change) {
        StringBuilder receipt = new StringBuilder("Receipt:\n");
        for (int i = 0; i < orderModel.size(); i++) {
            OrderItem item = orderModel.get(i);
            receipt.append(item.getName()).append(" x").append(item.getQuantity())
                    .append(" - ₱").append(String.format("%.2f", item.getTotalPrice())).append("\n");
        }
        receipt.append("\nTotal: ₱").append(String.format("%.2f", getTotal()));
        receipt.append("\nPayment: ₱").append(String.format("%.2f", payment));
        receipt.append("\nChange: ₱").append(String.format("%.2f", change));
        return receipt.toString();
    }

    private double getTotal() {
        double total = 0.0;
        for (int i = 0; i < orderModel.size(); i++) {
            OrderItem item = orderModel.get(i);
            total += item.getTotalPrice();
        }
        return total;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    // Custom renderer for the order list to display items with quantity and delete button
    private class OrderListCellRenderer extends JPanel implements ListCellRenderer<OrderItem> {
        private JLabel nameLabel;
        private JLabel priceLabel;
        private JButton deleteButton;
        private JButton increaseButton;
        private JButton decreaseButton;

        public OrderListCellRenderer() {
            setLayout(new BorderLayout());
            nameLabel = new JLabel();
            priceLabel = new JLabel();
            deleteButton = createStyledButton("Delete");
            increaseButton = createStyledButton("+");
            decreaseButton = createStyledButton("-");

            // Set button action listeners
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JList<OrderItem> list = (JList<OrderItem>) SwingUtilities.getAncestorOfClass(JList.class, OrderListCellRenderer.this);
                    if (list != null) {
                        int index = list.getSelectedIndex();
                        if (index != -1) {
                            orderModel.remove(index);
                            updateTotal(); // Update the total after removal
                        }
                    }
                }
            });

            increaseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JList<OrderItem> list = (JList<OrderItem>) SwingUtilities.getAncestorOfClass(JList.class, OrderListCellRenderer.this);
                    if (list != null) {
                        int index = list.getSelectedIndex();
                        if (index != -1) {
                            OrderItem item = orderModel.get(index);
                            item.incrementQuantity();
                            orderModel.set(index, item); // Update the item in the list model
                            updateTotal(); // Update the total after quantity change
                        }
                    }
                }
            });

            decreaseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JList<OrderItem> list = (JList<OrderItem>) SwingUtilities.getAncestorOfClass(JList.class, OrderListCellRenderer.this);
                    if (list != null) {
                        int index = list.getSelectedIndex();
                        if (index != -1) {
                            OrderItem item = orderModel.get(index);
                            if (item.getQuantity() > 1) {
                                item.decrementQuantity();
                                orderModel.set(index, item); // Update the item in the list model
                            } else {
                                orderModel.remove(index);
                            }
                            updateTotal(); // Update the total after quantity change
                        }
                    }
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 3));
            buttonPanel.add(increaseButton);
            buttonPanel.add(decreaseButton);
            buttonPanel.add(deleteButton);

            add(nameLabel, BorderLayout.CENTER);
            add(priceLabel, BorderLayout.EAST);
            add(buttonPanel, BorderLayout.WEST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends OrderItem> list, OrderItem value, int index, boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(value.getName() + " x" + value.getQuantity());
            priceLabel.setText("₱" + String.format("%.2f", value.getTotalPrice()));
            setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            return this;
        }
    }

    // Custom class to represent an order item
    private static class OrderItem {
        private String name;
        private double price;
        private int quantity;

        public OrderItem(String name, double price) {
            this.name = name;
            this.price = price;
            this.quantity = 1;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void incrementQuantity() {
            quantity++;
        }

        public void decrementQuantity() {
            if (quantity > 1) {
                quantity--;
            }
        }

        public double getTotalPrice() {
            return price * quantity;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CoffeePOS::new);
    }
}
