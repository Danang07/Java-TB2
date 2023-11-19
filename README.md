# Java-TB2

1. Buat proyek java di apache netbeans. nama Proyek "Perpustakaan"
2. Copy code Perpustakaan.Java (dalam folder ini) ke apache netbeans Perpustakaan.Java
3. Buat koneksi MySql ke Aplikasi dengan cara :  klik kanan pada Libraries dan pilih "add Jar", masukan file jarnya (ada di folder ini)
![image](https://github.com/Danang07/Java-TB2/assets/93381326/7dc2e4ca-74b5-4a51-a6c0-694d529a6d70)
4. buat database di Mysql dengan memasukan syntax dibawah

//membuat database
CREATE DATABASE perpustakaan;
//memilih database
USE perpustakaan;
//membuat tabel buku
CREATE TABLE buku (
  id INT AUTO_INCREMENT PRIMARY KEY,
  judul VARCHAR(255) NOT NULL,
  pengarang VARCHAR(255) NOT NULL,
  stok INT NOT NULL
);

5. running java nya. pastiin XAMPP menyala pada bagian MYSQL
