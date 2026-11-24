terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

# Provider: verbindet OpenTofu mit AWS. Die Zugangsdaten kommen aus ~/.aws/credentials, nicht aus dem Code.
provider "aws" {
  region = var.region
}

# Aktuelles Amazon Linux 2023 Image suchen (statt eine AMI-ID fest einzutragen)
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# Im Learner Lab darf man keine eigenen IAM-Rollen anlegen, darum das vorhandene Profil verwenden.
# Damit darf die Instanz z.B. SSM-Befehle empfangen, ohne dass Access Keys auf der Instanz liegen.
data "aws_iam_instance_profile" "lab" {
  name = "LabInstanceProfile"
}

# Nur der oeffentliche Schluessel geht zu AWS, der private bleibt auf dem eigenen Rechner
resource "aws_key_pair" "tictactest" {
  key_name   = "tictactest"
  public_key = file(pathexpand(var.public_key_path))
}

# Firewall: nur das Noetigste rein, raus ist alles erlaubt (Pakete, GitHub-Release-Download, SSM)
resource "aws_security_group" "tictactest" {
  name        = "tictactest"
  description = "Web-Konsole fuer TicTacTest"

  # nginx: Frontend, Terminal (/tty/) und Versionsabfrage fuer alle Spieler
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # nur zur Fehlersuche, das Deployment laeuft ueber SSM und braucht SSH nicht
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "tictactest" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.tictactest.key_name
  vpc_security_group_ids = [aws_security_group.tictactest.id]
  iam_instance_profile   = data.aws_iam_instance_profile.lab.name

  # Richtet die Instanz beim ersten Start ein (Java, ttyd, nginx, Dienst, config.js). Enthaelt KEINE App-Version,
  # darum muss die Instanz fuer einen neuen Release nicht neu erstellt werden.
  user_data = templatefile("${path.module}/setup.sh", {
    ttyd_version = var.ttyd_version
    ttyd_sha256  = var.ttyd_sha256
    repo         = var.repo
  })
  # user_data laeuft nur beim allerersten Start. Aendert sich die Einrichtung, wird darum eine neue Instanz
  # erstellt (die Elastic IP bleibt). Danach muss die App neu deployt werden (siehe docs/deployment.md).
  user_data_replace_on_change = true

  tags = {
    Name = "tictactest"
  }
}

# Feste oeffentliche IP: bleibt gleich, auch wenn das Learner Lab die Instanz stoppt und wieder startet
resource "aws_eip" "tictactest" {
  instance = aws_instance.tictactest.id
  domain   = "vpc"
}
