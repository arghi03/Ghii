-- CREATE DATABASE IF NOT EXISTS `register_perpustakaan` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE `register_perpustakaan`;

-- --------------------------------------------------------

--
-- Struktur tabel untuk `rolenew`
--
CREATE TABLE `rolenew` (
  `id_role` int(11) NOT NULL AUTO_INCREMENT,
  `nama_role` varchar(50) NOT NULL,
  PRIMARY KEY (`id_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Data awal untuk tabel `rolenew`
--
INSERT INTO `rolenew` (`id_role`, `nama_role`) VALUES
(1, 'Admin'),
(2, 'Supervisor'),
(3, 'User');

-- --------------------------------------------------------

--
-- Struktur tabel untuk `users`
--
CREATE TABLE `users` (
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  `nama` varchar(255) NOT NULL,
  `nim` varchar(25) NULL DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `nomor_telepon` varchar(20) NULL DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `id_role` int(11) NOT NULL,
  `otp_code` varchar(10) NULL DEFAULT NULL,
  `otp_expiry` datetime NULL DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `is_verified` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_user_role` (`id_role`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`id_role`) REFERENCES `rolenew` (`id_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Data awal untuk tabel `users` (Admin, Supervisor, dan User default)
--
INSERT INTO `users` (`nama`, `email`, `nim`, `nomor_telepon`, `password`, `id_role`, `is_active`, `is_verified`) VALUES
('admin', 'admin@app.com', '111', '08111', 'admin123', 1, 1, 1),
('supervisor', 'supervisor@app.com', '222', '08222', 'super123', 2, 1, 1),
('user', 'user@app.com', '333', '08333', 'user123', 3, 1, 1);

-- --------------------------------------------------------

--
-- Struktur tabel untuk `books`
--
CREATE TABLE `books` (
  `id_book` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `author` varchar(255) NOT NULL,
  `cover_image_path` text DEFAULT NULL,
  `book_file_path` text DEFAULT NULL,
  `rating` decimal(3,2) DEFAULT 0.00,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_book`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktur tabel untuk `loans`
--
CREATE TABLE `loans` (
  `id_loan` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `id_book` int(11) NOT NULL,
  `request_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `approved_date` datetime DEFAULT NULL,
  `return_date` datetime DEFAULT NULL,
  `expiry_date` datetime DEFAULT NULL,
  `status` enum('pending','approved','rejected','returned') NOT NULL DEFAULT 'pending',
  `approved_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_loan`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  FOREIGN KEY (`id_book`) REFERENCES `books` (`id_book`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktur tabel untuk `favorites`
--
CREATE TABLE `favorites` (
  `id_favorite` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `id_book` int(11) NOT NULL,
  `favorited_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_favorite`),
  UNIQUE KEY `user_book_favorite` (`id_user`,`id_book`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`),
  FOREIGN KEY (`id_book`) REFERENCES `books` (`id_book`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktur tabel untuk `book_suggestions`
--
CREATE TABLE `book_suggestions` (
  `id_suggestion` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `suggested_title` varchar(255) NOT NULL,
  `suggested_author` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `status` enum('pending','viewed','rejected','added') NOT NULL DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_suggestion`),
  FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;