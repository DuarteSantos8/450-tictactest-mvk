output "public_ip" {
  description = "Oeffentliche IP der Instanz"
  value       = aws_instance.tictactest.public_ip
}

output "public_dns" {
  description = "Oeffentlicher DNS-Name der Instanz"
  value       = aws_instance.tictactest.public_dns
}

output "ssh_command" {
  description = "Befehl fuer den SSH-Login"
  value       = "ssh -i ~/.ssh/tictactest ec2-user@${aws_instance.tictactest.public_ip}"
}
