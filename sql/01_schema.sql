DROP DATABASE IF EXISTS boutique_en_ligne;
CREATE DATABASE boutique_en_ligne;
USE boutique_en_ligne;

DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS categorie;
DROP TABLE IF EXISTS adresse;
DROP TABLE IF EXISTS produit;
DROP TABLE IF EXISTS commande;
DROP TABLE IF EXISTS ligne_commande;
DROP TABLE IF EXISTS paiement;
DROP TABLE IF EXISTS mouvement_stock;

CREATE TABLE clients (
	id_client INT AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(255) NOT NULL,
	prenom VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	telephone VARCHAR(12),
	date_inscription DATE NOT NULL DEFAULT (CURRENT_DATE),
	CONSTRAINT uq_clients_email UNIQUE (email)
) engine = InnoDB;

CREATE TABLE categorie (
	id_categorie INT AUTO_INCREMENT PRIMARY KEY,
	nom VARCHAR(100) NOT NULL,
	_description VARCHAR(255),
	CONSTRAINT uq_categorie_nom UNIQUE (nom)
) engine = InnoDB;

CREATE TABLE adresse (
	id_adresse INT AUTO_INCREMENT PRIMARY KEY,
	id_client INT NOT NULL,
	rue VARCHAR(255) NOT NULL,
	ville VARCHAR(255) NOT NULL,
	code_postal VARCHAR(255) NOT NULL,
	pays VARCHAR(255) NOT NULL,
	type_adresse VARCHAR(255) NOT NULL,
	INDEX idx_adresse_client (id_client),
	CONSTRAINT fk_adresse_client FOREIGN KEY (id_client) REFERENCES clients(id_client),
	CONSTRAINT chk_adresse_type CHECK (type_adresse IN ('livraison','facturation'))
) engine = InnoDB;

CREATE TABLE IF NOT EXISTS produit (
	id_produit INT AUTO_INCREMENT PRIMARY KEY,
	id_categorie INT NOT NULL,
	nom VARCHAR(255) NOT NULL,
	_description TEXT,
	prix DECIMAL(10,2) NOT NULL,
	stock INT NOT NULL DEFAULT 0,
	actif BOOLEAN NOT NULL DEFAULT TRUE,
	INDEX idx_produit_categorie (id_categorie),
	CONSTRAINT fk_produit_categorie FOREIGN KEY (id_categorie) REFERENCES categorie(id_categorie),
	CONSTRAINT chk_produit_prix CHECK (prix >= 0),
	CONSTRAINT chk_produit_stock CHECK (stock >= 0)
) engine = InnoDB;

CREATE TABLE commande (
	id_commande INT AUTO_INCREMENT PRIMARY KEY,
	id_client INT NOT NULL,
	date_commande DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	statut VARCHAR(25) NOT NULL DEFAULT 'panier',
	montant_total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
	INDEX idx_commande_client_date (id_client, date_commande),
	CONSTRAINT fk_commande_client FOREIGN KEY (id_client) REFERENCES clients(id_client),
	CONSTRAINT chk_commande_statut CHECK (statut IN ('panier','validee','expediee','livree','annulee')),
	CONSTRAINT chk_commande_montant CHECK (montant_total >= 0)
) engine = InnoDB;

CREATE TABLE ligne_commande (
	id_ligne INT AUTO_INCREMENT PRIMARY KEY,
	id_commande INT NOT NULL,
	id_produit INT NOT NULL,
	quantite INT NOT NULL,
	prix_unitaire DECIMAL(10,2) NOT NULL,
	CONSTRAINT uq_ligne_commande_produit UNIQUE (id_commande, id_produit),
	INDEX idx_ligne_produit (id_produit),
	CONSTRAINT fk_ligne_commande FOREIGN KEY (id_commande) REFERENCES commande(id_commande),
	CONSTRAINT fk_ligne_produit FOREIGN KEY (id_produit) REFERENCES produit(id_produit),
	CONSTRAINT chk_ligne_quantite CHECK (quantite > 0),
	CONSTRAINT chk_ligne_prix CHECK (prix_unitaire >= 0)
) engine = InnoDB;

CREATE TABLE paiement (
	id_paiement INT AUTO_INCREMENT PRIMARY KEY,
	id_commande INT NOT NULL,
	date_paiement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	montant DECIMAL(12,2) NOT NULL,
	mode_paiement VARCHAR(50) NOT NULL,
	statut VARCHAR(50) NOT NULL DEFAULT 'en_attente',
	INDEX idx_paiement_commande (id_commande),
	CONSTRAINT fk_paiement_commande FOREIGN KEY (id_commande) REFERENCES commande(id_commande),
	CONSTRAINT chk_paiement_montant CHECK (montant > 0),
	CONSTRAINT chk_paiement_statut CHECK (statut IN ('en_attente','accepte','refuse','rembourse'))
) engine = InnoDB;

CREATE TABLE mouvement_stock (
	id_mouvement INT AUTO_INCREMENT PRIMARY KEY,
	id_produit INT NOT NULL,
	type_mouvement VARCHAR(25) NOT NULL,
	quantite INT NOT NULL,
	date_mouvement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	motif VARCHAR(255),
	INDEX idx_mouvement_produit_date (id_produit, date_mouvement),
	CONSTRAINT fk_mouvement_produit FOREIGN KEY (id_produit) REFERENCES produit(id_produit),
	CONSTRAINT chk_mouvement_type CHECK (type_mouvement IN ('entree','sortie')),
	CONSTRAINT chk_mouvement_quantite CHECK (quantite > 0)
) engine = InnoDB;
