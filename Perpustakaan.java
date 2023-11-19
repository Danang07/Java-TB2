import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Perpustakaan extends JFrame {

  private JTextField judulField;
  private JTextField pengarangField;
  private JTextField stokField;
  private JTable tabelBuku;
  private DefaultTableModel tableModel;
  private int barisTerpilih;
  private JTextField fieldPencarian;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new Perpustakaan());
  }

  public class DatabaseUtil {

    private static final String URL =
      "jdbc:mysql://localhost:3306/perpustakaan";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
      return DriverManager.getConnection(URL, USER, PASSWORD);
    }
  }

  public Perpustakaan() {
    super("Aplikasi Perpustakaan");
    // Inisialisasi komponen GUI
    judulField = new JTextField(20);
    pengarangField = new JTextField(20);
    stokField = new JTextField(5);
    JButton tambahButton = new JButton("Tambah Buku");
    JButton updateButton = new JButton("Edit Buku");
    JButton hapusButton = new JButton("Hapus Buku");

    // Tambahkan aksi pada tombol tambah
    tambahButton.addActionListener(e -> tambahBuku());

    // Tambahkan aksi pada tombol update
    updateButton.addActionListener(e -> updateBuku());

    // Tambahkan aksi pada tombol hapus
    hapusButton.addActionListener(e -> hapusBuku());

    // Inisialisasi tabel
    tableModel =
      new DefaultTableModel(new Object[] { "Judul", "Pengarang", "Stok" }, 0);
    tabelBuku = new JTable(tableModel);

    // Menambahkan aksi untuk memilih baris pada tabel
    tabelBuku
      .getSelectionModel()
      .addListSelectionListener(e -> {
        barisTerpilih = tabelBuku.getSelectedRow();
        // Enable/disable tombol update dan hapus sesuai dengan pemilihan baris
        updateButton.setEnabled(barisTerpilih != -1);
        hapusButton.setEnabled(barisTerpilih != -1);
      });

    // Membuat layout
    JPanel panelInput = new JPanel(new GridLayout(10, 2, 10, 10));
    JLabel labelInstruksi = new JLabel("Masukkan Data Buku");
    panelInput.add(labelInstruksi);
    panelInput.add(new JLabel(""));
    panelInput.add(new JLabel("Judul:"));
    panelInput.add(judulField);
    panelInput.add(new JLabel("Pengarang:"));
    panelInput.add(pengarangField);
    panelInput.add(new JLabel("Stok:"));
    panelInput.add(stokField);

    // Tambahkan komponen pencarian ke dalam panelInput
    panelInput.add(new JLabel(""));
    JLabel labelPencarian = new JLabel("Cari Buku:");
    fieldPencarian = new JTextField(10);
    panelInput.add(new JLabel(""));
    JButton cariButton = new JButton("Cari");

    // Tambahkan aksi pada tombol cari
    cariButton.addActionListener(e -> cariBuku(fieldPencarian.getText()));

    panelInput.add(labelPencarian);
    panelInput.add(fieldPencarian);
    panelInput.add(cariButton);

    // Atur lebar tombol pencarian
    Dimension buttonSize = cariButton.getPreferredSize();
    buttonSize.width = 10; // Sesuaikan lebar sesuai kebutuhan
    cariButton.setPreferredSize(buttonSize);

    // Menambahkan tabel ke dalam JScrollPane untuk mendukung scrollbar jika diperlukan
    JScrollPane scrollPane = new JScrollPane(tabelBuku);

    // Menambahkan judul "Data Buku" di atas tabel
    JLabel labelDataBuku = new JLabel("Data Buku", SwingConstants.CENTER);
    scrollPane.setColumnHeaderView(labelDataBuku);

    // Menyusun komponen-komponen ke dalam panel utama
    JPanel panelUtama = new JPanel(new BorderLayout());
    panelUtama.add(panelInput, BorderLayout.NORTH);
    panelUtama.add(scrollPane, BorderLayout.CENTER);

    // Panel untuk tombol tambah, update, dan hapus
    JPanel panelTombol = new JPanel(new FlowLayout());
    panelTombol.add(tambahButton);
    panelTombol.add(updateButton);
    panelTombol.add(hapusButton);

    panelUtama.add(panelTombol, BorderLayout.SOUTH);

    // Menambahkan panel utama ke dalam frame
    add(panelUtama);

    // Mengatur layout frame
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 600);
    setLocationRelativeTo(null);
    setVisible(true);

    // Memuat data awal dari database
    loadDataFromDatabase();
  }

  private void tambahBuku() {
    String judul = judulField.getText().trim();
    String pengarang = pengarangField.getText().trim();
    String stokString = stokField.getText().trim();

    // Validasi tidak boleh kosong
    if (judul.isEmpty() || pengarang.isEmpty() || stokString.isEmpty()) {
      JOptionPane.showMessageDialog(
        this,
        "Harap isi semua field.",
        "Peringatan",
        JOptionPane.WARNING_MESSAGE
      );
      return;
    }

    try {
      int stok = Integer.parseInt(stokString);

      Buku buku = new Buku(judul, pengarang, stok);

      try (Connection connection = DatabaseUtil.getConnection()) {
        tambahBuku(connection, buku);
        JOptionPane.showMessageDialog(this, "Buku berhasil ditambahkan ");
        loadDataFromDatabase(); // Memuat ulang data setelah penambahan buku
      } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
          this,
          "Gagal menambahkan buku.",
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
      }
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(
        this,
        "Stok harus berupa angka.",
        "Peringatan",
        JOptionPane.WARNING_MESSAGE
      );
    }
  }

  private void updateBuku() {
    if (barisTerpilih == -1) {
      JOptionPane.showMessageDialog(
        this,
        "Pilih buku yang akan diupdate.",
        "Peringatan",
        JOptionPane.WARNING_MESSAGE
      );
      return;
    }

    Buku bukuTerpilih = getBukuDariBaris(barisTerpilih);

    // Menampilkan dialog untuk memasukkan data yang baru
    JTextField judulBaruField = new JTextField(bukuTerpilih.getJudul());
    JTextField pengarangBaruField = new JTextField(bukuTerpilih.getPengarang());
    JTextField stokBaruField = new JTextField(
      String.valueOf(bukuTerpilih.getStok())
    );

    JPanel myPanel = new JPanel();
    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
    myPanel.add(new JLabel("Judul Baru:"));
    myPanel.add(judulBaruField);
    myPanel.add(new JLabel("Pengarang Baru:"));
    myPanel.add(pengarangBaruField);
    myPanel.add(new JLabel("Stok Baru:"));
    myPanel.add(stokBaruField);

    int result = JOptionPane.showConfirmDialog(
      this,
      myPanel,
      "Masukkan data baru:",
      JOptionPane.OK_CANCEL_OPTION
    );

    if (result == JOptionPane.OK_OPTION) {
      // Mendapatkan nilai baru dari inputan pengguna
      String judulBaru = judulBaruField.getText().trim();
      String pengarangBaru = pengarangBaruField.getText().trim();
      String stokBaruString = stokBaruField.getText().trim();

      // Validasi tidak boleh kosong
      if (
        judulBaru.isEmpty() ||
        pengarangBaru.isEmpty() ||
        stokBaruString.isEmpty()
      ) {
        JOptionPane.showMessageDialog(
          this,
          "Harap isi semua field.",
          "Peringatan",
          JOptionPane.WARNING_MESSAGE
        );
        return;
      }

      try {
        int stokBaru = Integer.parseInt(stokBaruString);

        Buku bukuBaru = new Buku(judulBaru, pengarangBaru, stokBaru);

        try (Connection connection = DatabaseUtil.getConnection()) {
          updateBuku(connection, bukuBaru, bukuTerpilih);
          JOptionPane.showMessageDialog(this, "Buku berhasil diupdate");
          loadDataFromDatabase(); // Memuat ulang data setelah pengupdatean buku
        } catch (SQLException ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(
            this,
            "Gagal mengupdate buku.",
            "Error",
            JOptionPane.ERROR_MESSAGE
          );
        }
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
          this,
          "Stok harus berupa angka.",
          "Peringatan",
          JOptionPane.WARNING_MESSAGE
        );
      }
    }
  }

  private void hapusBuku() {
    if (barisTerpilih == -1) {
      JOptionPane.showMessageDialog(
        this,
        "Pilih buku yang akan dihapus.",
        "Peringatan",
        JOptionPane.WARNING_MESSAGE
      );
      return;
    }

    int konfirmasi = JOptionPane.showConfirmDialog(
      this,
      "Apakah Anda yakin ingin menghapus buku ini?",
      "Konfirmasi",
      JOptionPane.YES_NO_OPTION
    );
    if (konfirmasi == JOptionPane.YES_OPTION) {
      Buku bukuTerpilih = getBukuDariBaris(barisTerpilih);

      try (Connection connection = DatabaseUtil.getConnection()) {
        hapusBuku(connection, bukuTerpilih);
        JOptionPane.showMessageDialog(this, "Buku berhasil dihapus ");
        loadDataFromDatabase(); // Memuat ulang data setelah penghapusan buku
      } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
          this,
          "Gagal menghapus buku.",
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  private void tambahBuku(Connection connection, Buku buku)
    throws SQLException {
    String query = "INSERT INTO Buku (judul, pengarang, stok) VALUES (?, ?, ?)";
    try (
      PreparedStatement preparedStatement = connection.prepareStatement(query)
    ) {
      preparedStatement.setString(1, buku.getJudul());
      preparedStatement.setString(2, buku.getPengarang());
      preparedStatement.setInt(3, buku.getStok());
      preparedStatement.executeUpdate();
    }
  }

  private void updateBuku(Connection connection, Buku bukuBaru, Buku bukuLama)
    throws SQLException {
    String query = "UPDATE Buku SET judul=?, pengarang=?, stok=? WHERE judul=?";
    try (
      PreparedStatement preparedStatement = connection.prepareStatement(query)
    ) {
      preparedStatement.setString(1, bukuBaru.getJudul());
      preparedStatement.setString(2, bukuBaru.getPengarang());
      preparedStatement.setInt(3, bukuBaru.getStok());
      preparedStatement.setString(4, bukuLama.getJudul());
      preparedStatement.executeUpdate();
    }
  }

  private void hapusBuku(Connection connection, Buku buku) throws SQLException {
    String query = "DELETE FROM Buku WHERE judul=?";
    try (
      PreparedStatement preparedStatement = connection.prepareStatement(query)
    ) {
      preparedStatement.setString(1, buku.getJudul());
      preparedStatement.executeUpdate();
    }
  }

  private Buku getBukuDariBaris(int baris) {
    String judul = (String) tabelBuku.getValueAt(baris, 0);
    String pengarang = (String) tabelBuku.getValueAt(baris, 1);
    int stok = (int) tabelBuku.getValueAt(baris, 2);
    return new Buku(judul, pengarang, stok);
  }

  private void cariBuku(String keyword) {
    // Memuat data sesuai dengan kata kunci pencarian
    loadDataFromDatabase();
  }

  private void loadDataFromDatabase() {
    // Membersihkan data tabel sebelum memuat data baru
    tableModel.setRowCount(0);

    try (Connection connection = DatabaseUtil.getConnection()) {
      String query = "SELECT * FROM Buku";

      // Jika ada kata kunci pencarian, tambahkan klausa WHERE
      if (!fieldPencarian.getText().isEmpty()) {
        query += " WHERE judul LIKE ? OR pengarang LIKE ?";
      }

      try (
        PreparedStatement preparedStatement = connection.prepareStatement(query)
      ) {
        // Jika ada kata kunci pencarian, atur parameter
        if (!fieldPencarian.getText().isEmpty()) {
          preparedStatement.setString(1, "%" + fieldPencarian.getText() + "%");
          preparedStatement.setString(2, "%" + fieldPencarian.getText() + "%");
        }

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          // Memuat data dari ResultSet ke tabel
          while (resultSet.next()) {
            String judul = resultSet.getString("judul");
            String pengarang = resultSet.getString("pengarang");
            int stok = resultSet.getInt("stok");

            // Menambahkan baris baru ke tabel
            tableModel.addRow(new Object[] { judul, pengarang, stok });
          }
        }
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
        this,
        "Gagal memuat data dari database.",
        "Error",
        JOptionPane.ERROR_MESSAGE
      );
    }
  }

  public class Buku {

    private String judul;
    private String pengarang;
    private int stok;

    public Buku(String judul, String pengarang, int stok) {
      this.judul = judul;
      this.pengarang = pengarang;
      this.stok = stok;
    }

    public String getJudul() {
      return judul;
    }

    public String getPengarang() {
      return pengarang;
    }

    public int getStok() {
      return stok;
    }
  }
}
