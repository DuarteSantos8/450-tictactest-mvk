variable "region" {
  description = "AWS-Region (im Learner Lab us-east-1)"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "Groesse der EC2-Instanz"
  type        = string
  default     = "t2.micro"
}

variable "public_key_path" {
  description = "Pfad zum oeffentlichen SSH-Schluessel (der private Schluessel bleibt lokal)"
  type        = string
  default     = "~/.ssh/tictactest.pub"
}

variable "ssh_cidr" {
  description = "IP-Bereich, der per SSH zugreifen darf (besser: eigene IP, z.B. 1.2.3.4/32)"
  type        = string
  default     = "0.0.0.0/0"
}
