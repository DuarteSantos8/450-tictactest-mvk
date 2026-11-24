output "frontend_url" {
  description = "Hier spielen (Frontend)"
  value       = "http://${aws_eip.tictactest.public_ip}/"
}

output "terminal_url" {
  description = "URL der Web-Konsole"
  value       = "http://${aws_eip.tictactest.public_ip}/tty/"
}

output "version_url" {
  description = "Versionsabfrage des Backends"
  value       = "http://${aws_eip.tictactest.public_ip}/backend/version.txt"
}

output "instance_id" {
  description = "Instanz-ID fuer das Deployment (aws ssm send-command)"
  value       = aws_instance.tictactest.id
}

output "public_ip" {
  description = "Feste oeffentliche IP (Elastic IP)"
  value       = aws_eip.tictactest.public_ip
}

output "ssh_command" {
  description = "Befehl fuer den SSH-Login (nur zur Fehlersuche)"
  value       = "ssh -i ~/.ssh/tictactest ec2-user@${aws_eip.tictactest.public_ip}"
}
