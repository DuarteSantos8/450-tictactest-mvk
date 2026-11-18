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

# Nur der oeffentliche Schluessel geht zu AWS, der private bleibt auf dem eigenen Rechner
resource "aws_key_pair" "tictactest" {
  key_name   = "tictactest"
  public_key = file(pathexpand(var.public_key_path))
}

# Firewall: nur SSH (Port 22) rein, raus ist alles erlaubt (z.B. fuer Updates)
resource "aws_security_group" "tictactest" {
  name        = "tictactest"
  description = "SSH fuer TicTacTest"

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

  tags = {
    Name = "tictactest"
  }
}
