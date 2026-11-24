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

variable "ttyd_version" {
  description = "Feste ttyd-Version (Web-Konsole)"
  type        = string
  default     = "1.7.7"
}

variable "ttyd_sha256" {
  description = "SHA-256 von ttyd.x86_64 (aus SHA256SUMS vom ttyd-Release)"
  type        = string
  default     = "8a217c968aba172e0dbf3f34447218dc015bc4d5e59bf51db2f2cd12b7be4f55"
}

variable "repo" {
  description = "GitHub-Repository, aus dessen Releases das JAR geladen wird"
  type        = string
  default     = "DuarteSantos8/450-tictactest-mvk"
}
